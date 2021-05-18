package com.munch.test.project.one.thread

import com.munch.pre.lib.extend.obOnResume
import com.munch.pre.lib.log.Logger
import com.munch.test.project.one.base.BaseItemWithNoticeActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

/**
 * Create by munch1182 on 2021/5/18 9:51.
 */
class CoroutinesActivity : BaseItemWithNoticeActivity(), CoroutineScope {

    private val log = Logger().apply {
        obOnResume({ this.setListener { msg, _ -> notice(msg) } }, { this.setListener() })
        noStack = true
    }

    override fun clickItem(pos: Int) {
        when (pos) {
            0 -> repeat(4) {
                returnLastQuery()
            }
            1 -> testAwait()
        }
    }

    /**
     * await有缓存，第一次查询完成后会直接返回值
     */
    private fun testAwait() {
        launch {
            lock.withLock {
                if (async == null) {
                    async = newAsync()
                }
            }
            log.log(async?.await()?.toString())
        }
    }

    /**
     * 查询时若前一次查询未结束，则取消前一次查询
     * 多次查询只返回最后一次查询值
     */
    private fun returnLastQuery() {
        launch {
            val value = getNewAsync().await().toString()
            log.log("value: $value")
        }
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job
    private val lock = Mutex()

    private var async: Deferred<Int>? = null
    private var value = AtomicInteger(1)

    private suspend fun getNewAsync(): Deferred<Int> {
        return lock.withLock {
            if (async != null) {
                log.log("async cancel")
            }
            async?.cancel()
            async = newAsync()
            return@withLock async!!
        }
    }

    private fun newAsync(): Deferred<Int> {
        return async {
            log.log("start async query")
            delay(1000L)
            log.log("async queried")
            value.getAndIncrement()
        }
    }

    override fun getItem(): MutableList<String> {
        return mutableListOf("start async", "test await")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        log.setListener()
    }
}