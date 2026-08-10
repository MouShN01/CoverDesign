package com.example.coverdesign.ui.mapper

import com.example.coverdesign.domain.model.ButtonPlacement
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.ProstheticType
import com.example.coverdesign.data.dto.ProstheticTypeEnumDto
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.util.currentEpochMillis
import com.example.coverdesign.domain.util.generateUuid
import com.example.coverdesign.ui.viewmodel.ProstheticOrderUiState
import com.example.coverdesign.domain.model.LegSide as DomainLegSide
import com.example.coverdesign.domain.model.LimbLevel as DomainLimbLevel
import com.example.coverdesign.domain.model.ProstheticCoverOrderConfig as DomainProstheticCoverOrderConfig

internal fun ProstheticOrderUiState.toDomainConfig(): DomainProstheticCoverOrderConfig {
    val orderId = generateUuid()

    return DomainProstheticCoverOrderConfig(
        orderId = orderId,
        category = OrderCategory.PROSTHETIC_COVER,
        quantity = quantity,
        priceEstimate = PriceEstimate(
            configId = orderId, basePrice = 0.0, subtotal = 0.0, total = 0.0
        ),
        limbSide = DomainLegSide.valueOf(limbSide.name),
        limbLevel = limbLevel?.let { DomainLimbLevel.valueOf(it.name) }
            ?: DomainLimbLevel.BELOW_KNEE,
        prostheticType = prostheticType?.toEnum(),
        commonMeasurements = commonMeasurements,
        typeSpecificMeasurementMm = prostheticType?.typeSpecificMeasurementMm() ?: 0f,
        secondTypeSpecificMeasurementMm = prostheticType?.secondTypeSpecificMeasurementMm() ?: 0f,
        buttonPlacement = prostheticType?.buttonPlacementOrNull(),
        suspensionType = suspensionType,
        color = color,
        material = material,
        texture = texture,
        additionalNotes = additionalNotes,
        createdAt = currentEpochMillis())
}

internal fun ProstheticType.toEnum(): ProstheticTypeEnumDto = when (this) {
    is ProstheticType.AboveKnee.PolycentricKnee -> ProstheticTypeEnumDto.ABOVE_KNEE_POLYCENTRIC_KNEE
    is ProstheticType.AboveKnee.TotalKnee -> ProstheticTypeEnumDto.ABOVE_KNEE_TOTAL_KNEE
    ProstheticType.AboveKnee.ThroughKnee -> ProstheticTypeEnumDto.ABOVE_KNEE_THROUGH_KNEE
    is ProstheticType.AboveKnee.HighProfile -> ProstheticTypeEnumDto.ABOVE_KNEE_HIGH_PROFILE
    is ProstheticType.AboveKnee.Blade -> ProstheticTypeEnumDto.ABOVE_KNEE_BLADE
    is ProstheticType.AboveKnee.Microprocessor -> ProstheticTypeEnumDto.ABOVE_KNEE_MICROPROCESSOR
    is ProstheticType.AboveKnee.HydraulicFoot -> ProstheticTypeEnumDto.ABOVE_KNEE_HYDRAULIC_FOOT
    is ProstheticType.BelowKnee.ReleaseButton -> ProstheticTypeEnumDto.BELOW_KNEE_RELEASE_BUTTON
    is ProstheticType.BelowKnee.ReleaseValve -> ProstheticTypeEnumDto.BELOW_KNEE_RELEASE_VALVE
    is ProstheticType.BelowKnee.PinlockButton -> ProstheticTypeEnumDto.BELOW_KNEE_PINLOCK_BUTTON
    is ProstheticType.BelowKnee.HighProfile -> ProstheticTypeEnumDto.BELOW_KNEE_HIGH_PROFILE
    is ProstheticType.BelowKnee.Blade -> ProstheticTypeEnumDto.BELOW_KNEE_BLADE
    is ProstheticType.BelowKnee.Microprocessor -> ProstheticTypeEnumDto.BELOW_KNEE_MICROPROCESSOR
    is ProstheticType.BelowKnee.HydraulicFoot -> ProstheticTypeEnumDto.BELOW_KNEE_HYDRAULIC_FOOT
    is ProstheticType.BelowKnee.Rotationplasty -> ProstheticTypeEnumDto.BELOW_KNEE_ROTATIONPLASTY
    ProstheticType.BelowKnee.SymesSocket -> ProstheticTypeEnumDto.BELOW_KNEE_SYMES_SOCKET
}

internal fun ProstheticType.typeSpecificMeasurementMm(): Float = when (this) {
    is ProstheticType.AboveKnee.PolycentricKnee -> kneeUnitMm
    is ProstheticType.AboveKnee.TotalKnee -> kneeUnitMm
    is ProstheticType.AboveKnee.HighProfile -> footshellToFootUnitMm
    is ProstheticType.AboveKnee.Blade -> footshellToFootUnitMm
    is ProstheticType.AboveKnee.Microprocessor -> footshellToFootUnitMm
    is ProstheticType.AboveKnee.HydraulicFoot -> footshellToFootUnitMm
    is ProstheticType.BelowKnee.ReleaseButton -> footshellToButtonCenterMm
    is ProstheticType.BelowKnee.ReleaseValve -> footshellToButtonCenterMm
    is ProstheticType.BelowKnee.PinlockButton -> footshellToButtonCenterMm
    is ProstheticType.BelowKnee.HighProfile -> footshellToFootUnitMm
    is ProstheticType.BelowKnee.Blade -> footshellToFootUnitMm
    is ProstheticType.BelowKnee.Microprocessor -> footshellToFootUnitMm
    is ProstheticType.BelowKnee.HydraulicFoot -> footshellToFootUnitMm
    is ProstheticType.BelowKnee.Rotationplasty -> toeToFootshellMm
    else -> 0f
}

internal fun ProstheticType.secondTypeSpecificMeasurementMm(): Float = when (this) {
    is ProstheticType.BelowKnee.Rotationplasty -> heelKneeToFootshellMm
    else -> 0f
}

internal fun ProstheticType.buttonPlacementOrNull(): ButtonPlacement? = when (this) {
    is ProstheticType.BelowKnee.ReleaseButton -> buttonPlacement
    is ProstheticType.BelowKnee.ReleaseValve -> buttonPlacement
    is ProstheticType.BelowKnee.PinlockButton -> buttonPlacement
    else -> null
}
