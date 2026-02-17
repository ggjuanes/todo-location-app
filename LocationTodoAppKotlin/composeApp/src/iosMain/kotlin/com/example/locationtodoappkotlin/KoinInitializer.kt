package com.example.locationtodoappkotlin

import com.example.locationtodoappkotlin.di.appModule
import com.example.locationtodoappkotlin.di.platformModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(platformModule, appModule)
    }
}
