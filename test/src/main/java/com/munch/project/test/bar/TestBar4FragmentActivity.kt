package com.munch.project.test.bar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.munch.lib.helper.BarHelper
import com.munch.lib.helper.LogLog
import com.munch.lib.log
import com.munch.project.test.BaseActivity
import com.munch.project.test.BaseFragment
import com.munch.project.test.R
import com.munch.project.test.bar.TestBar4FragmentActivity.ViewPagerAdapter.Companion.COUNT_FRAGMENT

/**
 * 实际开发中，应保持statusbar的一致性
 *
 * 若要左右滑动时statusbar分别显示不同的效果，可以隐藏statusbar，然后自定义view实现
 * 在viewpager中不同fragment更改activity的statusbar的方式，有些手机高度不会自行调整，有的会闪烁
 *
 * Create by munch1182 on 2020/12/13 13:25.
 */
class TestBar4FragmentActivity : BaseActivity() {

    private val tabLayout by lazy { findViewById<TabLayout>(R.id.test_bar_tab_layout) }
    private val viewPager by lazy { findViewById<ViewPager2>(R.id.test_bar_view_pager) }
    private val helper by lazy { BarHelper(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_bar_for_fragment)

        viewPager.adapter = ViewPagerAdapter(this)
        TabLayoutMediator(tabLayout, viewPager, false, true) { tab, pos ->
            tab.text = "ITEM ${pos + 1}"
        }.attach()
        viewPager.offscreenPageLimit = 1
        helper.colorStatusBarByRes(R.color.colorPrimaryDark)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val pos = tab?.position ?: return
                log(pos)
                helper.hideStatusBar(pos >= 2)
                when (pos) {
                    0 -> {
                        helper.colorStatusBarByRes(R.color.colorPrimaryDark)
                    }
                    1 -> {
                        helper.colorStatusBarByRes(R.color.colorPrimary)
                    }
                    2, 4 -> {
                        helper.colorStatusBar(Color.TRANSPARENT)
                    }
                    3 -> {
                        helper.colorStatusBar(Color.parseColor("#66000000"))
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        companion object {
            const val COUNT_FRAGMENT = 5
        }

        override fun getItemCount() = COUNT_FRAGMENT

        override fun createFragment(position: Int): Fragment {
            return TestBarFragment.newFragment(position)
        }
    }

    class TestBarFragment : BaseFragment() {

        companion object {

            private const val KEY_POS = "KEY_POS"

            fun newFragment(pos: Int): TestBarFragment {
                return TestBarFragment().apply {
                    arguments = Bundle().apply {
                        putInt(KEY_POS, pos)
                    }
                }
            }
        }

        private var pos: Int = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            LogLog.log("testbar onCreate：${pos}")
            pos = arguments?.getInt(KEY_POS) ?: 0
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            LogLog.log("testbar onCreateView：${pos}")
            val layout =
                if (pos == COUNT_FRAGMENT - 1) R.layout.fragment_test_bar2 else R.layout.fragment_test_bar1
            return inflater.inflate(layout, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            LogLog.log("testbar onViewCreated：${pos}")
            val toolbar = view.findViewById<Toolbar>(R.id.test_bar_tb)
            if (pos <= 1) {
                (toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags =
                    AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
            } else if (pos >= 2) {
                val params = toolbar.layoutParams ?: return
                val height = BarHelper.getStatusBarHeight(this.context ?: return) ?: 0
                params.height += height
                toolbar.layoutParams = params
                toolbar.setPadding(
                    toolbar.paddingStart,
                    toolbar.paddingTop + height,
                    toolbar.paddingEnd,
                    toolbar.paddingBottom
                )
            }
            if (pos != COUNT_FRAGMENT - 1) {
                toolbar.title = "FRAGMENT $pos"
                toolbar.setTitleTextColor(Color.WHITE)
            }
        }

        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            LogLog.log("testbar onActivityCreated：${pos}")
        }

        override fun onResume() {
            super.onResume()
            LogLog.log("testbar onResume：${pos}")
        }

        override fun onPause() {
            super.onPause()
            LogLog.log("testbar onPause：${pos}")
        }

        override fun onStart() {
            super.onStart()
            LogLog.log("testbar onStart：${pos}")
        }

        override fun onStop() {
            super.onStop()
            LogLog.log("testbar onStop：${pos}")
        }

        override fun onDestroyView() {
            super.onDestroyView()
            LogLog.log("testbar onDestroyView：${pos}")
        }

        override fun onDestroy() {
            super.onDestroy()
            LogLog.log("testbar onDestroy：${pos}")
        }
    }
}