package com.munch.project.one.load

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.base.toImmutable
import com.munch.lib.log.log
import com.munch.project.one.load.net.Net
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/9/16 11:22.
 */
class DataLadViewModel : ViewModel() {

    private val dataLoadFlow = MutableStateFlow(UILoadState.Loading)

    val dataLoad = dataLoadFlow.toImmutable()

    //因为只需要在这里使用
    private val net = Net()

    init {
        viewModelScope.launch(Dispatchers.IO) {

            /* net.server.queryArticle2Flow(0)
                 .collect {
                 }*/
            val articles1 = net.server.queryArticleOnlyData(0, 1)
            log(articles1)
        }

    }
}