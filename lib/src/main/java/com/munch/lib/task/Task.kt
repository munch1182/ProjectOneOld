package com.munch.lib.task

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Create by munch1182 on 2021/3/13 14:49.
 */
interface Task<D, N> {

    /**
     * 此任务执行的方法
     *
     * 只有当此任务执行完毕，才会执行下一个任务，
     *
     * @param depend 上一个任务根据其执行情况传递的值
     */
    fun start(depend: MutableMap<String, D>?) {}

    /**
     * 给下一个任务传递的值，其会传递到下一个任务的[start]方法中
     *
     * 注意：传递的只是值，不会传递引用
     */
    fun next(): N? = null

    /**
     * 调度器，声明执行的线程
     */
    val dispatcher: CoroutineDispatcher

    /**
     * 优先级，无依赖或者同一依赖下的先后执行顺序
     */
    fun getPriority(): Int = 0

    /**
     * 用于标识此任务，相同标识的任务会被认为是同一任务
     */
    val uniqueKey: String

    /**
     * 此任务所依赖的任务的key以及设定的集合，如果无依赖，返回空集合
     *
     * 该map的string类型的key为依赖的任务的uniqueKey
     * 该map的Boolean类型的value用以设计判断条件：执行此任务时该依赖任务是否必须已经执行
     *
     * 如果不要求必须已经执行，则会跳过检查直接执行此任务
     * 如果要求必须已经执行，但执行此任务时会检查依赖任务：
     *      如果依赖任务不存在，则会跳过此任务
     *      如果依赖任务存在且未执行，则会等待依赖任务执行完毕后再执行此任务
     */
    fun dependsKeys(): MutableMap<String, Boolean> = mutableMapOf()
}