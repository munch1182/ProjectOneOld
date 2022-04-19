package com.munch.lib.recyclerview

import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import com.munch.lib.extend.newMMLp
import com.munch.lib.extend.newMWLp

/**
 * Create by munch1182 on 2022/3/31 22:31.
 */
open class ViewAdapter : BaseRecyclerViewAdapter<Byte, BaseViewHolder>() {


    override fun onBind(holder: BaseViewHolder, position: Int, bean: Byte) {
    }

    fun show() {
        if (itemCount <= 0) {
            add(0)
        }
    }

    fun hide() {
        if (itemCount > 0) {
            remove(0)
        }
    }
}

class EmptyAdapter(private var emptyNotice: CharSequence? = null) : ViewAdapter() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(TextView(parent.context).apply {
            layoutParams = newMMLp()
            setPadding(16)
            gravity = Gravity.CENTER
            text = emptyNotice ?: "当前没有数据"
        })
    }
}

class RefreshAdapter : ViewAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(TextView(parent.context).apply {
            layoutParams = newMWLp()
            setPadding(16)
            gravity = Gravity.CENTER
            text = "正在刷新"
        })
    }

}

class LoadMoreAdapter : ViewAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return BaseViewHolder(TextView(parent.context).apply {
            layoutParams = newMWLp()
            gravity = Gravity.CENTER
            setPadding(16)
            text = "正在加载更多"
        })
    }

}