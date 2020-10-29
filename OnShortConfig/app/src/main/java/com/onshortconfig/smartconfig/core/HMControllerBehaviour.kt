package com.onshortconfig.smartconfig.core

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar

interface HMControllerBehaviour {

    @LayoutRes
    fun getLayoutResId(savedInstanceState: Bundle?): Int?

    @MenuRes
    fun getMenuResId(): Int?

    @StringRes
    fun getTitleResId(): Int?

    fun getTitleString(): String?

    fun getToolbar(): Toolbar?
}