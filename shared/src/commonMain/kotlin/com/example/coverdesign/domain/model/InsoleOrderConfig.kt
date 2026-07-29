package com.example.coverdesign.domain.model

import com.example.coverdesign.domain.model.pricing.PriceEstimate

data class InsoleOrderConfig(
    override val orderId: String,
    override val category: OrderCategory = OrderCategory.INSOLE,
    override val quantity: Int = 1,
    override val priceEstimate: PriceEstimate,
    val rightFoot: InsoleFootConfig = InsoleFootConfig(side = LegSide.RIGHT),
    val leftFoot: InsoleFootConfig? = null,
    val material: Material = Material.SILICONE,
    val color: ProductColor = ProductColor("Black", "#000000")
) : OrderConfig