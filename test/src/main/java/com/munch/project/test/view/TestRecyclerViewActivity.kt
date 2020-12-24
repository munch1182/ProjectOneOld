package com.munch.project.test.view

import android.os.Bundle
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.test.R

/**
 * Create by munch1182 on 2020/12/24 17:24.
 */
class TestRecyclerViewActivity : TestBaseTopActivity() {

    private val letterNavigation: LetterNavigationBarView by lazy { findViewById(R.id.view_letter_navigation) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_recycler_view)
        letterNavigation.setAllLetters()
        letterNavigation.select("A")
    }
}