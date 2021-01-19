package com.munch.lib

/**
 * Create by munch1182 on 2020/12/16 11:43.
 */

/**
 * 用于标记未测试
 */
annotation class UNTEST

/**
 * 用于标记未完成的设计
 */
annotation class UNCOMPLETE(val need: String = "")

/**
 * 用于补充[androidx.annotation.RequiresPermission]，用于类
 */
annotation class RequiresPermission(
    val permission: String = "",
    val permissions: Array<String> = []
)

/**
 * 用于标记测试项目中专用的方法或类
 */
annotation class TESTONLY(val reason: String = "")

/**
 * 用于标记该类只是模板代码，使用时应该仿照该类根据变量完成自己的类
 * @param reason 模板的原因
 * @param variable 应该变化的部分
 */
annotation class TEMPLATE(val reason: String = "", val variable: Array<String> = [])