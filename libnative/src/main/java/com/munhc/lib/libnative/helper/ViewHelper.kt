package com.munhc.lib.libnative.helper

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.children
import com.munhc.lib.libnative.excetion.MethodEcxception

/**
 * Created by Munch on 2019/7/15 13:50
 */
object ViewHelper {

    fun setNonInput(editText: EditText) {
        editText.keyListener = null
        editText.isFocusable = false
    }

    fun getParams(view: View): ViewGroup.LayoutParams {
        return view.layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    fun clickItem(vg: ViewGroup, listener: View.OnClickListener, vararg clazz: Class<in View>) {
        vg.children.forEachIndexed { index, view ->
            if (clazz.isNotEmpty()) {
                clazz.forEach {
                    if (it.isAssignableFrom(view::class.java)) {
                        view.tag = index
                        view.setOnClickListener(listener)
                    }
                }
            } else {
                view.tag = index
                view.setOnClickListener(listener)
            }
        }

    }

    fun setTextViewNonVal(views: Array<TextView>, strs: Array<String?>) {
        if (views.size != strs.size) {
            throw MethodEcxception.wrongParameter()
        }
        views.forEachIndexed { index, textView ->
            textView.text = strs[index] ?: ""
        }
    }

    fun checkTextEmpry(listener: OnCheckTextViewListener, vararg views: TextView) {
        if (views.isEmpty()) {
            return
        }
        val array = ArrayList<String>(views.size)
        views.forEach {
            val str = it.text.toString()
            if (str.isEmpty() && !listener.onCheckEmpty(it)) {
                return
            }
            array.add(str)
        }
        listener.onResult(array)
    }

    interface OnCheckTextViewListener {

        /**
         * 当检查到view的text为空时回调
         *
         * @param view 数据为空的Textview或其子类
         * @return 是否继续检查剩余的View，true则继续
         */
        fun onCheckEmpty(view: TextView): Boolean

        /**
         * 当检查完所有view都不为空或者有为空但返回true时回调
         * 注意vals的长度
         *
         * @param vals 按顺序获取的Textview或其子类的值的集合
         */
        fun onResult(vals: List<String>)
    }

}