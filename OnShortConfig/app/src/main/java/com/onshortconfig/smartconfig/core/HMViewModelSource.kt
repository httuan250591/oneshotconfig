package com.onshortconfig.smartconfig.core

interface HMViewModelSource<T : HMViewModel> {
    val viewModel: T
}