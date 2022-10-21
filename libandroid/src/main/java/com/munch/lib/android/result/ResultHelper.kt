@file:Suppress("NOTHING_TO_INLINE")

package com.munch.lib.android.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.munch.lib.android.AppHelper
import com.munch.lib.android.dialog.ChoseDialog
import com.munch.lib.android.dialog.IDialog
import com.munch.lib.android.dialog.IDialogManager
import com.munch.lib.android.dialog.showThenReturnChose
import com.munch.lib.android.extend.*
import com.munch.lib.android.log.Logger
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
            return getInMain {
                ResultFragment().also {
                    fm.beginTransaction().add(it, TAG_RESULT_FRAGMENT)
                        .commitNowAllowingStateLoss()
                }
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

    /**
     * 顺序添加和执行Result
     */
    fun then(vararg permission: String) = CombinedResult(permission(*permission), this)
    fun then(intent: Intent) = CombinedResult(intent(intent), this)
    fun then(judge: IntentJudge, intent: Intent) = CombinedResult(judge(judge).intent(intent), this)
}

interface IResult

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
typealias PermissionDialogCreator = (Context, ExplainTime, Array<String>) -> ChoseDialog?

/**
 * 请求权限, 并回调请求结果
 */
class PermissionResult(
    private val permission: Array<String>,
    private val requester: ResultRequester
) : IResult {

    private val log = Logger("permission")

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
    private var pd: PermissionDialogCreator? = null

    /**
     * 权限结果
     */
    private val result = hashMapOf<String, Boolean>()

    private val act: FragmentActivity
        get() = requester.ctx.to()

    private var dm: IDialogManager? = null

    /**
     * 设置Dialog创建的回调
     * 当[ExplainTime]时, 会调用此方法来生成Dialog, 如果返回的[IDialog]不为null, 则会显示该[dialog]
     */
    fun setDialog(dm: IDialogManager? = null, dialog: PermissionDialogCreator): PermissionResult {
        this.dm = dm
        this.pd = dialog
        return this
    }

    fun request(listener: PermissionResultListener) {
        AppHelper.launch {

            catch { handle() }

            request.clear()
            denied.clear()
            result.clear()

            // 所有流程重新判断一次权限
            permission.forEach { result[it] = it.isGranted() }
            val isGrantAll = !result.containsValue(false)
            log.log("permission request result: ${if (isGrantAll) "grantAll" else fmt(result)}.")
            listener.onPermissionResult(isGrantAll, result)
        }
    }

    private suspend fun handle() {
        result.clear()
        request.clear()
        denied.clear()

        // 第一次权限判断, 注意此时可能未被授予的权限都被永久拒绝了, 则会先显示Before再直接显示ForDeniedForever
        permission.forEach { result[it] = it.isGranted() }

        request.addAll(result.filter { !it.value }.keys.toTypedArray()) // 用于申请未被授予的权限

        if (request.isNotEmpty()) { // 如果有未被授予的权限
            val dialogBefore = pd?.invoke(act, ExplainTime.Before, request.toTypedArray())
            if (dialogBefore != null) {
                log.log("permission explain dialog: Before.")
            }
            val choseForBefore = dialogBefore?.showThenReturnChose(dm)?.isChoseOk ?: true
            if (choseForBefore) { // 如果显示了dialog, 并且选择了确认; 或者没有显示dialog

                log.log("request permission: ${fmt(request)}.")

                val isGrantAllFroAll = requester.permission(request.toTypedArray()) // 第一次请求权限

                if (!isGrantAllFroAll) { // 如果有未被授予的权限
                    val newAll = request.new
                    request.clear()

                    newAll.forEach {
                        if (it.isGranted()) { // 已经取得的权限放入结果中
                            result[it] = true
                        } else if (it.isDenied(act)) {
                            denied.add(it) // 被永久拒绝的权限, 放入denied
                        } else {
                            request.add(it) // 未被永久拒绝的权限, 放入request
                        }
                    }

                    if (request.isNotEmpty()) {  // 如果有未被永久拒绝的权限
                        val dialogDenied =
                            pd?.invoke(act, ExplainTime.ForDenied, request.toTypedArray())
                        if (dialogDenied != null) {
                            log.log("permission explain dialog: ForDenied.")
                        }
                        val chose = dialogDenied?.showThenReturnChose(dm)?.isChoseOk ?: false

                        if (chose) { // 如果显示了dialog并且选择了确认

                            log.log("request permission: ${fmt(request)}.")

                            val isGrantAllForDenied =
                                requester.permission(request.toTypedArray()) // 第二次请求未被永久拒绝的权限

                            if (isGrantAllForDenied) {
                                request.forEach { result[it] = true }
                            } else {
                                val newDenied = request.new
                                request.clear()

                                newDenied.forEach {
                                    if (it.isGranted()) { // 已经取得的权限放入结果中
                                        result[it] = true
                                    } else if (it.isDenied(act)) {
                                        denied.add(it) // 被永久拒绝的权限, 放入denied
                                    } else {
                                        request.add(it) // 未被永久拒绝的权限, 放入request
                                    }
                                }
                            }

                            if (request.isNotEmpty()) { //如果两次请求仍有未被完全拒绝的权限, 则直接放入永久拒绝
                                denied.addAll(request)
                                request.clear()
                            }
                        } else {
                            log.log("${if (dialogDenied != null) "chose cancel" else "no explain"}. do nothing for denied.")
                        }
                    }

                    if (denied.isNotEmpty()) {
                        request.clear()
                        request.addAll(denied)
                        val dialogDeniedForever =
                            pd?.invoke(act, ExplainTime.ForDeniedForever, request.toTypedArray())
                        if (dialogDeniedForever != null) {
                            log.log("permission explain dialog: ForDeniedForever.")
                        }

                        val chose = dialogDeniedForever?.showThenReturnChose(dm)?.isChoseOk ?: false

                        if (chose) { // 如果显示了dialog并且选择了确认
                            log.log("start ACTION_SETTINGS for deniedForever.")
                            ResultHelper.with(act).intent(setting).start()
                        } else {
                            log.log("${if (dialogDeniedForever != null) "chose cancel" else "no explain"}. do nothing for deniedForever.")
                        }
                    }
                }
            } else {
                log.log("permission request: denied.")
            }
        }
    }

    private fun fmt(m: Map<String, Boolean>) = m.keys.joinToString { "${it.fmt()}:${m[it]}" }

    private fun fmt(p: MutableList<String>) =
        p.joinToString { it.fmt() }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun String.fmt() = removePrefix("android.permission.")
}
//</editor-fold>

//<editor-fold desc="intent">
/**
 * 请求一个intent, 并回调结果
 */
class IntentResult(private val intent: Intent, private val requester: ResultRequester) : IResult {

    fun startForResult(listener: IntentResultListener) {
        requester.start4Result(intent, listener)
    }
}
//</editor-fold>

//<editor-fold desc="judge">
typealias IntentJudge = (Context) -> Boolean
typealias IntentDialogCreator = (Context) -> ChoseDialog?

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
    private var requester: ResultRequester
) : IResult {

    private var intent: Intent? = null
    private var dialog: IntentDialogCreator? = null
    private var dm: IDialogManager? = null

    /**
     * 当第一次判断失败时,会调用[dialog],
     * 如果返回为null, 则会直接回调
     * 如果返回不为null, 则显示[ChoseDialog], 如果[ChoseDialog]中选择了取消, 则会直接回调, 否则会使用[intent]
     */
    fun setDialog(dm: IDialogManager? = null, dialog: IntentDialogCreator): JudgeIntentResult {
        this.dm = dm
        this.dialog = dialog
        return this
    }

    fun intent(intent: Intent): JudgeIntentResult {
        this.intent = intent
        return this
    }

    fun intent(action: String) = intent(Intent(action))

    fun startForResult(listener: JudgeIntentResultListener) {
        val firstJudge = judge.invoke(AppHelper)
        if (firstJudge) {
            listener.onJudgeIntentResult(true, Activity.RESULT_OK, null)
            return
        }
        val i = intent
        if (i == null) {
            listener.onJudgeIntentResult(false, Activity.RESULT_CANCELED, null)
            return
        }
        AppHelper.launch {
            val chose = dialog?.invoke(requester.ctx)?.showThenReturnChose(dm) // 显示dialog并返回选择结果
            if (chose == null || chose.isChoseOk) { // 如果未设置Dialog, 或者设置并选择了下一步
                requester.start4Result(i) { resultCode, intent -> // 跳转intent界面并返回结果
                    val secondResult = judge.invoke(requester.ctx)
                    listener.onJudgeIntentResult(secondResult, resultCode, intent)
                }
            } else { // 未设置或者未选择下一步, 直接回调当前结果
                val secondResult = judge.invoke(requester.ctx)
                listener.onJudgeIntentResult(secondResult, Activity.RESULT_CANCELED, null)
            }

        }
    }
}
//</editor-fold>

/**
 * 联合请求结果,任一个结果不为true都会停止执行并返回false
 */
class CombinedResult(result: IResult, private val helper: ResultHelper) {

    private val results = mutableListOf(result)

    private fun then(result: IResult): CombinedResult {
        results.add(result)
        return this
    }

    fun then(vararg permission: String) = then(helper.permission(*permission))
    fun then(intent: Intent) = then(helper.intent(intent))
    fun then(judge: IntentJudge, intent: Intent) = then(helper.judge(judge).intent(intent))

    fun start(l: ContactResultListener) {
        AppHelper.launch {
            var isAllOk = true
            kotlin.run stop@{
                results.forEach {
                    when (it) {
                        is PermissionResult -> isAllOk = it.start()
                        is JudgeIntentResult -> isAllOk = it.start()
                        is IntentResult -> isAllOk = it.start()
                    }
                    if (!isAllOk) {
                        return@stop
                    }
                }
            }
            l.isAllOk(isAllOk)
        }
    }
}

//<editor-fold desc="fargment">
/**
 * 使用一个无UI的Fragment代理当前真正的UI来请求权限, 以汇集请求和结果
 */
class ResultFragment : Fragment(), ResultRequester {

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
            permissionListener = null
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

fun interface ContactResultListener {
    fun isAllOk(ok: Boolean)
}
//</editor-fold>

//<editor-fold desc="extend">
fun PermissionResult.start(listener: PermissionResultSimpleListener) {
    request { isGrantAll, _ -> listener.onPermission(isGrantAll) }
}

suspend fun PermissionResult.start(): Boolean = suspendCancellableCoroutine {
    request { isGrantAll, _ -> it.resume(isGrantAll) }
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

inline fun FragmentActivity.with(vararg permission: String) =
    ResultHelper.with(this).permission(*permission)

inline fun Fragment.with(vararg permission: String) =
    ResultHelper.with(this).permission(*permission)

inline fun FragmentActivity.with(intent: Intent) = ResultHelper.with(this).intent(intent)
inline fun Fragment.with(intent: Intent) = ResultHelper.with(this).intent(intent)

inline fun FragmentActivity.with(noinline judge: IntentJudge) =
    ResultHelper.with(this).judge(judge)

inline fun Fragment.with(noinline judge: IntentJudge) =
    ResultHelper.with(this).judge(judge)

inline fun FragmentActivity.then(vararg permission: String) =
    ResultHelper.with(this).then(*permission)

inline fun Fragment.then(vararg permission: String) = ResultHelper.with(this).then(*permission)

inline fun FragmentActivity.then(intent: Intent) = ResultHelper.with(this).then(intent)
inline fun Fragment.then(intent: Intent) = ResultHelper.with(this).then(intent)

inline fun FragmentActivity.then(noinline judge: IntentJudge, intent: Intent) =
    ResultHelper.with(this).then(judge, intent)

inline fun Fragment.then(noinline judge: IntentJudge, intent: Intent) =
    ResultHelper.with(this).then(judge, intent)

//</editor-fold>