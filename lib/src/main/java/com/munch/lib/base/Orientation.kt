package com.munch.lib.base

import androidx.annotation.IntDef

/**
 * Create by munch1182 on 2021/2/27 11:58.
 */
@IntDef(
    flag = true,
    value = [Orientation.HORIZONTAL, Orientation.VERTICAL,
        Orientation.LR, Orientation.RL,
        Orientation.TB, Orientation.BT,
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
        const val LR = 1 shl 1
        const val RL = 1 shl 2
        const val TB = 1 shl 3
        const val BT = 1 shl 4

        const val HORIZONTAL = LR or RL
        const val VERTICAL = TB or BT

        const val All = HORIZONTAL or VERTICAL
    }
}
