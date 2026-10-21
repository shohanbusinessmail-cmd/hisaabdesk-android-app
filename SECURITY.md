# Security

## Current status and data storage

This is an early local-only build. Room uses the Android app's private internal database path, but this project does **not** encrypt the Room database. It does not implement a PIN, biometric gate, inactivity lock, encrypted attachments, or a local recovery flow. App-private storage is not equivalent to encrypted-at-rest financial storage. Device compromise, rooted access, debugging/backups configured by the platform, or a user with access to an unlocked device may expose records. No sensitive real-world use is recommended until the missing protections are implemented and reviewed.

The manifest disables Android app backup for now to avoid an undocumented plaintext database copy. This means uninstalling/clearing app data can erase records. No recovery is available yet. The app currently requests no network permission and contains no authentication tokens or custom cryptography.

## Signing keys

Release signing is expected to use the existing keystore stored as GitHub Actions secrets. Keep the original key and passwords in separate secure backups. Never commit them, include them in an APK artifact, paste them into source, or rotate them for an existing application ID unless intentionally creating an incompatible app. The CI workflow places the decoded keystore in the runner's temporary directory and does not print it. Without all four signing secrets the workflow produces an unsigned debug APK labelled for testing only.

## Future security work

Before claiming a protected finance app, implement and test a real PIN verifier with a vetted password KDF, unique salt and calibrated parameters; optionally integrate Android Keystore-backed encryption and BiometricPrompt; handle lockouts/recovery and recent-app preview; and evaluate a maintained database-encryption solution if database-at-rest protection is required. Do not roll custom cryptography or claim Room encrypts its database.
