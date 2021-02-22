@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.helper

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * 使用[getProxy]-不可见fragment来链式调用[Activity.startActivityForResult]或者[Activity.requestPermissions]
 *
 * 但正式使用时不建议使用 permission 部分，因为此类没有做任何处理
 * 没有先做权限判断，也没有做权限分组和兼容判断
 *
 * Create by munch1182 on 2020/12/22 16:50.
 */
class ResultHelper constructor(private val fm: FragmentManager) {

    companion object {

        private const val TAG = "RESULT_HELPER_FRAGMENT_TAG"
        private const val REQUEST_CODE = 1

        fun with(activity: FragmentActivity) = ResultHelper(activity)

        fun with(fragment: Fragment) = ResultHelper(fragment)
    }

    constructor(activity: FragmentActivity) : this(activity.supportFragmentManager)

    constructor(fragment: Fragment) : this(fragment.childFragmentManager)

    fun startForResult(intent: Intent): Result {
        return Result(getProxy(), intent)
    }

    fun requestPermission(vararg permissions: String): Permission {
        return Permission(getProxy(), permissions)
    }

    private fun getProxy(): ProxyFragment {
        return (fm.findFragmentByTag(TAG) ?: ProxyFragment().apply {
            fm.beginTransaction().add(this, TAG).commitNowAllowingStateLoss()
        }) as ProxyFragment
    }

    class Result constructor(private val fragment: ProxyFragment, private val intent: Intent) {

        fun res(listener: (isOk: Boolean, resultCode: Int, data: Intent?) -> Unit) {
            fragment.listenerResult(listener).startActivityForResult(intent, REQUEST_CODE)
        }
    }

    class Permission(
        private val fragment: ProxyFragment,
        private val permissions: Array<out String>
    ) {

        fun res(listener: (allGrant: Boolean, permissions: Array<out String>, grantResults: IntArray) -> Unit) {
            fragment.listenerPermission(listener).requestPermissions(permissions, REQUEST_CODE)
        }
    }

    class ProxyFragment : Fragment() {

        private var resListener: ((Boolean, Int, Intent?) -> Unit?)? = null
        private var permissionListener: ((Boolean, Array<out String>, IntArray) -> Unit)? = null

        fun listenerResult(listener: (isOk: Boolean, resultCode: Int, data: Intent?) -> Unit): ProxyFragment {
            this.resListener = listener
            return this
        }

        fun listenerPermission(listener: (allGrant: Boolean, permissions: Array<out String>, grantResults: IntArray) -> Unit): ProxyFragment {
            this.permissionListener = listener
            return this
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            resListener?.invoke(resultCode == Activity.RESULT_OK, resultCode, data)
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            var allGrant = false
            kotlin.run out@{
                grantResults.forEach {
                    allGrant = it == PackageManager.PERMISSION_GRANTED
                    if (!allGrant) {
                        return@out
                    }
                }
            }
            permissionListener?.invoke(
                allGrant,
                permissions,
                grantResults
            )
        }
    }

}