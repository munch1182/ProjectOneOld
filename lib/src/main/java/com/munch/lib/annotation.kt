@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib

/**
 * Create by munch1182 on 2020/12/16 11:43.
 */

/**
 * 用于标记未测试
 */
@Retention(AnnotationRetention.SOURCE)
annotation class UNTEST

/**
 * 用于标记未完成的设计
 */
@Retention(AnnotationRetention.SOURCE)
annotation class UNCOMPLETE(val need: String = "")

/**
 * 用于补充[androidx.annotation.RequiresPermission]，用于类
 */
@Retention(AnnotationRetention.SOURCE)
annotation class RequiresPermission(
    val permission: String = "",
    val permissions: Array<String> = []
)

/**
 * 用于标记测试项目中专用的方法或类
 */
@Retention(AnnotationRetention.SOURCE)
annotation class TESTONLY(val reason: String = "")

/**
 * 用于标记该类只是模板代码，使用时应该仿照该类根据变量完成自己的类
 * @param reason 模板的原因
 * @param variable 应该变化的部分
 */
@Retention(AnnotationRetention.SOURCE)
annotation class TEMPLATE(val reason: String = "", val variable: Array<String> = [])

/**
 * 用于标记该方法或者类需要更多测试去验证实用性
 */
@Retention(AnnotationRetention.SOURCE)
annotation class NEEDUSE

/**
 * 用于标记该方法或者类因为功能或者设计有问题，并不实用或者效果并不好，但仍然作为思路被保留，应该尽量避免使用
 */
@Retention(AnnotationRetention.SOURCE)
annotation class KEEP(val comment: String = "")