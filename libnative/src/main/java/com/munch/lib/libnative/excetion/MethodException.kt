package com.munch.lib.libnative.excetion

/**
 * Created by Munch on 2019/7/26 14:30
 */
class MethodException(message: String?) : BException(false, message) {


    companion object {

        @JvmStatic
        fun unSupport(): MethodException {
            return MethodException("不支持这样调用")
        }

        @JvmStatic
        fun wrongParameter(): MethodException {
            return MethodException("参数错误")
        }
    }
}