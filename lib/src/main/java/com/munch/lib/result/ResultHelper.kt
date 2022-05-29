package com.munch.lib.result

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.log.Logger

/**
 * Created by munch1182 on 2022/4/10 3:34.
 */
class ResultHelper(private val fm: FragmentManager) {

    companion object {

        fun with(activity: FragmentActivity) = ResultHelper(activity.supportFragmentManager)

        fun with(fragment: Fragment) = ResultHelper(fragment.childFragmentManager)

        private const val FRAGMENT_TAG = "com.munch.lib.ResultFragment"
    }

    private val log: Logger = Logger("Result")

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
     * 先进行[onJudge]判断，如果结果为false，则调用[intent]，当有结果时，再判断一次[onJudge]并回调结果
     */
    fun judgeOrIntent(onJudge: OnJudge, intent: OnIntent) =
        JudgeOrIntentRequest(onJudge, intent.invoke(fragment.requireContext()), fragment)

    fun contact(vararg permissions: String) = ContactRequest(fragment).contact(*permissions)
    fun contact(intent: Intent) = ContactRequest(fragment).contact(intent)
    fun contact(judge: OnJudge, intent: OnIntent) = ContactRequest(fragment).contact(judge, intent)
    fun contact() = ContactRequest(fragment)

    /**
     * 用以标记request
     */
    interface IRequest
}