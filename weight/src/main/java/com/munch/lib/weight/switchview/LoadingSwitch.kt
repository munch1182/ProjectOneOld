package com.munch.lib.weight.switchview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import androidx.core.view.children
import com.munch.lib.extend.SealedClassToStringByName
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.extend.layoutCenter
import com.munch.lib.extend.paddingHorizontal
import com.munch.lib.extend.paddingVertical
import com.munch.lib.weight.IColorView
import com.munch.lib.weight.R
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.math.max

class LoadingSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes),
    IContext, IColorView, Checkable, CoroutineScope {

    sealed class InitializeState : SealedClassToStringByName() {
        object Loading : InitializeState()
        object Check : InitializeState()
    }

    private val switch = Switch(context, attrs, defStyleAttr, defStyleRes).apply {
        update { checkFrom = Switch.CheckType.Call }
    }
    private var preChecked = false
        set(value) {
            field = value
            preCheckUpdate()
        }
    private var onCheck: OnCheckListener? = null
    private var job = Job()
    private var initializeState: InitializeState = InitializeState.Check

    init {
        addView(switch)
        context.obtainStyledAttributes(attrs, R.styleable.LoadingSwitch).apply {
            initializeState =
                when (getInt(R.styleable.LoadingSwitch_loadingSwitch_initializeState, 0)) {
                    0 -> InitializeState.Check
                    else -> InitializeState.Loading
                }
        }.recycle()
    }

    private var isChecking = false
        get() = runBlocking { lock.withLock { field } }
        set(value) = runBlocking { lock.withLock { field = value } }
    private val lock = Mutex()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxW = 0
        var maxH = 0
        val otherViewVisibility =
            if (initializeState == InitializeState.Loading) View.VISIBLE else View.INVISIBLE
        children.forEach {
            it.visibility = otherViewVisibility
            measureChild(it, widthMeasureSpec, heightMeasureSpec)
            maxW = max(maxW, it.measuredWidth)
            maxH = max(maxH, it.measuredHeight)
        }
        switch.visibility =
            if (initializeState == InitializeState.Loading) View.INVISIBLE else View.VISIBLE
        setMeasuredDimension(maxW + paddingHorizontal(), maxH + paddingVertical())
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        children.forEach { it.layoutCenter(r - l, b - t) }
    }

    override fun setColor(color: Int) {
        children.forEach { if (it is IColorView) it.setColor(color) }
    }

    override fun setChecked(checked: Boolean) {
        switch.isChecked = checked
    }

    private fun setCheckedByCheck(checked: Boolean) {
        preChecked = checked
    }

    override fun isChecked(): Boolean {
        return switch.isChecked
    }

    override fun toggle() {
        isChecked = !isChecked
    }

    private fun preCheckUpdate() {
        if (isChecked == preChecked) return
        if (isChecking) return
        isChecking = true
        launch {
            var result = true
            if (onCheck != null) {
                switchView(false)
                result = onCheck!!.onCheckUpdate(preChecked)
                switchView(true)
            }
            isChecking = false
            if (!result) return@launch
            withContext(Dispatchers.Main) { switch.isChecked = preChecked }
        }
    }

    override fun performClick(): Boolean {
        setCheckedByCheck(!isChecked)
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        return true
    }

    private suspend fun switchView(isShowSwitch: Boolean) {
        withContext(Dispatchers.Main) {
            val show = if (isShowSwitch) View.INVISIBLE else View.VISIBLE
            children.forEach { it.visibility = show }
            switch.visibility = if (isShowSwitch) View.VISIBLE else View.INVISIBLE
        }
    }

    fun setOnCheck(onCheck: OnCheckListener): LoadingSwitch {
        this.onCheck = onCheck
        return this
    }

    fun interface OnCheckListener {

        suspend fun onCheckUpdate(checked: Boolean): Boolean
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        job = Job()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job.cancel()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    fun getSwitch() = switch
}