package com.munch.project.launcher.app

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munch.lib.log
import kotlinx.coroutines.launch
import java.util.*

/**
 * Create by munch1182 on 2021/2/24 9:02.
 */
class AppViewModel @ViewModelInject constructor(private val repository: AppRepository) :
    ViewModel() {

    private val appList = MutableLiveData<MutableList<AppShowBean>>(null)
    fun getAppList(): LiveData<MutableList<AppShowBean>> = appList
    private var spanCount = MutableLiveData(4)
    fun getSpanCount(): LiveData<Int> = spanCount
    private var char: Char = ' '
    private var charIndex = -1

    init {
        viewModelScope.launch {
            val spanCount = spanCount.value!!
            val apps = repository.queryAppByScan()
                ?.map { AppShowBean.new(it) }
                ?.toMutableList()
                ?: return@launch
            apps.sort()
            val appsInSpace = mutableListOf<AppShowBean>()
            apps.forEach {
                if (it.latterChar != char && charIndex != -1) {
                    //给每一组最后一个位置添加一个占据剩余整行的空数据来占位置，需要GridLayoutManager.SpanSizeLookup配合
                    val i = spanCount - charIndex % spanCount - 1

                    if (i != 0) {
                        val parameter = ShowParameter(charIndex)
                        parameter.space2End = i
                        log(parameter.space2End)
                        //add
                        appsInSpace.add(AppShowBean.empty(char, parameter))
                    }

                    charIndex = 0
                    char = it.latterChar
                } else {
                    charIndex++
                }
                it.updateShowParameter(ShowParameter(charIndex))
                //add
                appsInSpace.add(it)
            }
            appList.postValue(appsInSpace)
        }
    }
}