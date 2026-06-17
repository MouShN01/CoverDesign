package com.example.coverdesign

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform