package com.f1rq.lifemap

import android.app.Application
import com.f1rq.lifemap.di.databaseModule
import com.f1rq.lifemap.di.repositoryModule
import com.f1rq.lifemap.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LifeMapApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LifeMapApplication)
            modules(
                databaseModule,
                repositoryModule,
                viewModelModule
            )
        }
    }
}