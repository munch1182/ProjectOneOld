package com.munch.project.one.dev

import com.munch.lib.fast.base.BaseBtnWithNoticeActivity
import com.munch.lib.fast.databinding.ItemSimpleBtnWithNoticeBinding

/**
 * Create by munch1182 on 2021/9/1 13:56.
 */
class TestFunActivity : BaseBtnWithNoticeActivity() {

    override fun getData(): MutableList<String?> {
        return mutableListOf("FUN1", "FUN2", "FUN3", "FUN4", "FUN5")
    }

    override fun onClick(pos: Int, bind: ItemSimpleBtnWithNoticeBinding) {
        super.onClick(pos, bind)
        when (pos) {
            0 -> {
            }
            1 -> {
            }
            2 -> {
            }
            3 -> {
            }
            4 -> {
            }
            else -> {
            }
        }
    }
}