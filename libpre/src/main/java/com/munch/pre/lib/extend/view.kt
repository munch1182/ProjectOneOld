package com.munch.pre.lib.extend

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.method.DigitsKeyListener
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.method.ReplacementTransformationMethod
import android.util.TypedValue
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.children
import com.munch.pre.lib.UNTESTED
import com.munch.pre.lib.base.listener.ViewIntTagClickListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

/**
 * 1s内重复点击只有第一次有效
 */
suspend fun View.setOnClickStart(action: suspend (view: View) -> Unit) {
    setOnClickStart(action, 1000L)
}

/**
 * 重复点击只有第一次有效
 */
@UNTESTED
suspend fun View.setOnClickStart(action: suspend (view: View) -> Unit, delayTime: Long) {
    /*flow {
         setOnClickListener {
             emit(Unit)
         }
     }.conflate()//conflate只会处理收集能够处理时的最新数据
         .collect {
             action.invoke(this@setOnClickStart)
         }*/
    val channel = Channel<Unit>()
    setOnClickListener {
        channel.offer(Unit)
    }
    for (i in channel) {
        action.invoke(this)
        delay(delayTime)
    }
}

/**
 * 给ViewGroup子控件批量设置点击事件
 * 注意：使用了view的tag，将tag设置为位置值
 *
 * @param clazz 需要设置点击事件的子类类型，此值影响view的tag代表的位置值
 * @param listener 子view的点击回调，index代表该view在被此方法设置点击事件中的位置，而不是子view的顺序
 */
fun ViewGroup.clickItem(listener: (v: View, index: Int) -> Unit, vararg clazz: Class<out View>) {
    val viewIndexClickListener = object : ViewIntTagClickListener {
        override fun onClick(v: View, index: Int) {
            listener.invoke(v, index)
        }
    }
    var index = 0
    this.children.forEach {
        if (clazz.isNotEmpty() && it::class.java !in clazz) {
            return@forEach
        }
        it.tag = index
        index++
        it.setOnClickListener(viewIndexClickListener)
    }
}

fun View.setPaddingCompat(
    l: Int = paddingLeft,
    t: Int = paddingTop,
    r: Int = paddingRight,
    b: Int = paddingBottom
) = setPadding(l, t, r, b)

fun View.setPadding(lr: Int, tb: Int) = setPadding(lr, tb, lr, tb)
fun View.setPadding(padding: Int) = setPadding(padding, padding, padding, padding)
fun View.addPadding(padding: Int) = setPadding(
    paddingLeft + padding,
    paddingTop + padding,
    paddingRight + padding,
    paddingBottom + padding
)

fun View.addPadding(l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0) = setPadding(
    paddingLeft + l,
    paddingTop + t,
    paddingRight + r,
    paddingBottom + b
)

fun View.setMargin(margin: Int) = setMargin(margin, margin, margin, margin, false)
fun View.setMargin(lr: Int, tb: Int) = setMargin(lr, tb, lr, tb, false)
fun View.setMargin(l: Int, t: Int, r: Int, b: Int) = setMargin(l, t, r, b, false)
fun View.addMargin(margin: Int) = setMargin(margin, margin, margin, margin, true)
fun View.addMargin(l: Int = 0, t: Int = 0, r: Int = 0, b: Int = 0) = setMargin(l, t, r, b, true)
fun View.addMargin(lr: Int, tb: Int) = setMargin(lr, tb, lr, tb, true)

/**
 * @param defParams 如果view的[View.getLayoutParams]为null则传入的初始化LayoutParams
 * 如不传入默认为[ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)]
 */
fun View.setMargin(
    l: Int,
    t: Int,
    r: Int,
    b: Int,
    add: Boolean,
    defParams: ViewGroup.MarginLayoutParams? = null
) {
    when (val params = this.layoutParams) {
        null -> {
            this.layoutParams = (defParams ?: ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )).apply {
                setMargins(l, t, r, b)
            }
        }
        is ViewGroup.MarginLayoutParams -> {
            params.setMargins(
                l + if (add) params.marginStart else 0,
                t + if (add) params.topMargin else 0,
                r + if (add) params.marginEnd else 0,
                b + if (add) params.bottomMargin else 0
            )
        }
        else -> {
            this.layoutParams =
                ViewGroup.MarginLayoutParams(params).apply { setMargins(l, t, r, b) }
        }
    }
}

fun View.setParams(func: (param: ViewGroup.LayoutParams) -> Unit) {
    this.layoutParams = when (val params = this.layoutParams) {
        null -> {
            (ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )).apply(func)
        }
        else -> params.apply(func)
    }
}

fun TextView.setTextSizeSp(spVal: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, spVal)
}

fun EditText.nonInput() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        focusable = View.NOT_FOCUSABLE
    }
    isFocusable = false
    isFocusableInTouchMode = false
}

fun EditText.inputActive() {
    isFocusable = true
    isFocusableInTouchMode = true
}

fun EditText.setTextCompat(text: String?) {
    setText(text)
    setSelection(text?.length ?: return)
}

fun EditText.pwdTransMethod(show: Boolean) {
    if (show) {
        if (transformationMethod == PasswordTransformationMethod.getInstance()) {
            transformationMethod = HideReturnsTransformationMethod.getInstance()
        }
    } else {
        if (transformationMethod == HideReturnsTransformationMethod.getInstance()) {
            transformationMethod = PasswordTransformationMethod.getInstance()
        }
    }
    if (text.isNotEmpty()) {
        setSelection(text.length)
    }
}

fun EditText.lowerInput() {
    transformationMethod = object : ReplacementTransformationMethod() {
        override fun getReplacement(): CharArray {
            return charArrayOf(
                'a',
                'b',
                'c',
                'd',
                'e',
                'f',
                'g',
                'h',
                'i',
                'j',
                'k',
                'l',
                'm',
                'n',
                'o',
                'p',
                'q',
                'r',
                's',
                't',
                'u',
                'v',
                'w',
                'x',
                'y',
                'z'
            )
        }

        override fun getOriginal(): CharArray {
            return charArrayOf(
                'A',
                'B',
                'C',
                'D',
                'E',
                'F',
                'G',
                'H',
                'I',
                'J',
                'K',
                'L',
                'M',
                'N',
                'O',
                'P',
                'Q',
                'R',
                'S',
                'T',
                'U',
                'V',
                'W',
                'X',
                'Y',
                'Z'
            )
        }
    }
}

fun EditText.upperInput() {
    transformationMethod = object : ReplacementTransformationMethod() {
        override fun getOriginal(): CharArray {
            return charArrayOf(
                'a',
                'b',
                'c',
                'd',
                'e',
                'f',
                'g',
                'h',
                'i',
                'j',
                'k',
                'l',
                'm',
                'n',
                'o',
                'p',
                'q',
                'r',
                's',
                't',
                'u',
                'v',
                'w',
                'x',
                'y',
                'z'
            )
        }

        override fun getReplacement(): CharArray {
            return charArrayOf(
                'A',
                'B',
                'C',
                'D',
                'E',
                'F',
                'G',
                'H',
                'I',
                'J',
                'K',
                'L',
                'M',
                'N',
                'O',
                'P',
                'Q',
                'R',
                'S',
                'T',
                'U',
                'V',
                'W',
                'X',
                'Y',
                'Z'
            )
        }
    }
}

/**
 * 设置edit的输入限制，即只能输入[digits]中的字符
 */
fun EditText.digitsInput(digits: String = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") {
    keyListener = DigitsKeyListener.getInstance(digits)
}

object ViewHelper {

    /**
     * 用于一个页面多个输入值的非空判断和获取
     *
     * 注意，返回值是根据传入的顺序返回的
     *
     * @param onCheckEmpty 当检查到有textview的text为空时回调，当返回为false忽略为空将空字符串加入该位置并进行下一个view的判断
     * 否则直接返回空集合
     */
    fun checkTextViewEmpty(
        onCheckEmpty: (view: TextView) -> Boolean,
        vararg textViews: TextView
    ): ArrayList<String> {
        if (textViews.isEmpty()) {
            return arrayListOf()
        }
        var str: String
        val list = ArrayList<String>(textViews.size)
        textViews.forEach {
            str = it.text.toString().trim()
            if (str.isEmpty() && !onCheckEmpty(it)) {
                return arrayListOf()
            }
            list.add(str)
        }
        return list
    }

    /**
     * 对传入的textview数组进行批量赋值
     *
     * 注意：对于[views]和[str]一一对应
     */
    fun setTextViewVal(views: Array<TextView>, str: Array<String?>) {
        if (views.size != str.size) {
            throw Exception("view与数据必须对等")
        }
        views.forEachIndexed { index, view -> view.text = str[index] }
    }

    /**
     * 批量设置是否可见
     *
     * 注意：不可见是[View.GONE]
     */
    fun setVisibility(isShow: Boolean, vararg views: View) {
        val vis = if (isShow) View.VISIBLE else View.GONE
        for (view in views) {
            view.visibility = vis
        }
    }

    /**
     * 批量设置是否可见
     *
     * 注意：不可见是[View.INVISIBLE]
     */
    fun setVisibility2Invisible(isShow: Boolean, vararg views: View) {
        val vis = if (isShow) View.VISIBLE else View.INVISIBLE
        for (view in views) {
            view.visibility = vis
        }
    }

    fun newParamsMW(): ViewGroup.LayoutParams {
        return ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun newParamsMM(): ViewGroup.LayoutParams {
        return ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun newParamsWW(): ViewGroup.LayoutParams {
        return ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun newMarginParamsMW(): ViewGroup.MarginLayoutParams {
        return ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun newMarginParamsMM(): ViewGroup.MarginLayoutParams {
        return ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    fun newMarginParamsWW(): ViewGroup.MarginLayoutParams {
        return ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    @Suppress("DEPRECATION")
    fun capture(activity: Activity, view: View): Bitmap {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888, true)
            PixelCopy.request(
                activity.window,
                Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
                bitmap,
                {},
                Handler(Looper.getMainLooper())
            )
        } else {
            view.isDrawingCacheEnabled = true
            view.buildDrawingCache()
            bitmap = view.drawingCache
        }
        return bitmap
    }
}
