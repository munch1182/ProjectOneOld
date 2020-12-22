package com.munch.project.testsimple.jetpack

import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.helper.dp2Px
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.LayoutArticleItemBinding
import com.munch.project.testsimple.jetpack.model.Article

/**
 * Create by munch1182 on 2020/12/19 14:04.
 */
class ArticleAdapter :
    PagingDataAdapter<Article, BindViewHolder>(ArticleDiffCallBack()) {

    override fun onBindViewHolder(holder: BindViewHolder, position: Int) {
        val article = getItem(holder.absoluteAdapterPosition) ?: return
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder {
        return BindViewHolder(R.layout.layout_article_item, parent)
    }

    class ArticleDiffCallBack : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
            newItem.id == oldItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
            newItem.title == oldItem.title && newItem.link == oldItem.link
    }
}