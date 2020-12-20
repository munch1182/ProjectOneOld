package com.munch.project.testsimple.jetpack

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.munch.lib.log
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.ActivityTestJetPackBinding
import com.munch.project.testsimple.jetpack.bind.binding
import com.munch.project.testsimple.jetpack.bind.bindingTop
import dagger.hilt.android.AndroidEntryPoint

/**
 * Create by munch1182 on 2020/12/17 15:29.
 */
@AndroidEntryPoint
class TestJetpackActivity : TestBaseTopActivity() {

    private val binding by bindingTop<ActivityTestJetPackBinding>(R.layout.activity_test_jet_pack)
    private val viewModel by lazy { ViewModelProvider(this).get(TestJetpackViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            lifecycleOwner = this@TestJetpackActivity
            adapter = ArticleAdapter()
            vm = viewModel
        }
    }

}