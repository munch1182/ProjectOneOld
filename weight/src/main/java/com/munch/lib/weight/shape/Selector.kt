package com.munch.lib.weight.shape

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.view.children
import com.munch.lib.extend.icontext.IContext
import com.munch.lib.weight.ContainerLayout
import com.munch.lib.weight.FunctionalView
import com.munch.lib.weight.ITextView

class Selector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ContainerLayout(context, attrs, defStyleAttr), FunctionalView, IContext {

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val v = getView() ?: return

        collectShapeDrawable()?.let { v.background = it }
        collectColorDrawable()?.let { v.background = it }
        if (v is TextView) {
            collectTextColor()?.let { v.setTextColor(it) }
        } else if (v is ITextView) {
            collectTextColor()?.let { v.setTextColor(it) }
        }
    }

    private fun collectTextColor() = children.filterIsInstance<Color>()
        .map { it.updateDrawable()?.let { d -> if (d is Color.TextColorDrawable) d else null } }
        .filterNotNull()
        .takeIf { it.any() }
        ?.toList()
        ?.let { l ->
            ColorStateList(Array(l.size) { l[it].state }, IntArray(l.size) { l[it].color })
        }

    private fun collectColorDrawable(): StateListDrawable? = StateListDrawable().apply {
        children.filterIsInstance<Color>()
            .map { it.updateDrawable()?.let { d -> if (d is Color.TextColorDrawable) null else d } }
            .filterNotNull()
            .takeIf { it.any() }
            ?.forEach { addState(it.state, it) }
            ?: return null
    }

    private fun collectShapeDrawable(): StateListDrawable? = StateListDrawable().apply {
        children.filterIsInstance<Shape>()
            .map { it.updateDrawable() }
            .takeIf { it.any() }
            ?.forEach { addState(it.state, it) }
            ?: return null
    }

    private fun Int.toStateStr(): String {
        return when (this) {
            android.R.attr.state_enabled -> "stateEnabled"
            android.R.attr.state_checkable -> "stateCheckable"
            android.R.attr.state_checked -> "stateChecked"
            android.R.attr.state_selected -> "stateSelected"
            android.R.attr.state_pressed -> "statePressed"
            android.R.attr.state_activated -> "stateActivated"
            android.R.attr.state_active -> "stateActive"
            android.R.attr.state_single -> "stateSingle"
            android.R.attr.state_first -> "stateFirst"
            android.R.attr.state_middle -> "stateMiddle"
            android.R.attr.state_last -> "stateLast"
            android.R.attr.state_hovered -> "stateHovered"
            android.R.attr.state_accelerated -> "stateAccelerated"
            android.R.attr.state_drag_can_accept -> "stateDragCanAccept"
            android.R.attr.state_drag_hovered -> "stateDragHovered"
            else -> this.toString()
        }
    }
}