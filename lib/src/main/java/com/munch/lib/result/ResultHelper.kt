package com.munch.lib.result

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.log.Logger
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
    fun permission(vararg permissions: String) =
        PermissionRequest(arrayOf(*permissions), fragment, log)

    /**
     * 进行intent跳转，获取result
     */
    fun intent(intent: Intent) = IntentRequest(intent, fragment)

    /**
     * 先进行[onJudge]判断，如果结果为false，则调用[intent]，当有结果时，再判断一次[onJudge]并回调结果
     */
    fun judgeOrIntent(onJudge: OnJudge, intent: Intent) =
        JudgeOrIntentRequest(onJudge, intent, fragment, log)

    class PermissionRequest(
        private val permissions: Array<String>,
        private val fragment: ResultFragment,
        private val log: Logger = Logger("JudgeIntent")
    ) {
        private var explain: ExplainPermissionNotice? = null

        fun explain(explain: (Context) -> ExplainPermissionNotice): PermissionRequest {
            this.explain = explain.invoke(fragment.requireContext())
            return this
        }

        fun request(listener: OnPermissionResultListener?) {
            fragment.requestPermissions(permissions, listener)
        }

    }

    class IntentRequest(private val intent: Intent, private val fragment: ResultFragment) {

        private var explain: ExplainIntentNotice? = null

        fun explain(explain: (Context) -> ExplainIntentNotice): IntentRequest {
            this.explain = explain.invoke(fragment.requireContext())
            return this
        }

        fun start(listener: OnIntentResultListener?) {
            fragment.startIntent(intent, listener)
        }
    }

    class JudgeOrIntentRequest(
        private val judge: OnJudge,
        private val intent: Intent,
        private val fragment: ResultFragment,
        private val log: Logger = Logger("JudgeIntent")
    ) {

        private var explain: ExplainIntentNotice? = null
        private var time = 0L

        /**
         * 用于在第二次判断的时候进行延时
         *
         * 因为有些状态的判断需要延时
         */
        fun delay(time: Long): JudgeOrIntentRequest {
            this.time = time
            return this
        }

        fun explain(explain: (Context) -> ExplainIntentNotice): JudgeOrIntentRequest {
            this.explain = explain.invoke(fragment.requireContext())
            return this
        }

        fun start(listener: OnJudgeResultListener?) {
            val j = judge.invoke(fragment.requireContext())
            log.log { "judge $intent 1: $j." }
            //第一次判断
            if (j) {
                listener?.onJudgeResult(true)
                return
            }
            val e = explain
            if (e != null && e.onIntentExplain()) {
                e.addOnSelectOk {
                    IntentRequest(intent, fragment).start { _, _ ->
                        ThreadHelper.mainHandler.postDelayed({
                            val j2 = judge.invoke(fragment.requireContext())
                            log.log { "judge $intent 2: $j2." }
                            //第二次判断并回调
                            listener?.onJudgeResult(j2)
                        }, time)
                    }
                    e.cancel()
                }.addOnCancel {
                    val j2 = judge.invoke(fragment.requireContext())
                    log.log { "chose cancel, judge $intent 2: $j2." }
                    //第二次判断并回调
                    listener?.onJudgeResult(j2)
                }
            } else {
                IntentRequest(intent, fragment).start { _, _ ->
                    ThreadHelper.mainHandler.postDelayed({
                        val j2 = judge.invoke(fragment.requireContext())
                        log.log { "judge $intent 2: $j2." }
                        //第二次判断并回调
                        listener?.onJudgeResult(j2)
                    }, time)
                }
            }
        }
    }
}

typealias OnJudge = (Context) -> Boolean

interface OnPermissionResultListener {

    fun onPermissionResult(isGrantAll: Boolean, result: Map<String, Boolean>)
}

interface OnIntentResultListener {

    fun onIntentResult(isOk: Boolean, data: Intent?)
}

interface OnJudgeResultListener {

    fun onJudgeResult(result: Boolean)
}

interface ExplainPermissionNotice : Notice {


    /**
     * 当需要解释权限请求时会回调此方法
     *
     * 此方法是用于调整显示
     *
     * @param isBeforeRequest 是否是在请求权限之前的回调
     * @param permissions 当前的权限
     *
     * @return 是否显示
     */
    fun onPermissionExplain(isBeforeRequest: Boolean, permissions: Map<String, Boolean>): Boolean {
        return false
    }
}

interface ExplainIntentNotice : Notice {

    fun onIntentExplain(): Boolean

}
