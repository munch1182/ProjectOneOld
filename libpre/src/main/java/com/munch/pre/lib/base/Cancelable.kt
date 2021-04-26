package com.munch.pre.lib.base

/**
 * 用于声明某些可以调用取消的类
 *
 * 适合于某些依赖其它状态影响的对象
 *
 * 注意：此声明为中断操作，而不是销毁操作
 *
 * @see Destroyable
 *
 * Create by munch1182 on 2021/4/26 14:47.
 */
interface Cancelable {

    fun cancel()
}

interface Destroyable {

    fun destroy()
}
