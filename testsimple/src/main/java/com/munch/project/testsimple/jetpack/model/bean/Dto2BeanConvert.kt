package com.munch.project.testsimple.jetpack.model.bean

/**
 * 数据的转换
 * 将后台接口返回的数据转换为自定义的bean类
 *
 * 如果后台接口返回的数据与前台显示的不符，或者后台接口经常变动
 * 可以考虑自定义bean类然后进行转换来与页面显示解耦
 *
 * 一般开发可以避免繁琐无需此步骤
 *
 * Create by munch1182 on 2021/1/8 14:00.
 */
interface Dto2BeanConvert<BEAN> {

    fun convert(): BEAN
}