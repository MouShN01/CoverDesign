package com.example.coverdesign.ui.mapper

import com.example.coverdesign.domain.model.InsoleOrderConfig
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.ui.viewmodel.InsoleFootConfig
import com.example.coverdesign.ui.viewmodel.InsoleOrderUiState
import com.example.coverdesign.domain.model.InsoleFootConfig as DomainInsoleFootConfig
import com.example.coverdesign.domain.model.LegSide as DomainLegSide

fun InsoleOrderUiState.toInsoleOrderConfig(orderId: String): InsoleOrderConfig =
    InsoleOrderConfig(
        orderId = orderId,
        category = OrderCategory.INSOLE,
        quantity = quantity,
        priceEstimate = PriceEstimate(
            configId = orderId,
            quantity = quantity,
            basePrice = 0.0,
            subtotal = 0.0,
            total = 0.0
        ),
        rightFoot = primaryFoot.toDomainFoot(),
        leftFoot = secondFoot?.toDomainFoot(),
        material = material,
        color = color
    )

internal fun InsoleFootConfig.toDomainFoot(): DomainInsoleFootConfig =
    DomainInsoleFootConfig(
        euSize = euSize,
        lengthMm = lengthMm,
        widthMm = widthMm,
        side = DomainLegSide.valueOf(side.name)
    )
