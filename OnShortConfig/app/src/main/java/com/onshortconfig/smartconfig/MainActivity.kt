package com.onshortconfig.smartconfig

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.onshortconfig.smartconfig.core.HMBindingActivity
import com.onshortconfig.smartconfig.core.HMFragment
import com.onshortconfig.smartconfig.core.HMFragmentHelper
import com.onshortconfig.smartconfig.core.util.FullScreenUtil
import com.onshortconfig.smartconfig.core.util.PermissionUtil
import com.onshortconfig.smartconfig.databinding.ActivityMainBinding
import com.onshortconfig.smartconfig.ui.CusDataFragment
import com.onshortconfig.smartconfig.ui.ScanWifiListFragment


class MainActivity : HMBindingActivity<ActivityMainBinding>(R.layout.activity_main) {

    private var currentFragment: HMFragment? = null


    private val fragmentHelper: HMFragmentHelper by lazy {
        HMFragmentHelper(this.supportFragmentManager, R.id.containerView)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        FullScreenUtil.setScreenPortrait(this)
    }

    override fun onContentViewCreated(parentView: View?, saveInstanceState: Bundle?) {
        super.onContentViewCreated(parentView, saveInstanceState)
        PermissionUtil.initPermissions(this)
        currentFragment = ScanWifiListFragment()
        replaceFragment(currentFragment)
    }

    private fun replaceFragment(fragment: HMFragment?) {
        if (fragment == null) return
        fragmentHelper.pushFragment(
                HMFragment.create(fragment.javaClass),
                isCommitNow = true
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (currentFragment != null && currentFragment is CusDataFragment)
            (currentFragment as CusDataFragment).onKeyDown()
        return super.onKeyDown(keyCode, event)
    }

}