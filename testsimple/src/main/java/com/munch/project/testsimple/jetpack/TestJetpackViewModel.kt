package com.munch.project.testsimple.jetpack

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.munch.project.testsimple.jetpack.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2020/12/19 15:07.
 */
class TestJetpackViewModel @ViewModelInject constructor(repository: ArticleRepository) :
    ViewModel() {

    val articleListLiveData = MutableLiveData<PagingData<Article>>()

    init {
        viewModelScope.launch(Dispatchers.Main) {
            repository.getArticleListToday()
                .flowOn(Dispatchers.IO)
                .catch { cause ->
                    cause.printStackTrace()
                }.collect {
                    articleListLiveData.postValue(it)
                }
        }
    }
}