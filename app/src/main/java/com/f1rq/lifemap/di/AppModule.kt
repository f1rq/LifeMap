package com.f1rq.lifemap.di

import android.app.Application
import androidx.room.Room
import com.f1rq.lifemap.data.database.AppDatabase
import com.f1rq.lifemap.data.repository.EventRepository
import com.f1rq.lifemap.ui.viewmodel.EventViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "lifemap_database"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<AppDatabase>().eventDao() }
}

val repositoryModule = module {
    single { EventRepository(get()) }
}

val viewModelModule = module {
    viewModel { EventViewModel(androidContext() as Application, get()) }}