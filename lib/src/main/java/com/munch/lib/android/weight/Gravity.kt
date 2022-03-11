package com.munch.lib.android.weight


import androidx.annotation.IntDef
import java.lang.annotation.Inherited

/**
 * Create by munch1182 on 2021/8/14 15:15.
 */
@IntDef(
    Gravity.START,
    Gravity.END,
    Gravity.TOP,
    Gravity.BOTTOM,
    Gravity.CENTER_HORIZONTAL,
    Gravity.CENTER_VERTICAL,
    Gravity.CENTER,
)
@Retention(AnnotationRetention.SOURCE)
@Inherited
annotation class Gravity {
    companion object {
        const val START = 1 shl 0
        const val END = 1 shl 1
        const val TOP = 1 shl 2
        const val BOTTOM = 1 shl 3

        const val CENTER_HORIZONTAL = START or END
        const val CENTER_VERTICAL = TOP or BOTTOM
        const val CENTER = CENTER_HORIZONTAL or CENTER_VERTICAL

        fun hasFlag(flags: Int, @Gravity flag: Int) = flags and flag == flag

        val all by lazy {
            intArrayOf(
                START or TOP,
                START or BOTTOM,
                START or CENTER_VERTICAL,
                END or TOP,
                END or BOTTOM,
                END or CENTER_VERTICAL,
                CENTER_HORIZONTAL or TOP,
                CENTER_HORIZONTAL or BOTTOM,
                CENTER
            )
        }
    }
}