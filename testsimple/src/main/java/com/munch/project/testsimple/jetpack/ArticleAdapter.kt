package com.munch.project.testsimple.jetpack

import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.helper.dp2Px
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.LayoutArticleItemBinding
import com.munch.project.testsimple.databinding.LayoutArticleStateBinding
import com.munch.project.testsimple.jetpack.model.bean.ArticleBean

/**
 * Create by munch1182 on 2020/12/19 14:04.
 */
class ArticleAdapter :
    PagingDataAdapter<ArticleBean, BindViewHolder>(ArticleDiffCallBack()) {

    private var itemClick: ((pos: Int, adapter: PagingDataAdapter<ArticleBean, BindViewHolder>) -> Unit)? =
        null

    fun setOnItemClick(func: (pos: Int, adapter: PagingDataAdapter<ArticleBean, BindViewHolder>) -> Unit): ArticleAdapter {
        itemClick = func
        return this
    }

    override fun onBindViewHolder(holder: BindViewHolder, position: Int) {
        val article = getItem(position) ?: return
        holder.executeBinding<LayoutArticleItemBinding> {
            it.article = article
        }
        article.tags.takeIf { it.isEmpty() }?.forEach { tag ->
            val context = holder.itemView.context
            val dp8 = context.dp2Px(8f).toInt()
            holder.getBind<LayoutArticleItemBinding>().articleVgTags
                .addView(TextView(context).apply {
                    text = tag.name
                    setPadding(dp8, 0, dp8, 0)
                    textSize =
                        getContext().dp2Px(getContext().resources.getDimension(R.dimen.sp_smaller))
                })
        }
        itemClick ?: return
        holder.itemView.setOnClickListener {
            itemClick!!.invoke(position, this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder {
        return BindViewHolder(R.layout.layout_article_item, parent)
    }

    class ArticleDiffCallBack : DiffUtil.ItemCallback<ArticleBean>() {
        override fun areItemsTheSame(oldItem: ArticleBean, newItem: ArticleBean): Boolean =
            newItem.id == oldItem.id

        override fun areContentsTheSame(oldItem: ArticleBean, newItem: ArticleBean): Boolean =
            newItem.title == oldItem.title && newItem.link == oldItem.link
    }

    class ArticleStateAdapter(private val adapter: PagingDataAdapter<*, *>) :
        LoadStateAdapter<BindViewHolder>() {
        override fun onBindViewHolder(holder: BindViewHolder, loadState: LoadState) {
            holder.getBind<LayoutArticleStateBinding>().apply {
                when {
                    loadState.endOfPaginationReached -> {
                        this.articleTvState.text = "没有更多数据了"
                        this.articleTvState.setOnClickListener(null)
                    }
                    loadState is LoadState.Loading -> {
                        this.articleTvState.text = "正在加载中，请稍等"
                        this.articleTvState.setOnClickListener(null)
                    }
                    loadState is LoadState.Error -> {
                        this.articleTvState.text = "加载错误，请点击重试"
                        this.articleTvState.setOnClickListener {
                            adapter.retry()
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): BindViewHolder {
            return BindViewHolder(R.layout.layout_article_state, parent)
        }
    }
}