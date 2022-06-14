package com.munch.project.one.record

import com.munch.lib.record.Record

/**
 * Create by munch1182 on 2022/5/21 16:59.
 */

internal sealed class RecordUIState {
    object Querying : RecordUIState()

    class Data(val query: Change, val data: List<Record>) : RecordUIState()

    class Error(val e: Exception) : RecordUIState()
}

internal sealed class QueryIntent {

    class Query(val query: RecordQuery) : QueryIntent() {

        override fun toString() = query.toString()
    }

    class Del(val record: Record) : QueryIntent()
    object Clear : QueryIntent()
}

data class Change(var query: RecordQuery, var dbName: String, var count: Int)

data class RecordQuery(
    var type: Int = -1,
    var time: Long = 0,
    var like: String = "",
    var page: Int = 0,
    var size: Int = 15
)