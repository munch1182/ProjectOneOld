package com.munch.lib.task

import androidx.annotation.IntDef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import java.security.Key
import java.util.*
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2021/3/13 14:49.
 */
class TaskDispatcher : CoroutineScope {

    private val tasks = LinkedList<Task<*, *>>()
    private val finishedTasks = mutableListOf<Task<*, *>>()
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    suspend fun <D, N> add(task: Task<D, N>) {
        coroutineScope {
            tasks.add(task)
        }
    }

    fun dumpTask() {
        tasks.forEach {
            if (it.dependsKeys().isNotEmpty()) {
                it.dependsKeys().forEach { t, u ->  }
            }
        }
    }

    private data class TaskStatus(
        val key: Key,
        @State var state: Int = State.WAITING,
        val result: Any? = null
    ) {

        fun update2Start() {
            state = State.START
        }

        fun update2Executing() {
            state = State.EXECUTING
        }

        fun update2Executed() {
            state = State.EXECUTED
        }

        fun update2Waiting() {
            state = State.WAITING
        }
    }

    @IntDef(State.START, State.EXECUTING, State.EXECUTED, State.WAITING)
    @Retention(AnnotationRetention.SOURCE)
    annotation class State {
        companion object {
            const val START = 0
            const val EXECUTING = 1
            const val EXECUTED = 2
            const val WAITING = 3
        }
    }

}