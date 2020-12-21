package com.munch.project.testsimple.jetpack

import androidx.databinding.ObservableBoolean
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingSource
import com.munch.lib.log
import com.munch.project.testsimple.jetpack.model.Article
import com.munch.project.testsimple.jetpack.net.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Create by munch1182 on 2020/12/19 15:07.
 */
class TestJetpackViewModel @ViewModelInject constructor(repository: ArticleRepository) :
    ViewModel() {

    val refresh: ObservableBoolean = ObservableBoolean(false)
    lateinit var pagingSource: PagingSource<Int, Article>

    init {
        refresh.set(true)
        viewModelScope.launch(Dispatchers.Main) {
            repository.getArticleListToday()
                .flowOn(Dispatchers.IO)
                .catch { cause ->
                    cause.printStackTrace()
                    refresh.set(false)
                }
                .collect {
                    log(111, it)
                    refresh.set(false)
                    pagingSource = it
                }
        }
    }
}