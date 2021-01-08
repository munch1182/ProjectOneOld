package com.munch.lib.helper

/**
 * 只建议根据业务需求在关键操作处使用，一般跳转之类的操作没有这个必要
 *
 * Create by munch1182 on 2021/1/8 9:15.
 */
object FastClickHelper {

    private const val MAX_CAPACITY = 5
    private val records = HashMap<String, Long>(MAX_CAPACITY)
    private var MIN_OP_TIME_DURATION = 500L

    /**
     * 全局设置快速点击的最小间隔事件
     */
    fun setMinOpTimeDuration(duration: Long) {
        MIN_OP_TIME_DURATION = duration
    }

    /**
     * 使用调用的文件名+行号作为key放入hashMap来区分每次判断
     *
     * @param flag 用于recyclerview等同一位置点击不同事件的区分
     * @param index 如果直接调用，可不传或者传1，否则，每多一层包裹调用需要加1
     */
    fun isFast(flag: Int = 0, index: Int = 1): Boolean {
        if (records.size > MAX_CAPACITY) {
            records.clear()
        }
        val element =
            Throwable().stackTrace.takeIf { it.size >= index + 1 }?.get(index) ?: return false
        val key = "${element.fileName}${element.lineNumber}$flag"
        val currentTimeMillis = System.currentTimeMillis()
        return if (!records.containsKey(key)) {
            records[key] = currentTimeMillis
            false
        } else {
            val timeDuration = currentTimeMillis - (records[key] ?: 0L)
            val isFast = timeDuration in 0..MIN_OP_TIME_DURATION
            if (!isFast) {
                records[key] = currentTimeMillis
            }
            isFast
        }
    }
}

fun isFast(flag: Int = 0) = FastClickHelper.isFast(flag, 2)