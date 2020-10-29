package com.onshortconfig.smartconfig.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class HMBindingFragment<T : ViewDataBinding>(@LayoutRes private val layoutResId: Int) : HMFragment(),
    HMDataBindingSource<T> {

    override lateinit var dataBinding: T

    override fun getLayoutResId(savedInstanceState: Bundle?): Int? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this@HMBindingFragment.dataBinding =
            DataBindingUtil.inflate(inflater, this@HMBindingFragment.layoutResId, container, false)
        this@HMBindingFragment.dataBinding.lifecycleOwner = viewLifecycleOwner
        return this@HMBindingFragment.dataBinding.root
    }

}