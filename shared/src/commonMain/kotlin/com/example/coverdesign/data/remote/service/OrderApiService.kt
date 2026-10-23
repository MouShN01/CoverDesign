package com.example.coverdesign.data.remote.service

import com.example.coverdesign.data.dto.InsoleOrderConfigDto
import com.example.coverdesign.data.dto.OrderDto
import com.example.coverdesign.data.dto.OrderIdDto
import com.example.coverdesign.data.dto.ProstheticCoverOrderConfigDto
import com.example.coverdesign.data.dto.SubmitOrderRequestDto
import com.example.coverdesign.data.remote.client.ApiConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

// data/remote/service/OrderApiService.kt
class OrderApiService(private val client: HttpClient) {

    suspend fun submitOrder(request: SubmitOrderRequestDto): OrderIdDto =
        client.post(ApiConstants.Endpoints.SUBMIT_ORDER) {
            setBody(request)
        }.body()

    suspend fun getOrderById(id: String): OrderDto =
        client.get(
            ApiConstants.Endpoints.ORDER_BY_ID.replace("{id}", id)
        ).body()

    suspend fun getOrders(): List<OrderDto> =
        client.get(
            ApiConstants.Endpoints.ORDERS
        ).body()

//    suspend fun submitInsoleOrder(request: SubmitInsoleOrderRequestDto): OrderIdDto =
    suspend fun submitInsoleOrder(request: InsoleOrderConfigDto): OrderIdDto =
        client.post(ApiConstants.Endpoints.SUBMIT_INSOLE_ORDER) {
            setBody(request)
        }.body()

//    suspend fun submitProstheticCoverOrder(request: SubmitProstheticCoverOrderRequestDto): OrderIdDto =
    suspend fun submitProstheticCoverOrder(request: ProstheticCoverOrderConfigDto): OrderIdDto =
        client.post(ApiConstants.Endpoints.SUBMIT_COVER_ORDER) {
            setBody(request)
        }.body()
}