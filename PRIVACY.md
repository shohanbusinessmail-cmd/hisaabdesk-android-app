# Privacy

## Current behavior

The app currently stores account labels, transaction metadata, and ledger postings in a local Room/SQLite database in the app's private storage. It contains no networking library or `INTERNET` permission, and does not implement account sign-in, cloud sync, analytics, advertising, or remote backup. The app does not ask for contacts, location, camera, media, or broad storage permissions. The current manifest sets `allowBackup=false`.

Room is not encrypted by this project. Anyone able to access the app's private data through a compromised device may be able to inspect it. There is no PIN or biometric lock and no export/import yet. Uninstalling or clearing app data can permanently remove the database. Make no assumption that records are backed up.

## Future export

JSON/CSV backup and restore are not implemented. If added, exports should remain user-initiated through Android's Storage Access Framework, exclude device secrets and PIN material, and explain whether attachment bytes are included. No records are transmitted to a remote service in the current implementation.

This file describes the code in this repository and is not a legal privacy notice for a future release with different dependencies or behavior. Re-check merged manifest permissions and SDK behavior for every release.
