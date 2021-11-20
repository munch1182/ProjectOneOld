package com.munch.lib.state

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
                loadingTarget?.layoutParams?.let { p ->
                    loadingView?.let { v ->
                        container.addView(v, p)
                    }
                }
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

}