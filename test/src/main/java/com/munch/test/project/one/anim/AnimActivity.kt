package com.munch.test.project.one.anim

import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        //1. 给未设置ShareElement的元素设置动画
        window.exitTransition = Slide(Gravity.START)
        super.onCreate(savedInstanceState)
    }

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
        return mutableListOf(
            ItemClassBean("荷塘月色", ImageTitleActivity::class.java),
            ItemClassBean.newItem(SimpleTitleActivity::class.java),
            ItemClassBean.newItem(SimpleTitleActivity::class.java)
        )
    }
}