package com.munch.lib.state

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.base.*
import com.munch.lib.dialog.IDialog
import com.munch.lib.task.pool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Create by munch1182 on 2021/11/20 14:57.
 */
class ViewNoticeHelper(
    activity: AppCompatActivity,
    private val loading: Pair<View?, View?>? = null,
    private val empty: Pair<View?, View?>? = null,
    private val error: Pair<View?, View?>? = null,
    private val dialog: IDialog? = null,
    //显示状态的最小显示时间，即调用了显示，但在该事件了又调用了完成显示，则会等到该时间完成才会完成显示
    private val minShowTime: Long = MIN_SHOW_TIME
) {

    companion object {

        private const val MIN_SHOW_TIME = 500L

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
    ) : this(activity, loading.toView(activity), empty.toView(activity), error.toView(activity)) {
        loadingParent = loadingView?.parent as? ViewGroup
    }

    private val container = activity.findViewById<FrameLayout>(android.R.id.content)
    private var loadingParent: ViewGroup? = null
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
        loadingTarget?.visible()
    }

    fun loading() {
        sureLoadViewAdded()
        loadingTarget?.invisible()
        loadingView?.visible()
    }

    private fun sureLoadViewAdded() {
        val loadV = loadingView ?: return
        loadingParent?.let {
            if (!it.contains(loadV)) {
                return@let null
            } else {
                return@let it
            }
        } ?: loadingTarget?.post {
            loadingTarget?.let { t ->
                loadingView?.let { v ->
                    v.removeParent()

                    val loc = IntArray(2)
                    t.getLocationOnScreen(loc)
                    val lp = FrameLayout.LayoutParams(ViewHelper.newWWLayoutParams())
                    lp.leftMargin = loc[0]
                    lp.topMargin = loc[1] + 30
                    lp.gravity = Gravity.CENTER_HORIZONTAL
                    loadingParent = container
                    container.addView(v, lp)
                }
            }
        }
    }

    /**
     * @param load 加载方法，会自动在此方法前隐藏[loadingTarget]并显示[loadingView]，并在该方法完成后显示[loadingTarget]并隐藏[loadingView]
     *
     * 注意：load执行在子线程，且需要阻塞整个线程
     */
    fun loading(load: suspend () -> Unit) {
        loading()
        val start = System.currentTimeMillis()
        pool {
            runBlocking {
                load.invoke()
                val end = System.currentTimeMillis()
                val showTime = end - start
                //显示loading的最小时间
                if (showTime < minShowTime) {
                    delay(minShowTime - showTime)
                }
                withContext(Dispatchers.Main) { loadingComplete() }
            }
        }
    }
}