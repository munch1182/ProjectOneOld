package com.munch.project.testsimple.jetpack

import android.os.Bundle
import androidx.paging.ExperimentalPagingApi
import androidx.recyclerview.widget.DividerItemDecoration
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.TestSimpleActivityTestJetPackBinding
import com.munch.project.testsimple.jetpack.bind.getViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Create by munch1182 on 2020/12/17 15:29.
 */
@AndroidEntryPoint
class TestJetpackActivity : TestBaseTopActivity() {

    private val binding by bindingTop<TestSimpleActivityTestJetPackBinding>(R.layout.test_simple_activity_test_jet_pack)

    @ExperimentalPagingApi
    private val viewModel by getViewModel<TestJetpackViewModel>()

    @ExperimentalPagingApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val articleAdapter = ArticleAdapter().setOnItemClick { pos, adapter ->
            val articleBean = adapter.peek(pos) ?: return@setOnItemClick
            WebViewActivity.openWebView(this, articleBean.title, articleBean.link)
        }
        binding.lifecycleOwner = this@TestJetpackActivity
        binding.jetPackRv.run {
            //返回值是一个ConcatAdapter类型
            adapter =
                articleAdapter.withLoadStateFooter(ArticleAdapter.ArticleStateAdapter(articleAdapter))
            addItemDecoration(
                DividerItemDecoration(this@TestJetpackActivity, DividerItemDecoration.VERTICAL)
            )
        }
        viewModel.getArticleLiveData().observe(this) {
            articleAdapter.submitData(this@TestJetpackActivity.lifecycle, it)
        }
        //其实可以直接在页面绑定
        viewModel.isLoading().observe(this) {
            binding.jetPackSrl.isRefreshing = it
        }
        binding.jetPackSrl.setOnRefreshListener {
            viewModel.judgeError2Refresh()
            articleAdapter.refresh()
        }
    }

}