package com.munch.test.project.one.player

import com.munch.test.project.one.base.BaseRvActivity
import com.munch.test.project.one.player.controller.MediaControllerActivity

/**
 * Create by munch1182 on 2021/6/1 9:32.
 */
class MediaActivity : BaseRvActivity() {
    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            IjkPlayerActivity::class.java,
            SoundActivity::class.java,
            MediaControllerActivity::class.java
        )
    }
}