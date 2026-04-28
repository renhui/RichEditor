package com.example.richtext

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RichNoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }

    companion object {
        @JvmStatic
        lateinit var INSTANCE: RichNoteApplication
            private set

        @JvmStatic
        fun getInstance(): RichNoteApplication = INSTANCE
    }
}
