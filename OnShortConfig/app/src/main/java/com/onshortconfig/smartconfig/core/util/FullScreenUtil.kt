package com.onshortconfig.smartconfig.core.util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View

object FullScreenUtil {
    fun setFullScreen(activity: Activity?) {
        if (activity != null && activity.window != null) {
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    fun setScreenPortrait(activity: Activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        }
    }

}
