package com.mr_brick.gps_tracker

import android.app.Application
import com.mr_brick.gps_tracker.db.MainDb

class MainApp : Application() {
    val database by lazy { MainDb.getDatabase(this) }
}