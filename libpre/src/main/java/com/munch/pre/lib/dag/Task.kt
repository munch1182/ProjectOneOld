package com.munch.pre.lib.dag

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


/**
 * Create by munch1182 on 2021/4/1 17:28.
 */
abstract class Task {

    abstract fun start(executor: Executor)

    abstract val uniqueKey: String

    open fun dependsOn(): MutableList<String> = mutableListOf()

    /**
     * 指定此任务的执行线程
     */
    open val dispatcher: CoroutineDispatcher = Dispatchers.Unconfined

}