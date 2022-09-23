package com.munch.lib.fast.view.dialog

import android.widget.LinearLayout
import com.munch.lib.android.extend.dp2Px
import com.munch.lib.android.extend.newCornerDrawable
import com.munch.lib.fast.view.base.ActivityHelper

/**
 * Create by munch1182 on 2022/9/20 11:48.
 */
object DialogHelper {

    fun message() = MessageDialog()

    /**
     * 仅提示消息的dialog
     */
    fun message(s: String) = message().message(s)

    /**
     * 从底部弹出dialog
     */
    fun bottom() = BottomAction2Dialog()
        .background(
            LinearLayout(ActivityHelper.curr!!).apply {
                orientation = LinearLayout.VERTICAL
                val dp8 = 8.dp2Px()
                background = newCornerDrawable(tl = dp8, tr = dp8)
            })
}