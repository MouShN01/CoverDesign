package com.example.coverdesign.domain.model

data class AppUser(
    val uid         : String    = "",
    val email       : String    = "",
    val displayName : String    = "",   // full name — required for orders
    val phone       : String    = "",   // required for delivery contact
    val role        : UserRole  = UserRole.CUSTOMER,
    val createdAt   : Long      = 0L
)
