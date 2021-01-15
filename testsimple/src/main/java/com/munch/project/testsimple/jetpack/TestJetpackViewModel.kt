package com.munch.project.testsimple.jetpack

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.map
import com.munch.lib.log
import com.munch.project.testsimple.jetpack.model.bean.ArticleBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel作为直接与UI通信的组件
 * 其主要任务是处理数据，包括获取、转换、判断数据、切换线程等
 *
 * 此处主要是将从[repository]中获取的数据内容进行转换，类型也从[flow]转为了[MutableList]
 *
 * Create by munch1182 on 2020/12/19 15:07.
 */
@ExperimentalPagingApi
class TestJetpackViewModel @ViewModelInject constructor(private val repository: ArticleRepository) :
    ViewModel() {

    val articleLiveData = MutableLiveData<PagingData<ArticleBean>>()
    val isLoading = MutableLiveData(false)
    private var error = false

    init {
        request()
    }

    fun judgeError2Refresh() {
        if (!error) {
            return
        }
        error = false
        request()
    }

    private fun request() {
        viewModelScope.launch {
            isLoading.postValue(true)
            repository.getArticleListToday()
                .map {
                    /*用以测试异常*/
                    /*if (index == 0) {
                        index++
                        throw Exception("test")
                    }*/
                    //此处的异常是PagingData内部的flow，其中的异常无法外部catch，需要自行try-catch
                    it.map map2@{ art ->
                        return@map2 art.convert()
                    }
                }
                .flowOn(Dispatchers.IO)
                //这个catch并不会跟adapter的LoadState.Error联动，因此此处需要自行跟UI联动
                .catch { c ->
                    error = true
                    c.printStackTrace()
                    log(c.message)
                    //emit保证报错后仍然返回状态
                    //因为empty里的receiver是NOOP_RECEIVER，所以一旦报错，刷新就不能再触发repository的操作
                    //因此需要judgeError2Refresh方法
                    emit(PagingData.empty())
                }
                .collectLatest {
                    isLoading.postValue(false)
                    articleLiveData.postValue(it)
                }
        }
    }
}