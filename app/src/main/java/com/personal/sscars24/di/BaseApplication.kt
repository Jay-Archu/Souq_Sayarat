package com.personal.sscars24.di

import android.app.Application
import com.personal.sscars24.utils.DialCodeCache
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        DialCodeCache.clear()
    }
}