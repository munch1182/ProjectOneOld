package com.munch.lib.weight.loading

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.munch.lib.extend.SealedClassToStringByName
import com.munch.lib.extend.layoutCenter
import com.munch.lib.extend.paddingHorizontal
import com.munch.lib.extend.paddingVertical
import com.munch.lib.weight.IColorView
import com.munch.lib.weight.R
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

class LoadingGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), IColorView, CoroutineScope {

    sealed class InitializeState : SealedClassToStringByName() {
        object Loading : InitializeState()
        object Child : InitializeState()
    }

    private var job = Job()
    private val loading = LoadingView(context, attrs, defStyleAttr, defStyleRes)
    private var initializeState: InitializeState = InitializeState.Loading
    private var onComplete: (() -> Unit)? = null
    private var loadingWidth = LayoutParams.WRAP_CONTENT
    private var loadingHeight = LayoutParams.WRAP_CONTENT

    init {
        addView(loading)
        context.obtainStyledAttributes(attrs, R.styleable.LoadingGroup).apply {
            initializeState =
                when (getInt(R.styleable.LoadingGroup_lg_initializeState, 0)) {
                    0 -> InitializeState.Child
                    else -> InitializeState.Loading
                }
            loadingWidth =
                getDimensionPixelOffset(R.styleable.LoadingGroup_lg_loading_width, loadingWidth)
            loadingHeight =
                getDimensionPixelOffset(R.styleable.LoadingGroup_lg_loading_width, loadingHeight)
        }.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxW = 0
        var maxH = 0
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val otherViewVisibility =
            if (initializeState == InitializeState.Loading) View.INVISIBLE else View.VISIBLE
        children.forEach {
            it.visibility = otherViewVisibility
            if (it != loading) {
                measureChild(
                    it,
                    MeasureSpec.makeMeasureSpec(w, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(h, MeasureSpec.UNSPECIFIED),
                )
            }
            maxW = max(maxW, it.measuredWidth)
            maxH = max(maxH, it.measuredHeight)
        }
        if (loadingWidth > 0 || loadingHeight > 0) {
            measureChild(
                loading,
                MeasureSpec.makeMeasureSpec(loadingWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(loadingHeight, MeasureSpec.AT_MOST)
            )
        } else {
            measureChild(loading, maxW, maxH)
        }
        loading.visibility =
            if (initializeState == InitializeState.Loading) View.VISIBLE else View.INVISIBLE
        setMeasuredDimension(maxW + paddingHorizontal(), maxH + paddingVertical())
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        children.forEach { it.layoutCenter(r - l, b - t) }
    }

    override fun setColor(color: Int) {
        children.forEach { if (it is IColorView) it.setColor(color) }
    }

    fun loading() = runBlocking(Dispatchers.Main) { switchShow(true) }

    fun loadComplete() = runBlocking(Dispatchers.Main) { switchShow(false) }

    fun load(f: suspend () -> Unit) {
        launch {
            loading()
            f.invoke()
            loadComplete()
        }
    }

    fun setOnLoadComplete(onComplete: (() -> Unit)? = null): LoadingGroup {
        this.onComplete = onComplete
        return this
    }

    private fun switchShow(showLoading: Boolean) {
        children.forEach { it.visibility = if (showLoading) View.INVISIBLE else View.VISIBLE }
        loading.visibility = if (showLoading) View.VISIBLE else View.INVISIBLE
        if (showLoading) {
            onComplete?.invoke()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        job = Job()
    }

    override fun onDetachedFromWindow() {
        job.cancel()
        super.onDetachedFromWindow()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

}