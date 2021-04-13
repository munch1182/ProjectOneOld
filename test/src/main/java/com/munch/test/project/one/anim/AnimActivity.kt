package com.munch.test.project.one.anim

import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseRvActivity

/**
 * Create by munch1182 on 2021/4/13 10:40.
 */
class AnimActivity : BaseRvActivity() {

    override fun next(bean: ItemClassBean, view: View) {
        /*super.next(bean, view)*/
        if (bean.target == null) {
            return
        }
        val name = getString(R.string.share_element_title)
        val tv = view.findViewById<TextView>(R.id.item_base_top_tv)
        ViewCompat.setTransitionName(tv, name)
        startActivity(
            Intent(this, bean.target),
            ActivityOptionsCompat.makeSceneTransitionAnimation(this, tv, name).toBundle()
        )
    }

    override fun getClassItem(): MutableList<ItemClassBean> {
        return ItemClassBean.newItems(
            ImageTitleActivity::class.java,
            SimpleTitleActivity::class.java,
            SimpleTitleActivity::class.java
        )
    }
}