package com.example

import android.app.Application
import com.example.data.local.AppSettingsDataStore
import com.example.data.repository.ExplainerRepository

class ExplainerApp : Application() {

    lateinit var appSettingsDataStore: AppSettingsDataStore
        private set

    lateinit var explainerRepository: ExplainerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        appSettingsDataStore = AppSettingsDataStore(this)
        explainerRepository = ExplainerRepository(this)
    }
}
