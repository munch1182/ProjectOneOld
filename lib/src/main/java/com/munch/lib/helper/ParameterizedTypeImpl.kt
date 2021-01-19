package com.munch.lib.helper

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 通过Type手动构建一个ParameterizedType
 *
 * Create by munch1182 on 2021/1/19 18:01.
 */
class ParameterizedTypeImpl(
    private val rawType: Type,
    private vararg val typeArguments: Type,
    private val ownerType: Type? = null,
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

    override fun toString(): String {
        return "ParameterizedTypeImpl(rawType = $rawType, typeArguments = $typeArguments,ownerType = $ownerType, )"
    }
}