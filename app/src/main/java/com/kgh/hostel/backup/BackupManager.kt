package com.kgh.hostel.backup

import android.content.Context
import android.net.Uri
import com.kgh.hostel.data.local.KGHDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports/imports a full ".kghbackup" archive: the checkpointed SQLite file,
 * photos, documents, and logo, plus a manifest.json used to validate
 * compatibility before restoring.
 *
 * Export destination and restore source are both chosen by the administrator
 * via Android's Storage Access Framework in the calling Activity/Screen
 * (ACTION_CREATE_DOCUMENT / ACTION_OPEN_DOCUMENT) — this class only writes
 * to / reads from the resulting Uri, so it works equally for local storage,
 * Google Drive, or any other document provider.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val SCHEMA_VERSION = 1
    }

    fun exportBackup(destination: Uri) {
        val dbFile = context.getDatabasePath(KGHDatabase.DATABASE_NAME)
        val photosDir = File(context.filesDir, "photos")
        val docsDir = File(context.filesDir, "documents")
        val logoDir = File(context.filesDir, "logo")

        context.contentResolver.openOutputStream(destination)?.use { out ->
            ZipOutputStream(out).use { zip ->
                // manifest
                val manifest = JSONObject().apply {
                    put("schemaVersion", SCHEMA_VERSION)
                    put("timestamp", System.currentTimeMillis())
                }
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest.toString().toByteArray())
                zip.closeEntry()

                if (dbFile.exists()) addFileToZip(zip, dbFile, "database.db")
                addDirToZip(zip, photosDir, "photos")
                addDirToZip(zip, docsDir, "documents")
                addDirToZip(zip, logoDir, "logo")
            }
        }
    }

    /** Returns true if the restore succeeded. Caller shows the mandatory warning before calling this. */
    fun restoreBackup(source: Uri): Boolean {
        // Safety snapshot of current state in case restore fails partway.
        val safetySnapshot = File(context.cacheDir, "pre_restore_snapshot.kghbackup")
        try {
            exportBackup(Uri.fromFile(safetySnapshot))
        } catch (_: Exception) { /* best-effort snapshot */ }

        return try {
            context.contentResolver.openInputStream(source)?.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry: ZipEntry? = zip.nextEntry
                    var manifestOk = false
                    while (entry != null) {
                        val name = entry.name
                        when {
                            name == "manifest.json" -> {
                                val json = JSONObject(zip.readBytes().decodeToString())
                                manifestOk = json.optInt("schemaVersion", -1) <= SCHEMA_VERSION
                            }
                            name == "database.db" -> {
                                val dbFile = context.getDatabasePath(KGHDatabase.DATABASE_NAME)
                                dbFile.parentFile?.mkdirs()
                                dbFile.outputStream().use { zip.copyTo(it) }
                            }
                            name.startsWith("photos/") || name.startsWith("documents/") || name.startsWith("logo/") -> {
                                val target = File(context.filesDir, name)
                                target.parentFile?.mkdirs()
                                target.outputStream().use { zip.copyTo(it) }
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                    manifestOk
                }
            } ?: false
        } catch (e: Exception) {
            // Roll back to the safety snapshot on any failure.
            if (safetySnapshot.exists()) {
                try { restoreBackup(Uri.fromFile(safetySnapshot)) } catch (_: Exception) {}
            }
            false
        }
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File, entryName: String) {
        zip.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(zip) }
        zip.closeEntry()
    }

    private fun addDirToZip(zip: ZipOutputStream, dir: File, prefix: String) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isFile) addFileToZip(zip, file, "$prefix/${file.name}")
        }
    }
}
