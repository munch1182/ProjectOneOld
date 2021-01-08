package com.munch.project.testsimple.jetpack

import android.os.Bundle
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.ActivityTestJetPackBinding
import com.munch.project.testsimple.jetpack.bind.bindingTop
import com.munch.project.testsimple.jetpack.bind.getViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Create by munch1182 on 2020/12/17 15:29.
 */
@AndroidEntryPoint
class TestJetpackActivity : TestBaseTopActivity() {

    private val binding by bindingTop<ActivityTestJetPackBinding>(R.layout.activity_test_jet_pack)
    private val viewModel by getViewModel<TestJetpackViewModel>()
    private var refreshByHand = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val articleAdapter = ArticleAdapter().setOnItemClick { pos, adapter ->
            val articleBean = adapter.peek(pos) ?: return@setOnItemClick
            WebViewActivity.openWebView(this, articleBean.title, articleBean.link)

        }
        binding.apply {
            lifecycleOwner = this@TestJetpackActivity
            adapter = articleAdapter
        }
        binding.jetPackRv.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        viewModel.articleLiveData.observe(this) {
            articleAdapter.submitData(this@TestJetpackActivity.lifecycle, it)
        }
        //其实可以直接在页面绑定
        viewModel.isLoading.observe(this) {
            binding.jetPackSrl.isRefreshing = it
        }
        binding.jetPackSrl.setOnRefreshListener {
            articleAdapter.refresh()
            refreshByHand = true
        }
        articleAdapter.addLoadStateListener {
            when (it.refresh) {
                is LoadState.Loading -> {
                    if (refreshByHand) {
                        return@addLoadStateListener
                    }
                    binding.jetPackSrl.isRefreshing = true
                }
                is LoadState.Error -> binding.jetPackSrl.isRefreshing = false
                is LoadState.NotLoading -> binding.jetPackSrl.isRefreshing = false
            }
            refreshByHand = false
        }
    }

}