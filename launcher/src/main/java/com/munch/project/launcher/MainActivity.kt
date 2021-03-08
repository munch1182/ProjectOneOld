package com.munch.project.launcher

import android.graphics.Color
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.munch.lib.helper.AppHelper
import com.munch.project.launcher.appitem.AppActivity
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.base.BaseFragment
import com.munch.project.launcher.databinding.ActivityMainBinding
import com.munch.project.launcher.main.CalendarFragment
import com.munch.project.launcher.main.HomeFragment

/**
 * Create by munch1182 on 2021/2/23 14:41.
 */
class MainActivity : BaseActivity() {

    private val bind by bind<ActivityMainBinding>(R.layout.activity_main)
    private val fragmentsAdapter by lazy { MainFragmentAdapter() }
    private val gesture by lazy {
        GestureDetector(this, object :
            GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                e1 ?: return false
                e2 ?: return false
                return if (e2.y - e1.y <= -300f) {
                    startAppActivity()
                    true
                } else {
                    false
                }
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                AppHelper.lock(this@MainActivity)
                return true
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this

        bind.mainVp.adapter = fragmentsAdapter
        bind.mainVp.setCurrentItem(fragmentsAdapter.getHomeFragmentPos(), false)
    }

    internal fun startAppActivity() {
        AppActivity.start(this)
    }

    override fun setPage(view: View) {
        super.setPage(view)
        window.navigationBarColor = Color.TRANSPARENT
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        gesture.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun onBackPressed() {
        /*屏蔽返回*/
        /*super.onBackPressed()*/
    }

    inner class MainFragmentAdapter : FragmentStateAdapter(this) {

        fun getHomeFragmentPos(): Int {
            return mainFragments.indexOf(homeFragment)
        }

        private val homeFragment = HomeFragment()
        private val calendarFragment = CalendarFragment()

        private val mainFragments = mutableListOf<BaseFragment<*>>(calendarFragment, homeFragment)

        override fun getItemCount(): Int = mainFragments.size

        override fun createFragment(position: Int): Fragment {
            return mainFragments[position]
        }
    }
}