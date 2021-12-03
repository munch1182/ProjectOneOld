package com.munch.lib

/**
 * 用于统一自定义声明，因为声明代码少，所以集中放置此文件中
 * Create by munch1182 on 2021/8/5 14:45.
 */

/**
 * 用于声明被声明对象处于开发中、未完成的状态
 * 即：当看见此声明时，被声明对象不应该被引用，且应该尽快完成
 */
@Retention(AnnotationRetention.SOURCE)
annotation class UnComplete

/**
 * 标记该方法或者对象为关键的实现
 */
@Retention(AnnotationRetention.SOURCE)
annotation class Imp