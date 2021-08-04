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
            2 -> composeScope()
        }
    }

    /**
     * 组合Scope
     */
    private fun composeScope() {
        val scope1 = CoroutineScope(Dispatchers.Default + CoroutineName("scope1"))
        val scope2 = CoroutineScope(Dispatchers.Default + CoroutineName("scope2"))
        val scope3 =
            CoroutineScope(scope1.coroutineContext + scope2.coroutineContext + CoroutineName("scope1+2"))

        val scope4 =
            CoroutineScope(scope2.coroutineContext + scope1.coroutineContext + CoroutineName("scope2+1"))
        log.log(scope1.coroutineContext)
        log.log(scope2.coroutineContext)
        launch {
            //scope1:1-3000L
            delay(3000L)
            scope1.cancel()
            //scope2:1-6000L
            delay(3000L)
            scope2.cancel()
        }
        launch {
            /*log.log("0ms,$scope3,$scope4")*/
            sureAlive(scope3, scope4)
            delay(3100L)
            /*log.log("3100ms,$scope3,$scope4")*/
            //1 cancel
            //1+2能执行，
            // 2+1不能执行
            // 因为组合的scope的job是使用右边的
            sureAlive(scope3, scope4)
            delay(6100L)
            /*log.log("6100L,$scope3,$scope4")*/
            sureAlive(scope3, scope4)
        }
    }

    private fun sureAlive(scope3: CoroutineScope, scope4: CoroutineScope) {
        scope3.launch { log.log("scope3 execute") }
        scope4.launch { log.log("scope4 execute") }
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
        return mutableListOf("start async", "test await", "compose scope")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        log.setListener()
    }
}