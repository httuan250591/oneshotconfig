package com.onshortconfig.smartconfig.core

import androidx.databinding.ViewDataBinding

interface HMDataBindingSource<T : ViewDataBinding> {
    val dataBinding: T
}