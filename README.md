# KGH — My Khandelwal Girls Hostel

An offline-first Android hostel-management app built with Kotlin, Jetpack Compose,
Room (SQLite), and Hilt — for the private/internal management of Khandelwal Girls
Hostel, Aurangabad. Not intended for the Play Store.

## What's implemented

- **Data layer**: Room database with entities/DAOs for Students, Documents, Rooms,
  Beds, Room-Change History, Attendance, Leave Applications, Hostel Leaving (with
  refund table), Payments (+ audit log), Hostel Settings, Audit Log.
- **Admission**: full form using the exact fields from the physical Admission Form,
  including the declaration & undertaking, plus room/bed selection at admission time.
- **Leave Application**: exact fields from the physical Leave form, including the
  office-use approval workflow (Grant/Reject, hostel in-charge name).
- **Hostel Leaving**: exact fields from the physical Hostel Leaving form, including
  the security-deposit refund table. Confirming it frees the student's bed and marks
  her "Hostel Left" — no data is deleted, all history is preserved.
- **Rooms & Beds**: add rooms (auto-generates beds), vacant-beds list, change
  room/bed with history, one-bed-one-student enforced via a transactional DAO method.
- **Attendance**: quick daily present/absent marking, monthly history summary.
- **Rent/Payments**: record payments per student per month, automatic due/partial/paid
  status, rent-due list, edit audit trail.
- **Dashboard**: live counts (students, present/absent, rooms, beds, rent due, on leave).
- **Security**: Admin PIN, salted-hashed and stored via Android Keystore-backed
  EncryptedSharedPreferences — never stored in plaintext.
- **Backup/Restore**: exports a `.kghbackup` zip (database + photos + documents +
  logo + manifest) to any location via Android's Storage Access Framework (local
  storage, Google Drive, or share sheet). Restore validates the manifest, takes an
  automatic safety snapshot first, and rolls back if anything fails.
- **Clear Data**: scoped (attendance only / temp data / everything), protected by a
  typed `CLEAR KGH DATA` confirmation phrase.

## What's stubbed or simplified (clearly marked in code with comments)

These are straightforward to finish but were simplified to keep this buildable
package focused — each is a small, self-contained addition using patterns already
established elsewhere in the code:

- **Date pickers**: admission/leave/leaving dates use placeholder text fields;
  swap in `DatePickerDialog` (Material3 has a built-in composable) where noted.
- **Photo & document capture**: `photoUri`/document fields exist end-to-end in the
  data layer; wiring the camera/gallery picker (`ActivityResultContracts.PickVisualMedia`
  or `TakePicture`) into the Admission form is the one remaining UI step.
- **Student Edit form**: reuses the Admission form's fields — currently a stub button;
  pre-fill `AdmissionFormState` from the existing `Student` and call `update()` instead
  of `admitStudent()`.
- **WhatsApp reminder button**: the Rent Due screen has the button in place; wire it to
  `Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/<number>?text=..."))`.
- **Reports export (PDF/CSV)**: the Reports screen lists all nine report categories;
  each would query its repository (already built) and render via `PdfDocument` or a
  simple CSV writer — same shape for all nine.
- **Room detail bed grid**: `RoomListScreen`'s row tap is currently a no-op; a
  `RoomDetailScreen` showing `observeBedsForRoom` as a grid (same data already
  exposed by `RoomRepository`) is the natural next screen.

None of these affect the data model or the pieces you specifically asked to see
first (admission/leave/hostel-leaving forms, backup/restore, room/bed integrity).

## Opening and building

You have two options: build it yourself with Android Studio (full control, good
for making further changes), or let GitHub build it for you in the cloud with
nothing installed on your computer (fastest if you just want the APK).

### Option A — No install, build in the cloud (GitHub Actions)

This project already includes a ready-made build workflow at
`.github/workflows/build-apk.yml`. GitHub's free tier runs it for you.

1. Create a free account at github.com (if you don't have one).
2. Click **New repository** (the "+" icon top-right → New repository). Name it
   anything (e.g. `kgh-hostel-app`), and it can be Public or Private — either works.
3. On the new repo's page, click **Add file → Upload files**, then drag the whole
   unzipped `KGH` folder into the browser window (modern GitHub supports dragging
   entire folders, not just single files). Commit the upload.
4. Click the **Actions** tab at the top of the repo. A run should already be
   in progress (triggered by your upload); if not, select the workflow on the
   left and click **Run workflow**.
5. Wait for it to finish — a few minutes, shown by a spinning yellow icon
   turning into a green checkmark.
6. Click the completed run, scroll to the **Artifacts** section at the bottom,
   and download **KGH-debug-apk** — it's a zip containing `app-debug.apk`.
7. Unzip that, then get `app-debug.apk` onto your phone any way you like
   (email it to yourself, Google Drive, WhatsApp) and tap it to install —
   you'll need to allow "install from unknown sources" the first time.

No phone connection and no local install needed for this path.

### Option B — Android Studio on your computer

1. Install **Android Studio** (Koala or newer recommended).
2. Open this `KGH/` folder as a project (**File → Open**).
3. Let Android Studio sync Gradle — it will download the Gradle distribution and all
   dependencies automatically (this needs an internet connection once).
4. If Android Studio asks about the Gradle wrapper, let it regenerate
   `gradlew`/`gradlew.bat` — this environment couldn't produce the wrapper's binary
   jar without network access, so the wrapper properties are included but the jar
   itself is not.
5. Build → **Build Bundle(s) / APK(s) → Build APK(s)**, or just click ▶ Run with a
   device/emulator connected.
6. The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk` — copy it to
   your phone and install it (you'll need to allow "install from unknown sources"
   since this isn't a Play Store app).

## Project structure

```
app/src/main/java/com/kgh/hostel/
  data/local/          Room entities, DAOs, database, type converters
  data/repository/      Repository layer (business rules: bed assignment,
                        hostel leaving, payment status, admission numbering)
  di/                   Hilt module wiring the database and DAOs
  security/             PIN hashing/verification
  backup/               .kghbackup export/import
  ui/                    One package per module (dashboard, students, admission,
                        rooms, attendance, leave, hostelleaving, payments,
                        reports, settings, security, navigation)
```

## Data safety notes

- No `fallbackToDestructiveMigration()` is used — a future schema change must ship
  a real Room `Migration`, so a normal app update will never wipe your data.
- `allowBackup="false"` in the manifest is intentional: Android's own auto-backup
  is disabled in favor of your explicit, complete `.kghbackup` export, which also
  includes photos and documents (Android's auto-backup does not reliably include
  large app-private files).
