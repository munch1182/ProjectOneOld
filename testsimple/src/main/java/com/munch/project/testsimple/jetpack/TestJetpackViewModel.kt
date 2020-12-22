package com.munch.project.testsimple.jetpack

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.munch.lib.UNCOMPLETE
import com.munch.project.testsimple.jetpack.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2020/12/19 15:07.
 */
@UNCOMPLETE
class TestJetpackViewModel @ViewModelInject constructor(private val repository: ArticleRepository) :
    ViewModel() {

    val articleLiveData = MutableLiveData<PagingData<Article>>()

    init {
        viewModelScope.launch {
            repository.getArticleListToday()
                .flowOn(Dispatchers.IO)
                /*.map {
                    throw Exception("error")
                    it
                }*/
                //这个catch并不会跟adapter的LoadState.Error联动，因此此处需要跟UI联动
                .catch { it.printStackTrace() }
                .collectLatest {
                    articleLiveData.postValue(it)
                }
        }
    }
}