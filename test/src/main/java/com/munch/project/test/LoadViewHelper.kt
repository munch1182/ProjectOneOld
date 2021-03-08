package com.munch.project.test

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.MainThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.helper.obWhenResume

/**
 * 用以控制[ILoading]的显示
 *
 * 主要工作在于将[ILoading]的view填充满[target]，然后控制生命周期和显示
 *
 * 主要调用方法为[bind]
 *
 * Create by munch1182 on 2021/2/23 17:11.
 */
class LoadViewHelper(
    private var target: ViewGroup? = null,
    private var view: ILoading = LoadViewProxy()
) {

    private val listVisibility4LinearLayout = arrayListOf<Pair<View, Int>>()
    private var currentIsLoading = false
    private var attached = false

    private fun attachTarget() {
        target ?: return
        if (attached && target!!.contains(view.getView())) {
            return
        }
        attached = true
        view.init(target!!.context)
        view.hide()
        val param = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        when (target) {
            is RelativeLayout, is FrameLayout -> {
                target!!.addView(view.getView(), param)
            }
            is ConstraintLayout -> {
                target!!.addView(view.getView(), ConstraintLayout.LayoutParams(param).apply {
                    startToStart = target!!.id
                    endToEnd = target!!.id
                    topToTop = target!!.id
                    bottomToBottom = target!!.id
                    constrainedHeight = true
                })
            }
            is LinearLayout -> {
                target!!.addView(view.getView(), param)
            }
            else -> throw UnsupportedOperationException("unsupported target view type")
        }
    }

    private fun detachTarget() {
        attached = false
        target?.removeView(view.getView())
        listVisibility4LinearLayout.clear()
    }

    /**
     * 主要用于没有使用构造初始化时设置[target]
     * 或者同一页面更改[target]
     */
    fun attachTarget(target: ViewGroup): LoadViewHelper {
        if (this.target != null) {
            detachTarget()
        }
        this.target = target
        attachTarget()
        return this
    }

    fun bind(owner: LifecycleOwner) {
        attachTarget()
        owner.obWhenResume(onResume = {
            if (currentIsLoading) {
                view.startLoading()
            }
        }, onPause = {
            view.stopLoading()
        }, onDestroy = {
            detachTarget()
        })
    }

    @MainThread
    fun startLoading() {
        target ?: return
        currentIsLoading = true
        if (target is LinearLayout) {
            listVisibility4LinearLayout.clear()
            var child: View
            for (i in 0 until target!!.childCount) {
                child = target!!.getChildAt(i) ?: continue
                listVisibility4LinearLayout.add(Pair(child, child.visibility))
                child.visibility = View.GONE
            }
        }
        view.show()
        view.startLoading()
    }

    @MainThread
    fun stopLoading() {
        if (!currentIsLoading) {
            return
        }
        currentIsLoading = false
        view.stopLoading()
        view.hide()
        if (target is LinearLayout && listVisibility4LinearLayout.isNotEmpty()) {
            listVisibility4LinearLayout.forEach {
                it.first.visibility = it.second
            }
        }
    }

    @MainThread
    fun startLoadingInTime(time: Long) {
        startLoading()
        target?.postDelayed({ stopLoading() }, time)
    }
}

interface ILoading {

    fun init(context: Context)

    fun getView(): View

    fun startLoading()

    fun stopLoading()

    fun show() {
        getView().visibility = View.VISIBLE
        startLoading()
    }

    fun hide() {
        getView().visibility = View.GONE
        stopLoading()
    }
}