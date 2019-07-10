package com.munch.lib.result.compat

import android.content.Intent
import android.support.annotation.NonNull
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager

/**
 * Created by Munch on 2019/7/9 10:46
 */
object ResultHelperCompat {

    private const val PROXY_FRAGMENT_TAG = "com.munch.lib.loop.ResultHelperCompat.PROXY_FRAGMENT_TAG"

    @JvmStatic
    fun start4Result(context: FragmentActivity, target: Class<*>, requestCode: Int) =
        start4Result(context, Intent(context, target), requestCode)

    @JvmStatic
    fun start4Result(context: FragmentActivity, intent: Intent, requestCode: Int) =
        start4Result(context.supportFragmentManager, intent, requestCode)

    @JvmStatic
    fun start4Result(context: Fragment, intent: Intent, requestCode: Int) =
        start4Result(context.childFragmentManager, intent, requestCode)

    @JvmStatic
    fun start4Result(context: Fragment, target: Class<*>, requestCode: Int) =
        start4Result(context, Intent(context.context, target), requestCode)

    @JvmStatic
    fun requestPermission(context: FragmentActivity, requestCode: Int, @NonNull vararg permissions: String) =
        requestPermission(context.supportFragmentManager, requestCode, permissions)

    @JvmStatic
    fun requestPermission(context: Fragment, requestCode: Int, @NonNull vararg permissions: String) =
        requestPermission(context.childFragmentManager, requestCode, permissions)

    private fun requestPermission(
        manager: FragmentManager,
        requestCode: Int, @NonNull permissions: Array<out String>
    ) = getFragment(manager).requestPermissions(requestCode, permissions)

    private fun start4Result(manager: FragmentManager, intent: Intent, requestCode: Int): Result =
        getFragment(manager).start4Result(intent, requestCode)

    private fun getFragment(manager: FragmentManager): ProxyFragment {
        return (manager.findFragmentByTag(PROXY_FRAGMENT_TAG)
            ?: ProxyFragment().apply {
                manager.beginTransaction()
                    .add(this, PROXY_FRAGMENT_TAG)
                    .commitNowAllowingStateLoss()
            }) as? ProxyFragment ?: throw RuntimeException("fragment tag($PROXY_FRAGMENT_TAG) had be used")
    }
}