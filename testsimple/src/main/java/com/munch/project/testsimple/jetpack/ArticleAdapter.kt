package com.munch.project.testsimple.jetpack

import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.helper.dp2Px
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.LayoutArticleItemBinding
import com.munch.project.testsimple.jetpack.bind.binding
import com.munch.project.testsimple.jetpack.model.Article

/**
 * Create by munch1182 on 2020/12/19 14:04.
 */
class ArticleAdapter :
    PagingDataAdapter<Article, ArticleAdapter.ArticleViewHolder>(ArticleDiffCallBack()) {

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        getItem(holder.absoluteAdapterPosition)?.let {
            /*it.tags.run {
                if (isEmpty()) {
                    return@let
                }
                forEach { tag ->
                    val context = holder.itemView.context
                    val dp8 = context.dp2Px(8f).toInt()
                    holder.binding.articleVgTags.addView(TextView(context).apply {
                        text = tag.name
                        setPadding(dp8, 0, dp8, 0)
                    })
                }
            }*/
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(binding(parent, R.layout.layout_article_item))
    }

    class ArticleViewHolder(val binding: LayoutArticleItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ArticleDiffCallBack : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
            newItem.id == oldItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
            newItem.title == oldItem.title && newItem.link == oldItem.link
    }
}