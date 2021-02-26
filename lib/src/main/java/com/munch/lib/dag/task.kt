package com.munch.lib.dag

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Create by munch1182 on 2021/2/25 16:30.
 */
interface Task {

    fun start(executor: Executor)

    val dispatcher: CoroutineDispatcher

    fun getPriority(): Int = 0

    val uniqueKey: Key

    fun dependsOn(): MutableList<Key>
}

/**
 * @param priority 当任务同级时，会按照优先级排序
 */
data class Key(val uniqueKey: String, val priority: Int = 0) {

    override fun hashCode(): Int {
        return uniqueKey.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other is Key) {
            return uniqueKey == other.uniqueKey
        }
        return false
    }
}
