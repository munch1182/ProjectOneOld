package com.munch.project.one.record

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.munch.lib.DBRecord
import cn.munch.lib.record.Record
import cn.munch.lib.record.RecordDao
import com.munch.lib.extend.toLive
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2022/4/20 13:41.
 */
class RecordVM : ViewModel() {

    companion object {
        const val TYPE_NORMAL = 0
        const val TYPE_READER = 1
    }

    private val dbFrom = MutableLiveData(TYPE_NORMAL)
    fun dbFrom() = dbFrom.toLive()
    private val record = MutableLiveData<List<Record>>(null)
    fun records() = record.toLive()
    private val count = MutableLiveData(0)
    fun count() = count.toLive()
    var query: RecordQuery = RecordQuery()
        private set
    private var reps: RecordDao? = DBRecord
    private var repsNormal = DBRecord

    //todo 更换未完成
    private var repsReader: RecordDao? = /*DBReader.getInstance(File("")).db.recordDao()*/null


    fun query() {
        viewModelScope.launch {
            record.postValue(reps?.query(query.type, query.like, query.time))
            count.postValue(reps?.querySize(query.type, query.like, query.time))
        }
    }

    fun del(r: Record?) {
        r ?: return
        viewModelScope.launch {
            reps?.del(r)
            query()
        }
    }

    fun changeFrom() {
        if (reps == repsNormal) {
            reps = repsReader
            dbFrom.postValue(TYPE_READER)
        } else {
            reps = repsNormal
            dbFrom.postValue(TYPE_NORMAL)
        }
        query()
    }

    init {
        query()
    }
}

data class RecordQuery(
    var type: Int = -1,
    var time: Long = 0,
    var like: String = ""
)