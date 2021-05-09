package com.munch.project.launcher

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.munch.pre.lib.base.BaseApp
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.databinding.ActivityMainBinding
import com.munch.project.launcher.extend.bind
import com.munch.project.launcher.item.AppActivity
import com.munch.project.launcher.calendar.CalendarFragment
import com.munch.project.launcher.home.HomeFragment
import com.munch.project.launcher.list.ListFragment
import kotlin.math.absoluteValue

/**
 * Create by munch1182 on 2021/5/8 10:18.
 */
class MainActivity : BaseActivity() {

    private val bind by bind<ActivityMainBinding>(R.layout.activity_main)
    private val fragmentAdapter by lazy { MainFSAdapter(this) }
    private val gestureDetector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private var handle = false
            override fun onDown(e: MotionEvent?): Boolean {
                handle = false
                return super.onDown(e)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (handle) {
                    return true
                }
                handle = true
                if (distanceY.absoluteValue > distanceX.absoluteValue && distanceY > 0 &&
                    bind.mainVp.currentItem == fragmentAdapter.homeItem()
                ) {
                    BaseApp.getInstance().getMainHandler()
                        .postDelayed({ AppActivity.start(this@MainActivity) }, 200L)
                }
                return true
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.mainVp.adapter = fragmentAdapter
        fragmentAdapter.showHomeItem(bind.mainVp)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //两个同时执行的话会有时会因为相邻条件而同时执行
        //但范围较小可以接受
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    override fun toggleTheme() {
        //不实现
        /*super.toggleTheme()*/
    }


    private class MainFSAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

        private val homeFragment = HomeFragment()

        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> homeFragment
                1 -> CalendarFragment()
                2 -> ListFragment()
                else -> homeFragment
            }
        }

        fun showHomeItem(vp: ViewPager2) {
            vp.setCurrentItem(homeItem(), false)
        }

        fun homeItem(): Int {
            return 0
        }
    }
}