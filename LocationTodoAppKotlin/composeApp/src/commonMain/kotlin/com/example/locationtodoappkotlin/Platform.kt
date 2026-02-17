package com.example.locationtodoappkotlin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform