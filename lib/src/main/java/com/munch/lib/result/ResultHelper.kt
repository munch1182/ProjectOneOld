package com.munch.lib.result

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.dialog.IDialogHandler
import java.util.*

/**
 * Create by munch1182 on 2021/8/20 16:37.
 */

/**
 * 用于标识[ResultHelper]可执行的操作
 */
interface IResult

internal typealias PermissionsDialog = (context: Context, permissions: Array<out String>) -> IDialogHandler?
internal typealias PermissionDialog = (context: Context, permissions: String) -> IDialogHandler?

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

        private val pb: PermissionRequestBuilder = PermissionRequestBuilder(permissions)

        fun request(listener: OnPermissionResultListener) {
            pb.listener = listener
            fragment.requestPermissions(pb)
        }

        fun request(grant: (allGrant: Boolean) -> Unit) {
            request(object : OnPermissionResultListener {
                override fun onResult(
                    allGrant: Boolean,
                    grantedList: MutableList<String>,
                    deniedList: MutableList<String>
                ) {
                    grant.invoke(allGrant)
                }
            })
        }

        fun requestGrant(grant: () -> Unit) {
            request(object : OnPermissionResultListener {
                override fun onResult(
                    allGrant: Boolean,
                    grantedList: MutableList<String>,
                    deniedList: MutableList<String>
                ) {
                    if (allGrant) {
                        grant.invoke()
                    }
                }
            })
        }

        /**
         * 设置是否当申请权限之前，显示Dialog解释权限请求的原因，此显示的Dialog即[explainPermission]的dialog
         */
        fun explainFirst(first: Boolean): PermissionResult {
            pb.explainFirst = first
            return this
        }

        /**
         * 解释权限显示的Dialog
         *
         * 如果[dialog]返回为null，则不显示dialog
         * 如果设置，则：
         * 1. 如果设置[explainFirst]为true，则会在申请权限之前显示此Dialog，参数[permissions]为需要申请的权限列表
         * 2. 当申请被拒绝之后，则显示此Dialog，参数[permissions]为需要被拒绝后需要再次申请的权限列表
         */
        fun explainPermission(dialog: PermissionsDialog): PermissionResult {
            pb.explainDialog = dialog
            return this
        }

        /**
         * 当申请被拒绝且不再询问之后，显示此Dialog解释需要跳转去手动开启权限
         */
        fun explainWhenDeniedPermanent(dialog: PermissionsDialog): PermissionResult {
            pb.explainDialogNeed2Setting = dialog
            return this
        }
    }

    inner class PermissionRequestBuilder(val permissions: Array<out String>) {

        //权限解释Dialog
        var explainDialog: PermissionsDialog? = null

        //当权限被永久拒绝后，解释前往设置的Dialog
        var explainDialogNeed2Setting: PermissionsDialog? = null

        //是否需要在第一次申请权限前显示权限解释Dialog
        var explainFirst: Boolean = false

        //最终结果回调
        var listener: OnPermissionResultListener? = null
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

        fun start(onResult: (result: Boolean) -> Unit) {
            if (judge.invoke()) {
                onResult.invoke(true)
            } else {
                fragment.startActivityForResult(intent, object : OnActivityResultListener {
                    override fun onResult(isOk: Boolean, resultCode: Int, data: Intent?) {
                        onResult.invoke(judge.invoke())
                    }
                })
            }
        }

        fun startOk(isOk: () -> Unit) {
            start {
                if (it) {
                    isOk.invoke()
                }
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
                is PermissionResult -> first.request {
                    if (!it) {
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
            grantedList: MutableList<String>,
            deniedList: MutableList<String>
        )
    }

    interface OnActivityResultListener {
        fun onResult(isOk: Boolean, resultCode: Int, data: Intent?)
    }
}