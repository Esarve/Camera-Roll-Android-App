package us.koller.cameraroll.util

import android.app.Application
import com.google.android.material.color.DynamicColors

class CameraRollApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}