package com.example.locationtodoappkotlin.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.locationtodoappkotlin.data.local.AppDatabase
import com.example.locationtodoappkotlin.data.remote.GeocodingApi
import com.example.locationtodoappkotlin.data.repository.TaskRepository
import com.example.locationtodoappkotlin.data.repository.TaskRepositoryImpl
import com.example.locationtodoappkotlin.presentation.alltasks.AllTasksViewModel
import com.example.locationtodoappkotlin.presentation.availabletasks.AvailableTasksViewModel
import com.example.locationtodoappkotlin.presentation.createtask.CreateTaskViewModel
import com.example.locationtodoappkotlin.presentation.edittask.EditTaskViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<AppDatabase> {
        get<androidx.room.RoomDatabase.Builder<AppDatabase>>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            .build()
    }

    single { get<AppDatabase>().taskDao() }

    single<TaskRepository> { TaskRepositoryImpl(get()) }

    single { GeocodingApi() }

    viewModel { AllTasksViewModel(get()) }
    viewModel { CreateTaskViewModel(get()) }
    viewModel { params -> EditTaskViewModel(get(), params.get()) }
    viewModel { AvailableTasksViewModel(get(), get(), get()) }
}
