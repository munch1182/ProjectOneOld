package com.munch.project.one.record

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.munch.lib.DBRecord
import cn.munch.lib.record.Record
import com.munch.lib.extend.toLive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/4/20 13:41.
 */
class RecordVM : ViewModel() {

    private val dao = DBRecord
    private val _uiState = MutableLiveData<UIState>(UIState.Querying)
    val uiState = _uiState.toLive()
    private val userIntent = MutableSharedFlow<QueryIntent>()
    private var change = Change(RecordQuery(), "dbRecord", 0)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            userIntent.collect {
                when (it) {
                    QueryIntent.Clear -> clear()
                    is QueryIntent.Query -> change.query = it.query
                    is QueryIntent.Del -> del(it.record)
                }
                query()
            }
        }
    }


    private suspend fun query() {
        val it = change.query
        val list = dao.query(it.type, it.like, it.time, it.page, it.size)
        change.count = list.size
        _uiState.postValue(UIState.Data(change, list))
    }

    fun dispatch(intent: QueryIntent) {
        viewModelScope.launch { userIntent.emit(intent) }
    }

    private suspend fun del(r: Record?) {
        r ?: return
        dao.del(r)
    }


    private suspend fun clear() {
        dao.clear()
    }

    init {
        viewModelScope.launch { query() }
    }
}

