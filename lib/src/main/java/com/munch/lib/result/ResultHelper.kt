package com.munch.lib.result

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.base.OnCancelListener
import com.munch.lib.base.OnNextListener
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

        private var onExplainRequestReasonListener:
                ((permissions: ArrayList<String>, context: Context) -> IDialogHandler)? = null
        private var resultListener: OnPermissionResultListener? = null
        private var resultCallback = object : OnPermissionResultListener {
            override fun onResult(
                allGrant: Boolean,
                grantedList: ArrayList<String>,
                deniedList: ArrayList<String>
            ) {
                if (onExplainRequestReasonListener != null) {
                    onExplainRequestReasonListener!!.invoke(deniedList, fragment.requireContext())
                        .apply {
                            setOnNext(object : OnNextListener {
                                override fun onNext() {

                                }
                            })
                            setOnCancel(object : OnCancelListener {
                                override fun onCancel() {
                                    resultListener?.onResult(allGrant, grantedList, deniedList)
                                }
                            })
                        }
                        .show()
                } else {
                    resultListener?.onResult(allGrant, grantedList, deniedList)
                }
            }
        }

        fun onExplainRequestReason(
            explain: (permissions: ArrayList<String>, context: Context) -> IDialogHandler
        ): Permission {
            onExplainRequestReasonListener = explain
            return this
        }

        fun explainBeforeRequest(): Permission {
            return this
        }

        fun request(listener: OnPermissionResultListener) {
            resultListener = listener
            fragment.requestPermissions(permissions, resultCallback)
        }
    }

    class ActivityResult(private val fragment: InvisibleFragment, private val intent: Intent) {

        fun start(listener: OnResultListener) {
            fragment.startActivityForResult(intent, listener)
        }
    }

    interface OnPermissionResultListener {
        fun onResult(
            allGrant: Boolean,
            grantedList: ArrayList<String>,
            deniedList: ArrayList<String>
        )
    }

    interface OnResultListener {
        fun onResult(isOk: Boolean, resultCode: Int, data: Intent?)
    }
}