package com.munch.lib.base

import androidx.annotation.IntDef

/**
 * Create by munch1182 on 2021/2/27 11:58.
 */
@IntDef(
    flag = true,
    value = [Orientation.HORIZONTAL, Orientation.VERTICAL,
        Orientation.LEFT_2_RIGHT, Orientation.RIGHT_2_LEFT,
        Orientation.TOP_2_BOTTOM, Orientation.BOTTOM_2_TOP,
        Orientation.All]
)
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPE
)
annotation class Orientation {
    companion object {
        const val LEFT_2_RIGHT = 1 shl 1
        const val RIGHT_2_LEFT = 1 shl 2
        const val TOP_2_BOTTOM = 1 shl 3
        const val BOTTOM_2_TOP = 1 shl 4

        const val HORIZONTAL = LEFT_2_RIGHT or RIGHT_2_LEFT
        const val VERTICAL = TOP_2_BOTTOM or BOTTOM_2_TOP

        const val All = HORIZONTAL or VERTICAL
    }
}
