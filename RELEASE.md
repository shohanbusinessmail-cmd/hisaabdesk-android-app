# Release and APK verification

## CI build

`.github/workflows/android-ci.yml` runs on pushes to `main`/`arena/**`, pull requests to `main`, and manual dispatch. It tests, lints, builds the debug APK, and uploads APK/reports as a workflow artifact.

## Tag build

Push a version tag such as `v0.1.0` to run `.github/workflows/android-release.yml`. The workflow reruns tests/lint, builds a release APK when signing secrets are configured, checks the package metadata and verifies APK signature, computes SHA-256, uploads the files, and publishes a GitHub Release. If signing secrets are absent/incomplete, it publishes a debug variant explicitly labelled `UNSIGNED-debug` for installation testing only. It must not be described as a production-signed release and will use the debug application ID suffix.

Configure the four secrets described in `README.md` before a production release. Keep the keystore and passwords backed up securely outside GitHub. A future update to the same installation requires the same package ID and signing key.

## Manual verification

After a run, inspect the Actions job conclusion and artifacts. For an APK downloaded from a release:

```sh
sha256sum -c HisaabDesk-vX.Y.Z-signed-release.apk.sha256
apkanalyzer manifest application-id HisaabDesk-vX.Y.Z-signed-release.apk
apksigner verify --verbose HisaabDesk-vX.Y.Z-signed-release.apk
adb install -r HisaabDesk-vX.Y.Z-signed-release.apk
```

Unsigned debug test builds use `com.shohankhan.hisaabdesk.debug` and cannot update a production-signed package. This repository has no release tag or published APK until a workflow is run and the resulting asset is verified. GitHub runner availability and usage quotas depend on account/repository policy.
