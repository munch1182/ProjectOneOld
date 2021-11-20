package com.munch.lib.state

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.base.ViewHelper
import com.munch.lib.base.gone
import com.munch.lib.base.removeParent
import com.munch.lib.base.visible
import com.munch.lib.dialog.IDialog

/**
 * Create by munch1182 on 2021/11/20 14:57.
 */
class ViewNoticeHelper(
    activity: AppCompatActivity,
    private val loading: Pair<View?, View?>? = null,
    private val empty: Pair<View?, View?>? = null,
    private val error: Pair<View?, View?>? = null,
    private val dialog: IDialog? = null
) {

    companion object {
        private fun Pair<Int?, Int?>?.toView(activity: AppCompatActivity): Pair<View?, View?>? {
            this ?: return null
            return first?.let { activity.findViewById<View>(it) } to
                    second?.let { activity.findViewById(it) }

        }
    }

    constructor(
        activity: AppCompatActivity,
        loading: Pair<Int?, Int?>? = null,
        empty: Pair<Int?, Int?>? = null,
        error: Pair<Int?, Int?>? = null
    ) : this(activity, loading.toView(activity), empty.toView(activity), error.toView(activity))

    private val container = activity.findViewById<FrameLayout>(android.R.id.content)
    private val loadingTarget: View?
        get() = loading?.first
    private val loadingView: View?
        get() = loading?.second
    private val emptyTarget: View?
        get() = empty?.first
    private val emptyView: View?
        get() = empty?.second
    private val errorTarget: View?
        get() = error?.first
    private val errorView: View?
        get() = error?.second

    init {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                continueAnyViewIfNeed()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                pauseAnyViewIfNeed()
            }
        })
    }

    private fun continueAnyViewIfNeed() {
    }

    private fun pauseAnyViewIfNeed() {
    }

    fun loadingComplete() {
        loadingView?.gone()
    }

    fun loading() {
        loadingView?.let {
            if (container.contains(it)) {
                it.visible()
                return@let null
            } else {
                return@let it
            }
        } ?: return
        loadingTarget?.post {
            loadingTarget?.let { t ->
                loadingView?.let { v ->
                    v.removeParent()

                    val loc = IntArray(2)
                    t.getLocationOnScreen(loc)
                    val lp = FrameLayout.LayoutParams(ViewHelper.newWWLayoutParams())
                    lp.leftMargin = loc[0]
                    lp.topMargin = loc[1] + t.height / 3
                    lp.gravity = Gravity.CENTER_HORIZONTAL
                    container.addView(v, lp)
                }
            }
        }
    }
}