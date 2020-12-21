package com.munch.project.testsimple.jetpack

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.log
import com.munch.project.testsimple.R
import com.munch.project.testsimple.databinding.LayoutArticleItemBinding
import com.munch.project.testsimple.jetpack.model.Article

/**
 * Create by munch1182 on 2020/12/19 14:04.
 */
class ArticleAdapter :
    PagingDataAdapter<Article, BindViewHolder>(ArticleDiffCallBack()) {

    override fun onBindViewHolder(holder: BindViewHolder, position: Int) {
        log("pos:$position")
        holder.executeBinding<LayoutArticleItemBinding> {
            it.article = getItem(holder.absoluteAdapterPosition) ?: return
            log(it.article)
            /*article.tags.run {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder {
        log("onCreateViewHolder")
        return BindViewHolder(R.layout.layout_article_item, parent)
    }

    class ArticleDiffCallBack : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
            newItem.id == oldItem.id

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
            /*newItem.title == oldItem.title && newItem.link == oldItem.link*/false
    }
}