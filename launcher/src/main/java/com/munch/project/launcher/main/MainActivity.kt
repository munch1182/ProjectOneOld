package com.munch.project.launcher.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.munch.project.launcher.R
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityMainBinding
import com.munch.project.launcher.extend.bind

/**
 * Create by munch1182 on 2021/5/8 10:18.
 */
class MainActivity : BaseActivity() {

    private val bind by bind<ActivityMainBinding>(R.layout.activity_main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.mainVp.adapter = fragmentAdapter
        fragmentAdapter.showHomeItem(bind.mainVp)
    }

    override fun toggleTheme() {
        //需要单独设置
        /*super.toggleTheme()*/
    }

    private val fragmentAdapter = object : FragmentStateAdapter(this) {

        private val homeFragment: HomeFragment = HomeFragment()

        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                CalendarFragment()
            } else {
                homeFragment
            }
        }

        fun showHomeItem(vp: ViewPager2) {
            vp.currentItem = itemCount - 1
        }
    }
}