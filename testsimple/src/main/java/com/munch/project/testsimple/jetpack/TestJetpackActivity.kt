package com.munch.project.testsimple.jetpack

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.ActivityTestJetPackBinding
import com.munch.project.testsimple.jetpack.bind.binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*

/**
 * Create by munch1182 on 2020/12/17 15:29.
 */
@AndroidEntryPoint
class TestJetpackActivity : TestBaseTopActivity() {

    private val binding by binding<ActivityTestJetPackBinding>(R.layout.activity_test_jet_pack)
    private val vm by lazy { ViewModelProvider(this).get(TestJetpackViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            lifecycleOwner = this@TestJetpackActivity
            adapter = ArticleAdapter()
            vm = this@TestJetpackActivity.vm
        }
    }

}