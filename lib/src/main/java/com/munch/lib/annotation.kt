package com.munch.lib

/**
 * Create by munch1182 on 2022/3/12 15:47.
 */

/**
 * 用于标注该类自带测试方法
 */
@Retention(AnnotationRetention.SOURCE)
annotation class Testable

@JvmInline
value class Priority(val priority: Int) {
    override fun toString() = priority.toString()
}

@JvmInline
value class Key(private val key: Int) {
    override fun toString() = key.toString()
}

/**
 * 重复执行处理策略
 */
sealed class RepeatStrategy {

    /**
     * 无视后续执行，只执行第一次的执行
     */
    object Ignore : RepeatStrategy()

    /**
     * 终止正在执行，替换成新的执行
     */
    object Replace : RepeatStrategy()
}