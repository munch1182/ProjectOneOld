package com.munch.lib.result

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.base.OnCancelListener
import com.munch.lib.base.OnNextListener
import com.munch.lib.dialog.IDialogHandler
import java.util.*

/**
 * Create by munch1182 on 2021/8/20 16:37.
 */

/**
 * 用于标识[ResultHelper]可执行的操作
 */
interface IResult

/**
 * 调用[with]去执行一个操作
 * 调用[contactWith]来执行连续的操作，比如权限拒绝后要手动开启权限的情形；或者检查定位权限但需要打开gps开关的情形
 */
class ResultHelper(private val fm: FragmentManager) {

    companion object {

        fun init(activity: FragmentActivity) = ResultHelper(activity.supportFragmentManager)
        fun init(fragment: Fragment) = ResultHelper(fragment.childFragmentManager)
    }

    private val fragment: InvisibleFragment
        get() {
            var fragment = fm.findFragmentByTag(InvisibleFragment.TAG) as? InvisibleFragment?
            if (fragment == null) {
                fragment = InvisibleFragment()
                fm.beginTransaction()
                    .add(fragment, InvisibleFragment.TAG)
                    .commitNowAllowingStateLoss()
            }
            return fragment
        }

    /**
     * 用于发起一个权限请求并回调结果
     */
    fun with(vararg permission: String) = PermissionResult(permission)

    /**
     * 用于发起一个[intent]请求并回调结果，包括resultCode
     */
    fun with(intent: Intent) = IntentResult(intent)

    /**
     * 首先会调用[judge]来判断，如果为false则会调用[intent]来发起请求，为true则回调true
     * 再次回到此页后会重新进行[judge]判断并回调结果
     */
    fun with(judge: () -> Boolean, intent: Intent) =
        CheckOrIntentResult(judge, intent)

    /**
     * 用于发起一个权限请求作为请求结果的第一步，后续可以添加其它方法来组合并获得最后结果的回调
     */
    fun contactWith(vararg permission: String) = ContactResult(PermissionResult(permission))

    /**
     * 用于发起一个Intent请求作为请求结果的第一步，后续可以添加其它方法来组合并获得最后结果的回调
     */
    fun contactWith(intent: Intent) = ContactResult(IntentResult(intent))

    /**
     * 用于发起一个带[judge]的Intent请求作为请求结果的第一步，后续可以添加其它方法来组合并获得最后结果的回调
     */
    fun contactWith(judge: () -> Boolean, intent: Intent) =
        ContactResult(CheckOrIntentResult(judge, intent))

    inner class PermissionResult(private val permissions: Array<out String>) : IResult {

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
        ): PermissionResult {
            onExplainRequestReasonListener = explain
            return this
        }

        fun explainBeforeRequest(): PermissionResult {
            return this
        }

        fun request(listener: OnPermissionResultListener) {
            resultListener = listener
            fragment.requestPermissions(permissions, resultCallback)
        }

        fun request(
            onResult: (
                allGrant: Boolean,
                grantedList: ArrayList<String>,
                deniedList: ArrayList<String>
            ) -> Unit
        ) {
            request(object : OnPermissionResultListener {
                override fun onResult(
                    allGrant: Boolean,
                    grantedList: ArrayList<String>,
                    deniedList: ArrayList<String>
                ) {
                    onResult.invoke(allGrant, grantedList, deniedList)
                }
            })
        }

        fun requestSimple(onResult: (allGrant: Boolean) -> Unit) {
            request(object : OnPermissionResultListener {
                override fun onResult(
                    allGrant: Boolean,
                    grantedList: ArrayList<String>,
                    deniedList: ArrayList<String>
                ) {
                    onResult.invoke(allGrant)
                }
            })
        }
    }

    inner class IntentResult(private val intent: Intent) : IResult {

        fun start(listener: OnActivityResultListener) {
            fragment.startActivityForResult(intent, listener)
        }

        fun start(onResult: (isOk: Boolean) -> Unit) {
            start(object : OnActivityResultListener {
                override fun onResult(isOk: Boolean, resultCode: Int, data: Intent?) {
                    onResult.invoke(isOk)
                }
            })
        }
    }

    inner class CheckOrIntentResult(private val judge: () -> Boolean, private val intent: Intent) :
        IResult {

        private var checkAllTime = false

        fun justCheckFirst(justFirst: Boolean = true): CheckOrIntentResult {
            checkAllTime = !justFirst
            return this
        }

        fun start(onResult: (result: Boolean) -> Unit) {
            if (judge.invoke()) {
                if (checkAllTime) {
                    fragment.setOnTriggeredListener(checkAllTime) { onResult.invoke(judge.invoke()) }
                }
                onResult.invoke(true)
            } else {
                fragment.setOnTriggeredListener(checkAllTime) {
                    if (!checkAllTime) {
                        fragment.setOnTriggeredListener(checkAllTime, null)
                    }
                    onResult.invoke(judge.invoke())
                }.startActivity(intent)
            }
        }
    }

    inner class ContactResult(op: IResult) : IResult {

        private val resultList = LinkedList<IResult>()
        private var judge: ((allGrant: Boolean, grantedList: ArrayList<String>, deniedList: ArrayList<String>) -> Boolean)? =
            null

        init {
            resultList.offer(op)
        }

        fun contactWith(vararg permission: String): ContactResult {
            resultList.offer(PermissionResult(permission))
            return this
        }

        fun contactWith(intent: Intent): ContactResult {
            resultList.offer(IntentResult(intent))
            return this
        }

        fun contactWith(judge: () -> Boolean, intent: Intent): ContactResult {
            resultList.offer(CheckOrIntentResult(judge, intent))
            return this
        }

        fun judgePermission(
            judge: (
                allGrant: Boolean, grantedList: ArrayList<String>, deniedList: ArrayList<String>
            ) -> Boolean
        ): ContactResult {
            this.judge = judge
            return this
        }

        fun start(onResult: (isOk: Boolean) -> Unit) {
            when (val first = resultList.pollFirst()) {
                is PermissionResult -> first.request { allGrant, grantedList, deniedList ->
                    if (judge == null && allGrant) {
                        start(onResult)
                    } else if (judge?.invoke(allGrant, grantedList, deniedList) == false) {
                        onResult.invoke(false)
                    } else {
                        start(onResult)
                    }
                }
                is IntentResult -> first.start {
                    if (!it) {
                        onResult.invoke(false)
                    } else {
                        start(onResult)
                    }
                }
                is CheckOrIntentResult -> first.start {
                    if (!it) {
                        onResult.invoke(false)
                    } else {
                        start(onResult)
                    }
                }
                null -> onResult.invoke(true)
                else -> throw IllegalStateException()
            }
        }
    }

    interface OnPermissionResultListener {
        fun onResult(
            allGrant: Boolean,
            grantedList: ArrayList<String>,
            deniedList: ArrayList<String>
        )
    }

    interface OnActivityResultListener {
        fun onResult(isOk: Boolean, resultCode: Int, data: Intent?)
    }
}