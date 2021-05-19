package com.munch.project.launcher

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.munch.project.launcher.base.BaseActivity
import com.munch.project.launcher.base.LauncherApp
import com.munch.project.launcher.calendar.CalendarFragment
import com.munch.project.launcher.databinding.ActivityMainBinding
import com.munch.project.launcher.extend.bind
import com.munch.project.launcher.home.HomeFragment
import com.munch.project.launcher.list.ListFragment

/**
 * Create by munch1182 on 2021/5/8 10:18.
 */
class MainActivity : BaseActivity() {

    private val bind by bind<ActivityMainBinding>(R.layout.activity_main)
    private val fragmentAdapter by lazy { MainFSAdapter(this) }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        LauncherApp.stopMeasureLaunch()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.lifecycleOwner = this
        bind.mainVp.adapter = fragmentAdapter
        fragmentAdapter.showHomeItem(bind.mainVp)
    }

    override fun toggleTheme() {
        //不实现
        /*super.toggleTheme()*/
    }

    override fun onBackPressed() {
        if (!isDefaultApp()) {
            super.onBackPressed()
        }
    }

    private fun isDefaultApp(): Boolean {
        val pm = packageManager
        var set = false
        val info = pm.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (info != null) {
            set = info.activityInfo.packageName == packageName
        }
        return set
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
            val homeItem = homeItem()
            if (homeItem != 0) {
                vp.setCurrentItem(homeItem, false)
            }
        }

        fun homeItem(): Int {
            return 0
        }
    }
}