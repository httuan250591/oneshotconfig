package com.onshortconfig.smartconfig.core

import android.os.Build
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.transition.Fade
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import timber.log.Timber

class HMFragmentHelper(private val fragmentManager: FragmentManager, @IdRes private val containerViewId: Int) {

    fun <T : HMFragment> initRootFragment(fragment: T, allowStateLoss: Boolean = false) {

        if (fragmentManager.findFragmentById(containerViewId) != null) {
            return
        }

        val fragmentTransaction = getFragmentTransaction(false)
            .replace(containerViewId, fragment, fragment::class.java.simpleName)

        if (!fragmentManager.isStateSaved) {
            fragmentTransaction.commitNow()
        } else if (allowStateLoss){
            fragmentTransaction.commitAllowingStateLoss()
        }
    }

    fun <T : HMFragment> pushFragment(
        fragment: T,
        replaceRootFragment: Boolean = false,
        addToBackStack: Boolean = false,
        isCommitNow: Boolean = false) {
        if (fragmentManager.findFragmentById(containerViewId)?.tag.equals(fragment::class.java.simpleName)) {
            Timber.i("fragment ${fragment.javaClass.name}  : commit false")
            return
        }

        if (replaceRootFragment) {
            popToRoot()
        }

        val fragmentTransaction = getFragmentTransaction(!replaceRootFragment)
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment::class.java.simpleName)
        }

        fragmentTransaction.replace(this.containerViewId, fragment, fragment::class.java.simpleName)

        when {
            fragmentManager.isStateSaved -> when {
                isCommitNow -> fragmentTransaction.commitNowAllowingStateLoss()
                else -> fragmentTransaction.commitAllowingStateLoss()
            }
            isCommitNow -> fragmentTransaction.commitNow()
            else -> fragmentTransaction.commit()
        }

    }

    fun <T : HMFragment> pushFragment(fragment: T, sharedElementView: View, replaceRootFragment: Boolean = false) {

        var previousFragment =
            fragmentManager.findFragmentById(containerViewId)

        if (previousFragment?.tag.equals(fragment::class.java.simpleName)) {
            return
        }

        if (replaceRootFragment) {
            popToRoot()
        }

        var exitFade = Fade()
        exitFade.duration = 300
        previousFragment!!.exitTransition = exitFade


        val enterTransitionSet = TransitionSet()
        enterTransitionSet.addTransition(TransitionInflater.from(sharedElementView.context).inflateTransition(android.R.transition.move))
        enterTransitionSet.duration = 300
        enterTransitionSet.startDelay = 1000
        fragment.sharedElementEnterTransition = enterTransitionSet

        val enterFade = Fade()
        enterFade.startDelay = 1300
        enterFade.duration = 300
        fragment.enterTransition = enterFade

        val fragmentTransaction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getFragmentTransaction(!replaceRootFragment)
                .addSharedElement(sharedElementView, sharedElementView.transitionName)
                .replace(
                    this.containerViewId,
                    fragment,
                    fragment::class.java.simpleName
                )
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

    fun popFragment(): Boolean {
        if (fragmentManager.backStackEntryCount > 0) {
            return fragmentManager.popBackStackImmediate()
        }

        return false
    }

    fun <T : HMFragment> isShowing(fragmentClass: Class<T>): Boolean {
        return fragmentManager.findFragmentById(containerViewId)
            ?.tag.equals(fragmentClass.simpleName)
    }

    fun <T : HMFragment> getCurrentFragment(): T? {
        return fragmentManager.findFragmentById(containerViewId) as? T
    }

    private fun getFragmentTransaction(hasAnimation: Boolean = true): FragmentTransaction {
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (hasAnimation) {
            // todo custom animation when change fragment
        }
        return fragmentTransaction
    }

    fun popToRoot() {
        val backStackCount = fragmentManager.backStackEntryCount
        for (i in 0 until backStackCount) {
            val backStackId = fragmentManager.getBackStackEntryAt(i).id
            fragmentManager.popBackStack(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }
}