package com.example.locationtodoappkotlin.di

import com.example.locationtodoappkotlin.data.local.getDatabaseBuilder
import com.example.locationtodoappkotlin.platform.LocationService
import org.koin.dsl.module

val platformModule = module {
    single { getDatabaseBuilder(get()) }
    single { LocationService(get()) }
}
