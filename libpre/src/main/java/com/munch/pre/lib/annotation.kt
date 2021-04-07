@file:Suppress("unused")

package com.munch.pre.lib

import kotlin.reflect.KClass

/**
 * Create by munch1182 on 2021/3/30 16:19.
 */

/**
 * 用于该目标标记未测试或者未测试完成
 *
 * 设计上不允许出现在正式版中
 */
@Retention(AnnotationRetention.SOURCE)
annotation class UNTESTED

/**
 * 用于标记未完成的设计
 */
@Retention(AnnotationRetention.SOURCE)
annotation class UNCOMPLETED(val need: String = "")

/**
 * 用于补充[androidx.annotation.RequiresPermission]，用于类
 */
@Retention(AnnotationRetention.SOURCE)
annotation class PERMISSIONS(
    val permission: String = "",
    val allOf: Array<String> = [],
    val anyOf: Array<String> = []
)

/**
 * 用于标记该类只是模板代码，使用时应该仿照该类根据变量完成自己的类
 * @param reason 模板的原因
 * @param variable 应该变化的部分
 */
@Retention(AnnotationRetention.SOURCE)
annotation class TEMPLATE(val reason: String = "", val variable: Array<String> = [])

/**
 * 用于标记该方法或者类因为功能或者设计有问题，并不实用或者效果并不好，但仍然作为思路被保留，应该尽量避免使用
 * 可以配合[Deprecated]使用
 */
@Retention(AnnotationRetention.SOURCE)
annotation class KEEP(val comment: String = "")

/**
 * 用于标记该方法是提醒性的，所以看上去该方法没有必要
 *
 * 即该方法看起来没什么实际用处，但是在逻辑上是需要的
 * 主要用于提示区别
 */
@Retention(AnnotationRetention.SOURCE)
annotation class ATTENTION

/**
 * 声明该对象默认依赖某些类来快速实现某些功能
 * 如果使用时没有依赖这些类，则应该自行实现这些功能
 */
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultDepend(val depend: Array<KClass<*>> = [])

/**
 * 如果该对象实际运用，应该注意其兼容性
 */
@Retention(AnnotationRetention.SOURCE)
annotation class COMPATIBILITY