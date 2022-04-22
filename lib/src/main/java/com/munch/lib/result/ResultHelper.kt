package com.munch.lib.result

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

/**
 * Created by munch1182 on 2022/4/10 3:34.
 */
class ResultHelper(private val fm: FragmentManager) {

    companion object {

        fun with(activity: FragmentActivity) = ResultHelper(activity.supportFragmentManager)

        fun with(fragment: Fragment) = ResultHelper(fragment.childFragmentManager)

        private const val FRAGMENT_TAG = "com.munch.lib.ResultFragment"
    }

    private val fragment: ResultFragment
        get() {
            var fragment = fm.findFragmentByTag(FRAGMENT_TAG) as? ResultFragment?
            if (fragment == null) {
                fragment = ResultFragment()
                fm.beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commitNowAllowingStateLoss()
            }
            return fragment
        }

    /**
     * 进行权限请求
     */
    fun permission(vararg permissions: String) = PermissionRequest(permissions, fragment)

    /**
     * 进行intent跳转，获取result
     */
    fun intent(intent: Intent) = IntentRequest(intent, fragment)

    /**
     * 先进行[judge]判断，如果判断为false，则跳转[intent]，并在intent返回后再次判断[judge]
     */
    fun judge(judge: OnJudge, intent: Intent) = JudgeIntentRequest(judge, intent, fragment)

    class PermissionRequest(
        private val permissions: Array<out String>,
        private val fragment: ResultFragment
    ) {

        fun request(listener: OnPermissionResultListener?) {
            fragment.requestPermissions(permissions, listener)
        }

    }

    class IntentRequest(private val intent: Intent, private val fragment: ResultFragment) {

        fun start(listener: OnIntentResultListener?) {
            fragment.startIntent(intent, listener)
        }
    }

    class JudgeIntentRequest(
        private val judge: OnJudge,
        private val intent: Intent,
        private val fragment: ResultFragment
    ) {

        fun result(listener: OnJudgeResultListener?) {
            fragment.judge2Result(judge, intent, listener)
        }
    }

}

interface OnJudge {

    fun onJudge(context: Context): Boolean
}

interface OnPermissionResultListener {

    fun onPermissionResult(
        isGrantAll: Boolean,
        grantedArray: Array<String>,
        deniedArray: Array<String>
    )
}

interface OnIntentResultListener {

    fun onIntentResult(isOk: Boolean, resultCode: Int, data: Intent?)
}

interface OnJudgeResultListener {

    fun onJudgeResult(result: Boolean)
}
