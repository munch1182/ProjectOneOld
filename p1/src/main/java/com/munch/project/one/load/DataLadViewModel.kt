package com.munch.project.one.load

import androidx.lifecycle.ViewModel
import com.munch.lib.base.toImmutable
import com.munch.project.one.load.net.Net
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Create by munch1182 on 2021/9/16 11:22.
 */
class DataLadViewModel : ViewModel() {

    private val dataLoadFlow = MutableStateFlow(UILoadState.Loading)

    val dataLoad = dataLoadFlow.toImmutable()

    //因为只需要在这里使用
    private val net = Net()

    init {
        net.server.queryArticle2Flow(0)
    }
}