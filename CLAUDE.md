# CoverDesign — Project Instructions

## Theme Rules (enforced for every new screen)

All UI screens must source every style token from the theme files under
`androidApp/src/main/kotlin/com/example/coverdesign/ui/theme/`. No screen file
may contain raw style values.

### Colors — `Color.kt`
- Use only the named constants defined in `Color.kt` (e.g. `NeutralText`, `PatientActiveTeal`).
- Never write `Color(0xFF…)` or `Color.Red` directly in a screen.
- If a new color is required, add it as a named constant in `Color.kt` first.

### Typography — `Type.kt`
- Use `MaterialTheme.typography.*` slots (e.g. `MaterialTheme.typography.bodyMedium`).
- Never write `fontSize = X.sp` in a screen file.
- If a needed style doesn't map cleanly to an existing slot, add a new `TextStyle`
  entry to `AppTypography` in `Type.kt`.

### Shapes — `Shape.kt`
- Use `MaterialTheme.shapes.*` (e.g. `MaterialTheme.shapes.medium` for 12 dp corners).
- Never write `RoundedCornerShape(X.dp)` in a screen file.
- Available slots: `extraSmall` 4 dp · `small` 8 dp · `medium` 12 dp · `large` 16 dp · `extraLarge` 20 dp.
- Use `CircleShape` only for perfectly round elements (avatars, dot indicators).

### Spacing & sizing
- Spacing and `size(X.dp)` values may stay inline — they are layout, not style.

## Screen structure conventions
- Entry-point composable: `FooScreen(…)` — wired to a ViewModel or nav arguments.
- Internal content composable: `FooContent(…)` — pure, testable, no ViewModel dependency.
- Private sub-composables at the bottom of the file.
- Always include an `@Preview` wrapping `AppTheme { … }`.

## Navigation
- Add every new route as an `object` in `Screen.kt`.
- Wire screens into the appropriate nav graph (`ClinicianGraph.kt`, `PatientGraph.kt`, etc.).