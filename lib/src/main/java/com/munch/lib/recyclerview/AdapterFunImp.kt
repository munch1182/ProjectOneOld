package com.munch.lib.recyclerview


import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.munch.lib.recyclerview.AdapterFunImp.Default
import com.munch.lib.recyclerview.AdapterFunImp.Differ
import com.munch.lib.task.isMain

/**
 * 因为[Default]/[Differ]的实现中都有实际Adapter方法调用的线程判断
 * （[Default]直接依赖[Default.mainHandler]实现,而[Differ]是依赖[AsyncListDiffer.mUpdateCallback]来实现）
 * 因此更建议在子线程中调用这些方法
 *
 * Create by munch1182 on 2021/8/5 17:01.
 */
sealed class AdapterFunImp<D>(
    private val mainHandler: Handler = Handler(Looper.getMainLooper())
) : IAdapterFun<D> {

    protected var adapter: BaseRecyclerViewAdapter<*, *>? = null

    protected fun impInMain(imp: () -> Unit) {
        if (Thread.currentThread().isMain()) {
            imp.invoke()
        } else {
            mainHandler.post(imp)
        }
    }

    open fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>) {
        this.adapter = adapter
    }

    class Default<D>(
        mainHandler: Handler = Handler(Looper.getMainLooper())
    ) : AdapterFunImp<D>(mainHandler) {

        private val list = mutableListOf<D>()

        override val itemSize: Int
            get() = list.size

        @SuppressLint("NotifyDataSetChanged")
        override fun set(newData: List<D>?) {
            val size = list.size
            list.clear()
            if (newData != null) {
                list.addAll(newData)
            }
            val adapter = adapter ?: return
            val newSize = list.size
            impInMain {
                when {
                    size == 0 -> adapter.notifyItemRangeInserted(0, list.size)
                    newSize == 0 -> adapter.notifyItemRangeRemoved(0, size)
                    else -> adapter.notifyDataSetChanged()
                }
            }
        }

        override fun add(index: Int, element: D) {
            if (index in 0..list.size) {
                list.add(index, element)
                val adapter = adapter ?: return
                impInMain { adapter.notifyItemInserted(index) }
            }
        }

        override fun add(index: Int, elements: Collection<D>) {
            if (index in 0..list.size) {
                val size = elements.size
                list.addAll(index, elements)
                val adapter = adapter ?: return
                impInMain { adapter.notifyItemRangeInserted(index, size) }
            }
        }

        override fun remove(element: D) {
            val pos = list.indexOf(element)
            if (pos == -1) {
                return
            }
            list.remove(element)
            val adapter = adapter ?: return
            impInMain { adapter.notifyItemRemoved(pos) }
        }

        override fun remove(startIndex: Int, size: Int) {
            val endIndex = startIndex + size
            if (endIndex <= list.size) {
                list.removeAll(list.subList(startIndex, endIndex))
                val adapter = adapter ?: return
                impInMain { adapter.notifyItemRangeRemoved(startIndex, size) }
            }
        }

        override fun remove(element: Collection<D?>) {
            list.removeAll(element)
            element.forEach {
                val index = getIndex(it ?: return@forEach) ?: return@forEach
                val adapter = adapter ?: return@forEach
                impInMain { adapter.notifyItemRemoved(index) }
            }
        }

        override fun update(index: Int, element: D, payload: Any?) {
            val size = list.size
            if (index in 0 until size) {
                list[index] = element
                val adapter = adapter ?: return
                impInMain { adapter.notifyItemChanged(index, payload) }
            }
        }

        override fun update(startIndex: Int, elements: Collection<D>, payload: Any?) {
            val size = list.size
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) in 0 until size) {
                elements.forEachIndexed { index, d -> list[startIndex + index] = d }
                val adapter = adapter ?: return
                impInMain {
                    adapter.notifyItemRangeChanged(startIndex, updateCount, payload)
                }
            }
        }

        override fun add(element: D) = add(list.size, element)
        override fun add(elements: Collection<D>) = add(list.size, elements)
        override fun remove(index: Int) = remove(index, 1)

        override fun updateOrThrow(index: Int, element: D, payload: Any?) {
            if (list.size <= index) {
                throw IndexOutOfBoundsException()
            }
            update(index, element, payload)
        }

        override fun updateOrThrow(startIndex: Int, elements: Collection<D>, payload: Any?) {
            val size = list.size
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) >= size) {
                throw IndexOutOfBoundsException()
            }
            update(startIndex, elements, payload)
        }

        override fun get(index: Int) = if (list.size <= index) null else list[index]
        override fun getIndex(element: D): Int? = list.indexOf(element).takeIf { it != -1 }
        override fun contains(element: D): Boolean = list.contains(element)
    }

    /**
     * 注意[differ]的实现，特别是[androidx.recyclerview.widget.ListUpdateCallback]的实现，因为
     * [androidx.recyclerview.widget.AsyncListDiffer.submitList]的回调线程不是固定的
     */
    class Differ<D>(
        private val callback: DiffUtil.ItemCallback<D>,
        mainHandler: Handler = Handler(Looper.getMainLooper()),
        private val runnable: Runnable? = null
    ) : AdapterFunImp<D>(mainHandler) {

        private var differ: AsyncListDiffer<D>? = null

        override fun bindAdapter(adapter: BaseRecyclerViewAdapter<*, *>) {
            super.bindAdapter(adapter)
            differ = AsyncListDiffer(adapter, callback)
        }

        override val itemSize: Int
            get() = list.size

        private val list: MutableList<D>
            get() = differ?.currentList ?: mutableListOf()

        override fun set(newData: List<D>?) {
            differ ?: throw IllegalArgumentException()
            if (list.isEmpty() || newData == null || newData.isEmpty()) {
                impInMain { differ?.submitList(newData, runnable) }
            } else {
                //此方法在newData和data为null时会直接在当前线程回调，否则会在子线程中计算，在主线程中回调
                differ?.submitList(newData, runnable)
            }
        }

        override fun add(index: Int, element: D) =
            set(ArrayList(list).apply { add(index, element) })

        override fun add(index: Int, elements: Collection<D>) =
            set(ArrayList(list).apply { addAll(index, elements) })

        override fun remove(element: D) {
            val pos = list.indexOf(element)
            if (pos == -1) {
                return
            }
            set(ArrayList(list).apply { remove(element) })
        }

        override fun remove(startIndex: Int, size: Int) {
            val list = ArrayList(list)
            val endIndex = startIndex + size
            if (endIndex <= list.size) {
                list.removeAll(list.subList(startIndex, endIndex))
                set(list)
            }
        }

        override fun remove(element: Collection<D?>) {
            val newData = ArrayList(list)
            element.forEach {
                getIndex(it ?: return@forEach) ?: return@forEach
                newData.remove(it)
            }
            set(newData)
        }

        override fun update(index: Int, element: D, payload: Any?) {
            val size = list.size
            if (index in 0 until size) {
                set(ArrayList(list).apply { set(index, element) })
            }
        }

        override fun update(startIndex: Int, elements: Collection<D>, payload: Any?) {
            val size = list.size
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) in 0 until size) {
                val newData = ArrayList(list)
                elements.forEachIndexed { index, d -> list[startIndex + index] = d }
                set(newData)
            }
        }

        override fun add(element: D) = add(list.size, element)
        override fun add(elements: Collection<D>) = add(list.size, elements)
        override fun remove(index: Int) = remove(index, 1)

        override fun updateOrThrow(index: Int, element: D, payload: Any?) {
            if (list.size <= index) {
                throw IndexOutOfBoundsException()
            }
            update(index, element)
        }

        override fun updateOrThrow(startIndex: Int, elements: Collection<D>, payload: Any?) {
            val size = list.size
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) >= size) {
                throw IndexOutOfBoundsException()
            }
            update(startIndex, elements)
        }

        override fun get(index: Int) = if (list.size <= index) null else list[index]
        override fun getIndex(element: D): Int? = list.indexOf(element).takeIf { it != -1 }
        override fun contains(element: D): Boolean = list.contains(element)
    }
}