package com.example.coverdesign.domain.model

import com.example.coverdesign.domain.model.pricing.PriceEstimate

data class ProstheticCoverOrderConfig(
    override val orderId: String,
    override val category: OrderCategory = OrderCategory.COSMETIC_COVER,
    override val quantity: Int,
    override val priceEstimate: PriceEstimate,
    val limbSide: LegSide,
    val limbLevel: LimbLevel,
    val prostheticType: ProstheticTypeEnum?,
    val commonMeasurements: ProstheticCommonMeasurements = ProstheticCommonMeasurements(),
    // Type-specific measurements — semantics depend on prostheticType:
    //   PolycentricKnee / TotalKnee           → kneeUnitMm
    //   HighProfile / Blade / Microprocessor / HydraulicFoot → footshellToFootUnitMm
    //   ReleaseButton / ReleaseValve / PinlockButton         → footshellToButtonCenterMm
    //   Rotationplasty                         → toeToFootshellMm (primary), heelKneeToFootshellMm (secondary)
    val typeSpecificMeasurementMm: Float = 0f,
    val secondTypeSpecificMeasurementMm: Float = 0f,
    val buttonPlacement: ButtonPlacement? = null,
    val fasteningType: FasteningType = FasteningType.STRAP,
    val color: ProductColor,
    val material: Material,
    val texture: Texture,
    val inscription: String = "",
    val additionalElements: List<AdditionalElement> = emptyList(),
    val additionalNotes: String = "",
    val createdAt: Long
) : OrderConfig
