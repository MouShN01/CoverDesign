package com.example.coverdesign.domain.model

enum class UserRole {
    CUSTOMER,    // browses catalogue, configures and orders
    TECHNICIAN,  // receives orders, updates status, manages production
    SELLER       // manages catalogue, views all orders, sets pricing
}