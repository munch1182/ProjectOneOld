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