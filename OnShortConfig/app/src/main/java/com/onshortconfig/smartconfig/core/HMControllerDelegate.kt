package com.onshortconfig.smartconfig.core

import android.os.Bundle
import android.view.View

interface HMControllerDelegate {
    fun onContentViewCreated(parentView: View?, saveInstanceState: Bundle?)
}