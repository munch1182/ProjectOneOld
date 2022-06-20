package com.munch.lib.notice

import com.munch.lib.extend.catch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Created by munch1182 on 2022/5/28 2:38.
 */
/**
 * 从[Notice]中选择了[com.munch.lib.notice.Chose.Ok]，则返回true，否则返回false
 */
suspend fun Notice.choseSureOrFalse() = withContext(Dispatchers.Main) {
    catch {
        suspendCancellableCoroutine {
            addOnCancel { if (it.isActive) it.resume(false) }
            addOnSelectOk { it.resume(true) }
            show()
        }
    } ?: false
}