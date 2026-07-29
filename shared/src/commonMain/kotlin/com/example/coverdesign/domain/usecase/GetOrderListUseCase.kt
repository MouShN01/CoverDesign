package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.OrderListItem
import com.example.coverdesign.domain.repo.OrderRepository
import kotlinx.coroutines.flow.Flow

class GetOrderListUseCase(private val repo: OrderRepository) {
    operator fun invoke(): Flow<List<OrderListItem>> = repo.getOrderListItems()
}