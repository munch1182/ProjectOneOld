package com.munch.project.testsimple.jetpack

import androidx.databinding.ObservableBoolean
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
@FlowPreview
class TestJetpackViewModel @ViewModelInject constructor(repository: ArticleRepository) :
    ViewModel() {

    @Inject
    lateinit var api: Api

    val refresh: ObservableBoolean = ObservableBoolean(false)

    init {
        refresh.set(true)
        viewModelScope.launch(Dispatchers.Main) {
            repository.getArticleListToday()
                .flowOn(Dispatchers.IO)
                .catch {
                    refresh.set(false)
                }
                .collect {
                    refresh.set(false)
                }
        }
    }
}