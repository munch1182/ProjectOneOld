package com.munch.lib.result

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.notice.Notice

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
    fun permission(vararg permissions: String) = PermissionRequest(arrayOf(*permissions), fragment)

    /**
     * 进行intent跳转，获取result
     */
    fun intent(intent: Intent) = IntentRequest(intent, fragment)

    /**
     * 先进行[onJudge]判断，如果结果为false，则调用[intent]，当有结果时，再判断一次[onJudge]并回调结果
     */
    fun judgeOrIntent(onJudge: OnJudge, intent: Intent) =
        JudgeOrIntentRequest(onJudge, intent, fragment)

    class PermissionRequest(
        private val permissions: Array<String>,
        private val fragment: ResultFragment
    ) {
        private var explain: Notice? = null

        fun request(listener: OnPermissionResultListener?) {
            fragment.requestPermissions(permissions, listener)
        }

    }

    class IntentRequest(private val intent: Intent, private val fragment: ResultFragment) {

        private var explain: Notice? = null

        fun start(listener: OnIntentResultListener?) {
            fragment.startIntent(intent, listener)
        }
    }

    class JudgeOrIntentRequest(
        private val judge: OnJudge,
        private val intent: Intent,
        private val fragment: ResultFragment
    ) {
        private var explain: Notice? = null

        fun start(listener: OnJudgeResultListener?) {
            //第一次判断
            if (judge.onJudge(fragment.requireContext())) {
                listener?.onJudgeResult(true)
                return
            }
            IntentRequest(intent, fragment).start { _, _ ->
                //第二次判断并回调
                listener?.onJudgeResult(judge.onJudge(fragment.requireContext()))
            }
        }
    }
}

interface OnJudge {

    fun onJudge(context: Context): Boolean
}

interface OnPermissionResultListener {

    fun onPermissionResult(isGrantAll: Boolean, result: Map<String, Boolean>)
}

interface OnIntentResultListener {

    fun onIntentResult(isOk: Boolean, data: Intent?)
}

interface OnJudgeResultListener {

    fun onJudgeResult(result: Boolean)
}
