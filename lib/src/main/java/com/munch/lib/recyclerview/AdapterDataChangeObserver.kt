package com.munch.lib.recyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * Create by munch1182 on 2022/4/19 17:47.
 */
abstract class AdapterDataChangeObserver : RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
        super.onChanged()
        onDataChange()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        super.onItemRangeChanged(positionStart, itemCount)
        onDataChange()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        super.onItemRangeChanged(positionStart, itemCount, payload)
        onDataChange()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        onDataChange()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount)
        onDataChange()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        super.onItemRangeRemoved(positionStart, itemCount)
        onDataChange()
    }

    override fun onStateRestorationPolicyChanged() {
        super.onStateRestorationPolicyChanged()
    }

    protected abstract fun onDataChange()
}