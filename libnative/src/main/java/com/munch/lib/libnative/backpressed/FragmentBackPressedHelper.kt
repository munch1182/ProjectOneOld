package com.munch.lib.libnative.backpressed

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * Created by Munch on 2019/7/15 9:31
 */
object FragmentBackPressedHelper {

    fun handleBackPressed(activity: FragmentActivity) =
        handleBackPressed(activity.supportFragmentManager)

    fun handleBackPressed(fragment: Fragment) =
        handleBackPressed(fragment.childFragmentManager)

    fun handleBackPressed(manager: FragmentManager): Boolean {
        val fragments = manager.fragments
        if (fragments.isEmpty()) {
            return false
        }
        fragments.forEach {
            if (isHandleBackPressedByFragment(it)) {
                return true
            }
        }
        return false
    }

    private fun isHandleBackPressedByFragment(it: Fragment): Boolean {
        return it is IFragmentBackPressedHandle
                && it.isVisible
                //viewpager
                && it.userVisibleHint
                && it.onFragmentBackPressed()
    }
}