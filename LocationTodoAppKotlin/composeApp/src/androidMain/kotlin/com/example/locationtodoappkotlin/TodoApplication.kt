package com.example.locationtodoappkotlin

import android.app.Application
import com.example.locationtodoappkotlin.di.appModule
import com.example.locationtodoappkotlin.di.platformModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TodoApplication)
            modules(platformModule, appModule)
        }
    }
}
