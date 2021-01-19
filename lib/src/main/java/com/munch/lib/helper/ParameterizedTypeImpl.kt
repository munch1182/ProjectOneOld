package com.munch.lib.helper

import com.munch.lib.UNCOMPLETE
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 *
 * @see  "com.google.gson.reflect.TypeToken.getParameterized"
 *
 * Create by munch1182 on 2021/1/19 18:01.
 */
@UNCOMPLETE("未规范化")
class ParameterizedTypeImpl(
    private val ownerType: Type? = null,
    private val rawType: Type,
    private vararg val typeArguments: Type
) : ParameterizedType {
    override fun getActualTypeArguments(): Array<out Type> {
        return typeArguments
    }

    override fun getRawType(): Type {
        return rawType
    }

    override fun getOwnerType(): Type? {
        return ownerType
    }
}