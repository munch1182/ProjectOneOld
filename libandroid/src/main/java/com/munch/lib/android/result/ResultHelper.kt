package com.munch.lib.android.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.android.AppHelper
import com.munch.lib.android.dialog.ChoseDialog
import com.munch.lib.android.dialog.DialogManager
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.dialog.showThenReturnChose
import com.munch.lib.android.extend.SealedClassToStringByName
import com.munch.lib.android.extend.isDenied
import com.munch.lib.android.extend.isGranted
import com.munch.lib.android.extend.to
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ResultHelper private constructor(private val fm: FragmentManager) {

    companion object {
        fun with(act: FragmentActivity) = ResultHelper(act.supportFragmentManager)
        fun with(frag: Fragment) = ResultHelper(frag.childFragmentManager)
        private const val TAG_RESULT_FRAGMENT = "com.munch.lib.android.result.TAG_RESULT_FRAGMENT"
    }

    private val fragment: ResultFragment
        get() {
            val frag = fm.findFragmentByTag(TAG_RESULT_FRAGMENT)?.to<ResultFragment>()
            if (frag != null) return frag
            return ResultFragment().also {
                fm.beginTransaction().add(it, TAG_RESULT_FRAGMENT)
                    .commitNowAllowingStateLoss()
            }
        }

    /**
     * 请求权限
     */
    fun permission(vararg permission: String) = PermissionResult(arrayOf(*permission), fragment)

    /**
     * 请求一个intent结果
     */
    fun intent(intent: Intent) = IntentResult(intent, fragment)

    /**
     * 先进行一次[judge]判断, 如果失败, 则请求intent, 并在intent返回后, 再进行一次[judge]判断, 并回调结果
     *
     * 常用于需要跳转页面去开启权限的操作
     */
    fun judge(judge: IntentJudge) = JudgeIntentResult(judge, fragment)
}

interface ResultRequester {
    val ctx: Context
    fun start4Result(intent: Intent, listener: IntentResultListener)
    fun start4Permission(permission: Array<String>, listener: PermissionResultListener)
}

sealed class ExplainTime : SealedClassToStringByName() {
    /**
     * 在一切申请之前的解释
     */
    object Before : ExplainTime()

    /**
     * 为申请被拒绝的权限解释
     */
    object ForDenied : ExplainTime()

    /**
     * 为被永久拒绝的权限解释
     */
    object ForDeniedForever : ExplainTime()
}

//<editor-fold desc="permission">
typealias PermissionDialog = (Context, ExplainTime, Map<String, Boolean>) -> ChoseDialog?

/**
 * 请求权限, 并回调请求结果
 */
class PermissionResult(
    private val permission: Array<String>,
    private val requester: ResultRequester
) {

    /**
     * 存放待请求权限的权限
     */
    private val request = mutableListOf<String>()

    /**
     * 存放已被拒绝的权限
     */
    private val denied = mutableListOf<String>()

    /**
     * 权限请求Dialog
     */
    private var pd: PermissionDialog? = null

    /**
     * 权限结果
     */
    private val result = hashMapOf<String, Boolean>()

    private val act: FragmentActivity
        get() = requester.ctx.to()

    /**
     * todo 处理当前页面已有的Dialog和权限请求的dialog的顺序
     */
    fun attach(dm: DialogManager): PermissionResult {
        return this
    }

    /**
     * 设置Dialog创建的回调
     * 当[ExplainTime]时, 会调用此方法来生成Dialog, 如果返回的[IDialog]不为null, 则会显示该[dialog]
     */
    fun setDialog(dialog: PermissionDialog): PermissionResult {
        pd = dialog
        return this
    }

    fun judgeDialog2Execute(listener: PermissionResultListener) {
        AppHelper.launch {
            handle()
            listener.onPermissionResult(!result.containsValue(false), result)
        }
    }

    private suspend fun handle() {
        result.clear()
        clearAll()

        request.addAll(permission) // 将所有权限加入待处理列表

        judgeDialog2Execute(ExplainTime.Before) { requester.permission(request.toTypedArray()) } // 第一次请求 (将永久拒绝的放入denied中, 将未被永久拒绝的放入request中)

        if (request.isNotEmpty()) { // 处理未被永久拒绝的权限
            judgeDialog2Execute(ExplainTime.ForDenied) { requester.permission(request.toTypedArray()) } // 将未被永久拒绝的下一次再请求, 放入request中并且请求
            this.request.forEach { this.result[it] = false } // 第二次请求仍被拒绝的不再请求, 直接当作被拒绝
        }

        if (denied.isNotEmpty()) { // 处理被永久拒绝的权限
            judgeDialog2Execute(ExplainTime.ForDeniedForever) { // 如果允许
                ResultHelper.with(act).intent(Intent(Settings.ACTION_SETTINGS)).start() // 则跳转设置界面
            }
            this.request.forEach { this.result[it] = false } // 第二次请求仍被拒绝的不再请求, 直接当中被拒绝
        }

        // 处理结果
        this.result.clear()
        request.forEach { this.result[it] = it.isGranted() }
    }

    private suspend fun judgeDialog2Execute(time: ExplainTime, execute: suspend () -> Boolean) {
        val dialog = pd?.invoke(act, time, result)?.showThenReturnChose() // 如果能显示弹窗, 则显示并返回结果
        if (dialog == null || dialog.isChoseNext) {  // 未设置对应的Dialog或者选择了请求
            val result = execute.invoke() // 请求所有权限
            if (!result) { // 如果权限未全部获取, 则处理第一次请求结果
                val list = ArrayList(request)
                denied.clear()
                request.clear()
                list.forEach {
                    if (it.isGranted()) {
                        this.result[it] = true // 已拥有的权限记录在result中
                    } else if (it.isDenied(act)) {
                        this.denied.add(it) // 已被永久拒绝的权限放入denied中
                    } else {
                        this.request.add(it) // 未被永久拒绝的下一次再请求, 放入request中
                    }
                }
            }
        }
    }


    private fun clearAll() {
        request.clear()
        denied.clear()
    }

}
//</editor-fold>

//<editor-fold desc="intent">
/**
 * 请求一个intent, 并回调结果
 */
class IntentResult(private val intent: Intent, private val requester: ResultRequester) {

    fun startForResult(listener: IntentResultListener) {
        requester.start4Result(intent, listener)
    }
}
//</editor-fold>

//<editor-fold desc="judge">
typealias IntentJudge = (Context) -> Boolean
typealias IntentDialog = (Context) -> ChoseDialog?

/**
 * 先进行一次[judge]判断
 * 如果成功, 则回调成功,
 * 如果失败, 则请求[intent], 并在intent返回后, 再进行一次[judge]判断, 并回调结果
 *
 * 常用于需要跳转页面去开启权限的操作
 *
 * [judge]中提供的Context与UI无关
 */
class JudgeIntentResult(
    private val judge: IntentJudge,
    private val requester: ResultRequester
) {

    private var intent: Intent? = null
    private var id: IntentDialog? = null

    /**
     * 当第一次判断失败时,会调用[id],
     * 如果返回为null, 则会直接回调
     * 如果返回不为null, 则显示[ChoseDialog], 如果[ChoseDialog]中选择了取消, 则会直接回调, 否则会使用[intent]
     */
    fun setDialog(dialog: IntentDialog): JudgeIntentResult {
        id = dialog
        return this
    }

    fun intent(intent: Intent): JudgeIntentResult {
        this.intent = intent
        return this
    }

    fun startForResult(listener: JudgeIntentResultListener) {
        val firstJudge = judge.invoke(AppHelper)
        val i = intent
        if (firstJudge || i == null) {
            listener.onJudgeIntentResult(true, Activity.RESULT_OK, null)
            return
        }
        AppHelper.launch {
            val chose = id?.invoke(requester.ctx)?.showThenReturnChose() // 显示dialog并返回选择结果
            if (chose == null || chose.isChoseNext) { // 如果未设置Dialog, 或者设置并选择了下一步
                requester.start4Result(i) { resultCode, intent -> // 跳转intent界面并返回结果
                    listener.onJudgeIntentResult(
                        judge.invoke(requester.ctx), resultCode, intent
                    )
                }
            } else { // 未设置或者未选择下一步, 直接回调当前结果
                listener.onJudgeIntentResult(
                    judge.invoke(requester.ctx), Activity.RESULT_CANCELED, null
                )
            }

        }
    }
}
//</editor-fold>

//<editor-fold desc="fargment">
/**
 * 使用一个无UI的Fragment代理当前真正的UI来请求权限, 以汇集请求和结果
 */
private class ResultFragment : Fragment(), ResultRequester {

    private var resultListener: IntentResultListener? = null
    private var permissionListener: PermissionResultListener? = null

    override val ctx: Context
        get() = requireActivity()

    private val result =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            resultListener?.onIntentResult(it.resultCode, it.data)
            resultListener = null
        }
    private val permission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissionListener?.onPermissionResult(!it.containsValue(false), it)
        }

    override fun start4Result(intent: Intent, listener: IntentResultListener) {
        this.resultListener = listener
        this.result.launch(intent)
    }

    override fun start4Permission(
        permission: Array<String>,
        listener: PermissionResultListener
    ) {
        this.permissionListener = listener
        this.permission.launch(permission)
    }

}
//</editor-fold>

//<editor-fold desc="listener">
fun interface PermissionResultListener {
    fun onPermissionResult(isGrantAll: Boolean, result: Map<String, Boolean>)
}

fun interface PermissionResultSimpleListener {
    fun onPermission(isGrantAll: Boolean)
}

fun interface IntentResultListener {
    fun onIntentResult(resultCode: Int, intent: Intent?)
}

fun interface IntentResultSimpleListener {
    fun onIntentResult(isOk: Boolean)
}

fun interface JudgeIntentResultListener {
    fun onJudgeIntentResult(judgeResult: Boolean, resultCode: Int, intent: Intent?)
}

fun interface JudgeIntentResultSimpleListener {
    fun onJudgeResult(judgeResult: Boolean)
}
//</editor-fold>

//<editor-fold desc="extend">
fun PermissionResult.start(listener: PermissionResultSimpleListener) {
    judgeDialog2Execute { isGrandAll, _ -> listener.onPermission(isGrandAll) }
}

suspend fun PermissionResult.start(): Boolean = suspendCancellableCoroutine {
    judgeDialog2Execute { isGrantAll, _ -> it.resume(isGrantAll) }
}

fun IntentResult.start(c: Int = Activity.RESULT_OK, listener: IntentResultSimpleListener) {
    startForResult { code, _ -> listener.onIntentResult(code == c) }
}

suspend fun IntentResult.start(c: Int = Activity.RESULT_OK): Boolean = suspendCancellableCoroutine {
    startForResult { code, _ -> it.resume(c == code) }
}

fun JudgeIntentResult.start(listener: JudgeIntentResultSimpleListener) {
    startForResult { judge, _, _ -> listener.onJudgeResult(judge) }
}

suspend fun JudgeIntentResult.start(): Boolean = suspendCancellableCoroutine {
    startForResult { judge, _, _ -> it.resume(judge) }
}

suspend fun ResultRequester.permission(permission: Array<String>): Boolean =
    suspendCancellableCoroutine {
        start4Permission(permission) { isGrantAll, _ -> it.resume(isGrantAll) }
    }
//</editor-fold>