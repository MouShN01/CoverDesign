package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

data class MeasurementSpec(
    val instructions: List<String>,
    val imageUrl: String? = null
)

@Serializable
enum class ButtonPlacement { MEDIAL, LATERAL, POSTERIOR }

sealed class ProstheticType {
    abstract val displayName: String
    abstract val measurementSpec: MeasurementSpec

    // ── Above-knee ────────────────────────────────────────────────────────────

    sealed class AboveKnee : ProstheticType() {

        data class PolycentricKnee(
            val kneeUnitMm: Float = 0f
        ) : AboveKnee() {
            override val displayName = "Polycentric Knee"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Add knee unit measurements as shown in the diagram."),
                imageUrl = null
            )
        }

        data class TotalKnee(
            val kneeUnitMm: Float = 0f
        ) : AboveKnee() {
            override val displayName = "Total Knee"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf(
                    "We will use the knee unit measurement to move the top strap below the knee " +
                    "mechanism so that there is no interference with the functionality of the knee."
                ),
                imageUrl = null
            )
        }

        object ThroughKnee : AboveKnee() {
            override val displayName = "Through Knee"
            override val measurementSpec = MeasurementSpec(instructions = emptyList())
        }

        data class HighProfile(
            val footshellToFootUnitMm: Float = 0f
        ) : AboveKnee() {
            override val displayName = "High Profile"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Measure from Top of Footshell to Top of Foot Unit.")
            )
        }

        data class Blade(
            val footshellToFootUnitMm: Float = 0f
        ) : AboveKnee() {
            override val displayName = "Blade"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Measure from Top of Footshell to Top of Foot Unit.")
            )
        }

        data class Microprocessor(
            val footshellToFootUnitMm: Float = 0f
        ) : AboveKnee() {
            override val displayName = "Microprocessor"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Measure from Top of Footshell to Top of Foot Unit.")
            )
        }

        data class HydraulicFoot(
            val footshellToFootUnitMm: Float = 0f
        ) : AboveKnee() {
            override val displayName = "Hydraulic Foot"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Measure from Top of Footshell to Top of Foot Unit.")
            )
        }
    }

    // ── Below-knee ────────────────────────────────────────────────────────────

    sealed class BelowKnee : ProstheticType() {

        data class ReleaseButton(
            val footshellToButtonCenterMm: Float = 0f,
            val buttonPlacement: ButtonPlacement = ButtonPlacement.MEDIAL
        ) : BelowKnee() {
            override val displayName = "Release Button"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf(
                    "Measure from top of the footshell to center of release button.",
                    "Specify button placement: Medial, Lateral, or Posterior."
                )
            )
        }

        data class ReleaseValve(
            val footshellToButtonCenterMm: Float = 0f,
            val buttonPlacement: ButtonPlacement = ButtonPlacement.MEDIAL
        ) : BelowKnee() {
            override val displayName = "Release Valve"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf(
                    "Measure from top of the footshell to center of release button.",
                    "Specify button placement: Medial, Lateral, or Posterior."
                )
            )
        }

        data class PinlockButton(
            val footshellToButtonCenterMm: Float = 0f,
            val buttonPlacement: ButtonPlacement = ButtonPlacement.MEDIAL
        ) : BelowKnee() {
            override val displayName = "Pinlock Button"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf(
                    "Measure from top of the footshell to center of release button.",
                    "Specify button placement: Medial, Lateral, or Posterior."
                )
            )
        }

        data class HighProfile(
            val footshellToFootUnitMm: Float = 0f
        ) : BelowKnee() {
            override val displayName = "High Profile"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Measure from Top of Footshell to Top of Foot Unit.")
            )
        }

        data class Blade(
            val footshellToFootUnitMm: Float = 0f
        ) : BelowKnee() {
            override val displayName = "Blade"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Measure from Top of Footshell to Top of Foot Unit.")
            )
        }

        data class Microprocessor(
            val footshellToFootUnitMm: Float = 0f
        ) : BelowKnee() {
            override val displayName = "Microprocessor"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Measure from Top of Footshell to Top of Foot Unit.")
            )
        }

        data class HydraulicFoot(
            val footshellToFootUnitMm: Float = 0f
        ) : BelowKnee() {
            override val displayName = "Hydraulic Foot"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf("Measure from Top of Footshell to Top of Foot Unit.")
            )
        }

        data class Rotationplasty(
            val toeToFootshellMm: Float = 0f,
            val heelKneeToFootshellMm: Float = 0f
        ) : BelowKnee() {
            override val displayName = "Rotationplasty Prosthesis"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf(
                    "Measure from tip of Pointed Toe to top of footshell.",
                    "Measure from just below the 'Heel/Knee' of the socket to the top of the footshell."
                )
            )
        }

        object SymesSocket : BelowKnee() {
            override val displayName = "Symes Socket"
            override val measurementSpec = MeasurementSpec(
                instructions = listOf(
                    "Part of the prosthetic cover will be removed to accommodate the Symes socket fit."
                )
            )
        }
    }
}