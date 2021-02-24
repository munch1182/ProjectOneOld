package com.munch.project.launcher.app

import com.munch.project.launcher.R
import com.munch.project.launcher.base.recyclerview.BaseBindAdapter
import com.munch.project.launcher.base.recyclerview.BaseBindViewHolder
import com.munch.project.launcher.databinding.ItemAppBeanBinding

/**
 * Create by munch1182 on 2021/2/24 11:13.
 */
class AppItemAdapter : BaseBindAdapter<AppShowBean, ItemAppBeanBinding>(R.layout.item_app_bean) {

    override fun onBind(
        holder: BaseBindViewHolder<ItemAppBeanBinding>,
        data: AppShowBean,
        position: Int
    ) {
        holder.binding.app = data
    }
}
