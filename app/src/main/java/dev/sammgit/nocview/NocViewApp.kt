package dev.sammgit.nocview

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.sammgit.nocview.widget.WidgetRefreshWorker

@HiltAndroidApp
class NocViewApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WidgetRefreshWorker.enqueuePeriodic(this)
    }
}
