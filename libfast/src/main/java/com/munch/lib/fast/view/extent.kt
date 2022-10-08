package com.munch.lib.fast.view

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.munch.lib.android.extend.catch
import com.munch.lib.android.extend.toOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.abs
import kotlin.random.Random

@Suppress("NOTHING_TO_INLINE")
inline fun LifecycleOwner.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch(context, start, block)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Fragment.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    noinline block: suspend CoroutineScope.() -> Unit
) {
    viewLifecycleOwner.launch(context, start, block)
}

fun newRandomWord(): String {
    val heightPos = 176 + abs(Random.nextInt(39))
    val lowPos = 161 + abs(Random.nextInt(93))
    return catch {
        String(byteArrayOf(heightPos.toByte(), lowPos.toByte()), Charset.forName("GBK"))
    } ?: ""
}

fun newRandomString(len: Int = 5, sb: StringBuilder = StringBuilder()): String {
    repeat(len) { sb.append(newRandomWord()) }
    return sb.toString()
}

inline fun <reified BEHAVIOR : CoordinatorLayout.Behavior<View>> CoordinatorLayout.findFirst(): View? {
    return children.firstOrNull {
        it.layoutParams.toOrNull<CoordinatorLayout.LayoutParams>()?.behavior is BEHAVIOR
    }
}