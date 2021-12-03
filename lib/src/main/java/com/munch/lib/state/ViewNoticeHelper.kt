package com.munch.lib.state

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.contains
import com.munch.lib.base.*
import com.munch.lib.dialog.IViewDialog
import com.munch.lib.task.pool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Create by munch1182 on 2021/11/20 14:57.
 */
class ViewNoticeHelper(
    private var init: ViewBean? = null,
    private var loading: ViewBean? = null,
    private var empty: ViewBean? = null,
    private var error: ViewBean? = null,
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
        init: Pair<View?, View?>? = null,
        loading: Pair<View?, View?>? = null,
        empty: Pair<View?, View?>? = null,
        error: Pair<View?, View?>? = null,
        dialog: IViewDialog? = null,
        nothing: IViewDialog? = null
    ) : this(
        init = ViewBean.toBeanNoCover(activity, init),
        loading = ViewBean.loading(activity, loading, dialog),
        empty = ViewBean.toBeanNoCover(activity, empty),
        error = ViewBean.toBeanNoCover(activity, error)
    )

    constructor(
        activity: AppCompatActivity,
        init: Pair<Int?, Int?>? = null,
        loading: Pair<Int?, Int?>? = null,
        empty: Pair<Int?, Int?>? = null,
        error: Pair<Int?, Int?>? = null,
        dialog: IViewDialog? = null
    ) : this(
        activity,
        init.toView(activity),
        loading.toView(activity),
        empty.toView(activity),
        error.toView(activity),
        dialog
    )

    private fun continueAnyViewIfNeed() {
    }

    private fun pauseAnyViewIfNeed() {
    }

    //<editor-fold desc="show">
    @UiThread
    fun init() {
        init?.showView()
    }

    @UiThread
    fun dismissInit() {
        init?.dismissView()
    }

    fun init(ing: suspend (v: View) -> Unit) {
        init?.show(ing)
    }

    @UiThread
    fun showLoading() {
        loading?.showView()
    }

    @UiThread
    fun dismissLoading() {
        loading?.dismissView()
    }

    fun loading(ing: suspend (v: View) -> Unit) {
        loading?.show(ing)
    }

    @UiThread
    fun showError() {
        error?.showView()
    }

    @UiThread
    fun dismissError() {
        error?.dismissView()
    }

    @UiThread
    fun showEmpty() {
        empty?.showView()
    }

    @UiThread
    fun dismissEmpty() {
        empty?.dismissView()
    }
    //</editor-fold>

    class ViewBean(
        activity: Activity? = null,
        var target: View? = null,
        var v: View? = null,
        //显示的view是否要覆盖目标view，否则应该使用类似dialog的带有层次的显示view
        private var needCover: Boolean = true,
        //该状态是否用dialog来显示
        private var dialog: IViewDialog? = null,
        //显示状态的最小显示时间，即调用了显示，但在该事件了又调用了完成显示，则会等到该时间完成才会完成显示
        private val minShowTime: Long = MIN_SHOW_TIME
    ) {

        companion object {

            fun toBeanNoCover(activity: AppCompatActivity, p: Pair<View?, View?>?): ViewBean? {
                p?.second ?: return null
                return ViewBean(activity, p.first, p.second)
            }

            fun loading(
                activity: AppCompatActivity,
                p: Pair<View?, View?>?,
                dialog: IViewDialog?
            ): ViewBean? {
                p?.second ?: return null
                return ViewBean(activity, p.first, p.second, false, dialog)
            }
        }

        init {
            v?.removeParent()
        }

        private val content: ViewGroup? = activity?.findViewById(android.R.id.content)

        private fun sureAdd() {
            val c = content ?: return
            val p = target ?: return
            val v = v ?: return
            if (c.contains(v)) {
                return
            }
            v.removeParent()
            val loc = IntArray(2)
            p.getLocationOnScreen(loc)
            val lp = FrameLayout.LayoutParams(ViewHelper.newWWLayoutParams())
            lp.leftMargin = loc[0]
            lp.topMargin = loc[1] + 30
            lp.gravity = Gravity.CENTER_HORIZONTAL
            c.addView(v, lp)
        }

        fun show(ing: suspend (v: View) -> Unit) {
            v ?: return
            pool {
                runBlocking {
                    withContext(Dispatchers.Main) { showView() }
                    val start = System.currentTimeMillis()
                    ing.invoke(v!!)
                    val end = System.currentTimeMillis()
                    val showTime = end - start
                    //显示loading的最小时间
                    if (showTime < minShowTime) {
                        delay(minShowTime - showTime)
                    }
                    withContext(Dispatchers.Main) { dismissView() }
                }
            }
        }

        @UiThread
        fun showView() {
            v ?: return
            sureAdd()
            if (needCover) {
                target?.invisible()
            }
            v?.visible()
        }

        @UiThread
        fun dismissView() {
            v ?: return
            target?.visible()
            v?.gone()
        }
    }
}