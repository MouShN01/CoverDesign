package com.example.coverdesign.domain.model

data class AppUser(
    val uid         : String    = "",
    val email       : String    = "",
    val displayName : String    = "",
    val phone       : String    = "",
    val role        : UserRole  = UserRole.PATIENT,
    val createdAt   : Long      = 0L,
    val hasInsoles  : Boolean   = false
)
