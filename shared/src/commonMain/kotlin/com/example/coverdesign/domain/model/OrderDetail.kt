package com.example.coverdesign.domain.model

sealed class OrderDetail {
    abstract val status: OrderStatus

    data class CatalogOrder(val order: Order) : OrderDetail() {
        override val status: OrderStatus get() = order.status
    }

    data class InsoleOrder(
        val config: InsoleOrderConfig,
        override val status: OrderStatus,
    ) : OrderDetail()

    // adaptedModels is a list because one prosthetic cover config can have
    // multiple 3D model iterations (colour variants, revisions, bilateral cases).
    // AdaptedModel3DEntity.configId is already a many-to-one FK so no schema change needed;
    // the repo populates this by querying all adapted models for config.orderId.
    data class ProstheticCoverOrder(
        val config: ProstheticCoverOrderConfig,
        override val status: OrderStatus,
        val adaptedModels: List<AdaptedModel3D> = emptyList(),
    ) : OrderDetail()
}