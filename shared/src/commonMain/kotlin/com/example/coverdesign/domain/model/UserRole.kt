package com.example.coverdesign.domain.model

enum class UserRole {
    PATIENT,    // browses catalogue, configures and orders
    TECHNICIAN,  // receives orders, updates status, manages production
    DOCTOR       // manages catalogue, views all orders, sets pricing
}