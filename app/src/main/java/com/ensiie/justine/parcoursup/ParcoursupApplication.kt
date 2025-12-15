package com.ensiie.justine.parcoursup

import android.app.Application
import com.ensiie.justine.parcoursup.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class ParcoursupApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@ParcoursupApplication)
            modules(appModule)
        }
    }
}
