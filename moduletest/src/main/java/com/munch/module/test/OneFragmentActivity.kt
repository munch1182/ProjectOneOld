package com.munch.module.test

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.munch.lib.libnative.root.RootActivity
import com.munch.module.test.fragments.*

/**
 * Created by Munch on 2019/7/13 17:02
 */
class OneFragmentActivity : RootActivity() {

    var tb: TabLayout? = null
    var pos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_fragment)

        tb = findViewById<TabLayout>(R.id.tb)

        tb!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                hideFragment()
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                showFragment()
            }
        })
    }

    private fun hideFragment() {
        val fg = supportFragmentManager.findFragmentByTag(tb?.selectedTabPosition.toString())
            ?: when (tb?.selectedTabPosition) {
                0 -> Fragment1()
                1 -> Fragment2()
                2 -> Fragment3()
                3 -> Fragment4()
                4 -> Fragment5()
                else -> null
            }
        supportFragmentManager.beginTransaction()
            .hide(fg!!)
            .commit()
    }

    fun showFragment() {
        val fg = supportFragmentManager.findFragmentByTag(tb?.selectedTabPosition.toString())
            ?: when (tb?.selectedTabPosition) {
                0 -> Fragment1()
                1 -> Fragment2()
                2 -> Fragment3()
                3 -> Fragment4()
                4 -> Fragment5()
                else -> null
            }
        supportFragmentManager.beginTransaction()
            .add(R.id.fl, fg!!, tb?.selectedTabPosition?.toString())
            .commit()
    }
}