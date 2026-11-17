# CoverDesign — TODO / Placeholder Backlog

Ordered by urgency. **P0** = live `TODO()` calls that throw `NotImplementedError` at runtime.
**P1** = core README features that are silently broken. **P2** = security. **P3** = UX correctness. **P4** = platform stubs. **P5** = code quality.

---

## P0 — Runtime Crashes (throws `NotImplementedError` when triggered)

| # | Location | Issue                                                                                                  |
|---|----------|--------------------------------------------------------------------------------------------------------|
| 1 | `ClinicianGraph.kt:27` | `onNotificationsClick` calls bare `TODO()` — tapping the notification bell crashes the app immediately |
| 2 | `ClinicianGraph.kt:39` | Patients list composable is `TODO()` — navigating to the Patients tab crashes                          |
| 3 | `ClinicianGraph.kt:43` | Reports composable is `TODO()` — navigating to Reports tab crashes                                     |
| 4 | `ClinicianGraph.kt:47` | Settings composable is `TODO()` — navigating to Settings tab crashes                                   |
| 5 | `GoogleAuthenticator.jvm.kt:5` | `authenticate()` is `TODO()` — desktop login crashes on auth attempt                                   |
| 6 | `GoogleAuthenticator.ios.kt:5` | `authenticate()` is `TODO()` — iOS login crashes on auth attempt                                       |
| 7 | `ArCapability.jvm.kt:5` | `isSupported()` is `TODO()` — any desktop code path that checks AR support crashes                     |
| 8 | `DatabaseDriverFactory.jvm.kt:7` | `create()` is `TODO()` — desktop app crashes on first SQLDelight access                                |
| 9 | `PriceEstimate.kt` | TODO() change the signature                                                                            |

---

## P1 — Core Features Described in README That Don't Work

| # | Location | Issue |
|---|----------|-------|
| 9 | `ArSessionRepositoryImpl.android.kt` (entire file) | Whole Android AR session implementation is commented out with multiple `TODO()` stubs. The **AR Virtual Try-On** feature (ARCore camera overlay) listed in README is non-functional. Methods `startSession`, `stopSession`, `updateOverlayConfig`, `captureSnapshot`, `observeTrackingState`, `observeSessionState` all need real implementations. |
| 10 | `ClinicianHomeViewModel.kt:52` | `// TODO: fetch patient list from backend` — `recentPatients` / `allPatients` are never populated; clinician home screen always shows an empty patient list. |
| 11 | `ClinicianGraph.kt:54` | Patient data in `WriteRecommendationScreen` is hardcoded (`"Maria L."`, `"Post-operative Rehab"`, etc.) instead of being fetched by `patientId` from a ViewModel. Any patientId navigated to shows the same dummy data. |
| 12 | `ProductConfigViewModel.kt:377` | `basePrice ?: 1.0` — prices are never loaded from the server; the **dynamic pricing engine** (README: "base + material + texture + quantity") always uses a placeholder base of `1.0`. |
| 13 | `ProductDto.kt:7` | `// TODO mirror backend JSON` — the DTO field names and structure have not been verified against the real backend contract; network deserialization may silently fail or produce wrong data. |
| 14 | `ArSessionRepositoryImpl.jvm.kt` (all 5 methods) | JVM/desktop `ArSessionRepository` methods are all `// TODO("Not yet implemented")` stubs (commented out). Blocks desktop AR path entirely. |
| 15 | `ArSessionRepositoryImpl.ios.kt` (all 5 methods) | iOS `ArSessionRepository` methods are all `// TODO("Not yet implemented")` stubs. Blocks iOS AR path. |

---

## P2 — Security

| # | Location | Issue |
|---|----------|-------|
| 16 | `TokenStorage.kt:6` / `AppModule.kt:74` | `TokenStorage` is explicitly marked a `// placeholder — will be replaced with EncryptedStorage in security step`. Auth tokens are not yet encrypted at rest. README lists **encrypted token storage** as a stable/v1 feature — the encrypted implementation must be wired into the DI module. |
| 17 | `FirebaseStorageRepositoryImpl.kt:16` | `// TODO change pass for textures` — the Firebase Storage path `textures/$userId/$configId.jpg` may not match the production bucket layout; uploading textures could write to the wrong location or fail ACL checks. |

---

## P3 — UX Correctness

| # | Location | Issue |
|---|----------|-------|
| 18 | `RegisterScreen.kt:185` | Error supporting text reads `"Email is invalid"` but the variable triggering it is `passwordMismatch` — the wrong validation message is shown to the user. |
| 19 | `OrderSummaryScreen.kt:182` | `// TODO if error disable the continue button change to retry` — when an error is displayed the "Continue" button remains enabled, allowing the user to attempt submission on a broken state. |
| 20 | `ModelFailedIndicator.kt:60` | `// TODO add go home` — model generation failure screen has only a "Retry" button with no way to navigate back to Home; user is stuck if retries keep failing. |
| 21 | `ProductConfigScreen.kt:209` | `// TODO adapt for insoles and prosthetics` — the proceed dialog is not customised per order category; insole and prosthetic flows show a generic dialog that may present irrelevant options. |
| 22 | `MeasurementValidator.kt:200` | `validateProsthesisCover` contains only generic bounds checks (negative / > 10 000 mm) with no prosthesis-specific rules (e.g. residual limb ratio, circumference vs length constraints). |

---

## P4 — Architecture / Code Quality

| # | Location | Issue |
|---|----------|-------|
| 23 | `ProductConfigViewModel.kt:257` | `productRepo.getConfigById()` called directly from ViewModel — `// TODO make it clean architecture use UseCase`. Violates the clean-arch boundary already in place for other flows. |
| 24 | `ProductConfigViewModel.kt:375` | `// TODO think orderId` — `orderId` is a random UUID with no server-side coordination; if orders are later persisted server-side, ID collisions or mismatches are possible. |
| 25 | `Theme.kt:94` | `// TODO enableEdgeToEdge() in MainActivity` — edge-to-edge rendering is not enabled; status bar color workaround is in place but the proper API call is missing from `MainActivity`. |
| 26 | `ProductRepositoryImpl.kt:104` | `saveConfig` return type explicitly marked `// TODO remove Unit` — the function should return a `Resource<Unit>` (or similar) to propagate save errors to callers. |
| 27 | `Model3DRepositoryImplTest.kt:15` | `// TODO !!!! HEERREE ???!!! generateAdaptedModel` — the `generateAdaptedModel` test path is missing; 3D model generation has no test coverage for the adapted-model flow. |
