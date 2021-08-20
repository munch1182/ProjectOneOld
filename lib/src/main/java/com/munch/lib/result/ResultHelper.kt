package com.munch.lib.result

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.dialog.IDialogHandler

/**
 * Create by munch1182 on 2021/8/20 16:37.
 */
class ResultHelper(private val fm: FragmentManager) {

    companion object {

        fun init(activity: FragmentActivity) = ResultHelper(activity.supportFragmentManager)
        fun init(fragment: Fragment) = ResultHelper(fragment.childFragmentManager)
    }

    fun with(vararg permission: String) = Permission(ensureFragment(), permission)
    fun with(intent: Intent) = ActivityResult(ensureFragment(), intent)

    private fun ensureFragment(): InvisibleFragment {
        var fragment = fm.findFragmentByTag(InvisibleFragment.TAG) as? InvisibleFragment?
        if (fragment == null) {
            fragment = InvisibleFragment()
            fm.beginTransaction().add(fragment, InvisibleFragment.TAG).commitNowAllowingStateLoss()
        }
        return fragment
    }

    class Permission(
        private val fragment: InvisibleFragment,
        private val permissions: Array<out String>
    ) {

        fun onExplainRequestReason(
            deniedList: ArrayList<String>,
            dialog: IDialogHandler
        ): Permission {
            return this
        }

        fun explainBeforeRequest(): Permission {
            return this
        }

        fun request(callback: OnPermissionCallback) {
            fragment.requestPermissions(permissions, callback)
        }
    }

    class ActivityResult(private val fragment: InvisibleFragment, private val intent: Intent) {

        fun start(callback: OnResultCallback) {
            fragment.startActivityForResult(intent, callback)
        }
    }

    interface OnPermissionCallback {
        fun onResult(
            allGrant: Boolean,
            grantedList: ArrayList<String>,
            deniedList: ArrayList<String>
        )
    }

    interface OnResultCallback {
        fun onResult(isOk: Boolean, resultCode: Int, data: Intent?)
    }
}