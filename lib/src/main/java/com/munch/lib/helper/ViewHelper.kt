package com.munch.lib.helper

import android.os.Build
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.children

/**
 * Create by munch1182 on 2020/12/7 10:27.
 */
/**
 * 给ViewGroup子控件批量设置点击事件
 * 注意：使用了view的tag，将tag设置为位置值
 * @param clazz 需要设置点击事件的子类类型，此值影响view的tag代表的位置值
 */
fun ViewGroup.clickItem(listener: View.OnClickListener, vararg clazz: Class<out View>) {
    var index = 0
    var willTag: Boolean
    this.children.forEach view@{ view ->
        if (clazz.isNotEmpty()) {
            willTag = false
            clazz.forEach clazz@{ clazz ->
                //是需要的类
                if (clazz.isAssignableFrom(view::class.java)) {
                    willTag = true
                    return@clazz
                }
            }
            if (!willTag) {
                return@view
            }
        }
        view.tag = index
        view.setOnClickListener(listener)
        index++
    }
}

fun View.setMargin(margin: Int) = setMargin(margin, margin, margin, margin, false)
fun View.setMargin(lr: Int, tb: Int) = setMargin(lr, tb, lr, tb, false)
fun View.setMargin(l: Int, t: Int, r: Int, b: Int) = setMargin(l, t, r, b, false)
fun View.addMargin(margin: Int) = setMargin(margin, margin, margin, margin, true)
fun View.addMargin(lr: Int, tb: Int) = setMargin(lr, tb, lr, tb, true)
fun View.addMargin(l: Int, t: Int, r: Int, b: Int) = setMargin(l, t, r, b, true)

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

fun TextView.setTextSizeSp(spVal: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, spVal)
}

fun EditText.nonInput() {
    keyListener = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        focusable = View.NOT_FOCUSABLE
    }
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
}
