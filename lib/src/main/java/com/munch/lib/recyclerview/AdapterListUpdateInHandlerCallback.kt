package com.munch.lib.recyclerview

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.task.isMain

/**
 * 将[RecyclerView.Adapter]的更新方法放入指定线程，一般是主线程
 *
 * Create by munch1182 on 2021/12/6 15:45.
 */
class AdapterListUpdateInHandlerCallback(
    private val adapter: RecyclerView.Adapter<*>,
    private val handler: Handler = Handler(Looper.getMainLooper())
) : ListUpdateCallback {

    override fun onInserted(position: Int, count: Int) {
        imp { adapter.notifyItemRangeInserted(position, count) }
    }

    override fun onRemoved(position: Int, count: Int) {
        imp { adapter.notifyItemRangeRemoved(position, count) }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        imp { adapter.notifyItemMoved(fromPosition, toPosition) }
    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {
        imp { adapter.notifyItemRangeChanged(position, count, payload) }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun imp(noinline imp: () -> Unit) {
        if (Thread.currentThread().isMain()) {
            imp.invoke()
        } else {
            handler.post(imp)
        }
    }
}