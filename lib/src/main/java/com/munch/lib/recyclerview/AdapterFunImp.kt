package com.munch.lib.recyclerview

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.AsyncListDiffer
import com.munch.lib.recyclerview.AdapterFunImp.Default
import com.munch.lib.recyclerview.AdapterFunImp.Differ
import com.munch.lib.task.isMain

/**
 * 因为[Default]/[Differ]的实现中都有实际Adapter方法调用的线程判断
 * （[Default]直接依赖[Default.mainHandler]实现,而[Differ]是依赖[AsyncListDiffer.mUpdateCallback]来实现，参见[AdapterListUpdateInHandlerCallback]）
 * 因此更建议在子线程中调用这些方法
 *
 * Create by munch1182 on 2021/8/5 17:01.
 */
sealed class AdapterFunImp<D>(
    override val noTypeAdapter: BaseRecyclerViewAdapter<*, *>,
) : IsAdapter, IAdapterFun<D> {

    class Default<D>(
        noTypeAdapter: BaseRecyclerViewAdapter<*, *>,
        private val data: MutableList<D?>,
        private val mainHandler: Handler = Handler(Looper.getMainLooper())
    ) : AdapterFunImp<D>(noTypeAdapter) {

        private fun impInMain(imp: () -> Unit) {
            if (Thread.currentThread().isMain()) {
                imp.invoke()
            } else {
                mainHandler.post(imp)
            }
        }

        override fun set(newData: List<D?>?) {
            val size = data.size
            if (size > 0) {
                data.clear()
                impInMain { noTypeAdapter.notifyItemRangeRemoved(0, size) }
            }
            if (newData != null) {
                data.addAll(newData)
                impInMain { noTypeAdapter.notifyItemRangeInserted(0, newData.size) }
            }
        }


        override fun add(index: Int, element: D?) {
            if (index in 0..data.size) {
                data.add(index, element)
                impInMain { noTypeAdapter.notifyItemInserted(index) }
            }
        }

        override fun add(index: Int, elements: Collection<D?>) {
            if (index in 0..data.size) {
                val size = elements.size
                data.addAll(index, elements)
                impInMain { noTypeAdapter.notifyItemRangeInserted(index, size) }
            }
        }

        override fun remove(element: D) {
            val pos = data.indexOf(element)
            if (pos == -1) {
                return
            }
            data.remove(element)
            impInMain { noTypeAdapter.notifyItemRemoved(pos) }
        }

        override fun remove(startIndex: Int, size: Int) {
            val endIndex = startIndex + size
            if (endIndex <= data.size) {
                data.removeAll(data.subList(startIndex, endIndex))
                impInMain { noTypeAdapter.notifyItemRangeRemoved(startIndex, size) }
            }
        }

        override fun remove(element: Collection<D?>) {
            data.removeAll(element)
            element.forEach {
                val index = getIndex(it ?: return@forEach) ?: return@forEach
                impInMain { noTypeAdapter.notifyItemRemoved(index) }
            }
        }

        override fun update(index: Int, element: D?, payload: Any?) {
            val size = data.size
            if (index in 0 until size) {
                data[index] = element
                impInMain { noTypeAdapter.notifyItemChanged(index, payload) }
            }
        }

        override fun update(startIndex: Int, elements: Collection<D?>, payload: Any?) {
            val size = data.size
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) in 0 until size) {
                elements.forEachIndexed { index, d -> data[startIndex + index] = d }
                impInMain {
                    noTypeAdapter.notifyItemRangeChanged(startIndex, updateCount, payload)
                }
            }
        }

        override fun add(element: D?) = add(data.size, element)
        override fun add(elements: Collection<D?>) = add(data.size, elements)
        override fun remove(index: Int) = remove(index, 1)

        override fun updateOrThrow(index: Int, element: D?, payload: Any?) {
            if (data.size <= index) {
                throw IndexOutOfBoundsException()
            }
            update(index, element, payload)
        }

        override fun updateOrThrow(startIndex: Int, elements: Collection<D?>, payload: Any?) {
            val size = data.size
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) >= size) {
                throw IndexOutOfBoundsException()
            }
            update(startIndex, elements, payload)
        }

        override fun get(index: Int) = if (data.size <= index) null else data[index]
        override fun getIndex(element: D): Int? = data.indexOf(element).takeIf { it != -1 }
        override fun contains(element: D): Boolean = data.contains(element)
    }

    /**
     * 注意[differ]的实现，特别是[androidx.recyclerview.widget.ListUpdateCallback]的实现，因为
     * [androidx.recyclerview.widget.AsyncListDiffer.submitList]的回调线程不是固定的
     *
     * @see AdapterListUpdateInHandlerCallback
     */
    class Differ<D>(
        noTypeAdapter: BaseRecyclerViewAdapter<*, *>,
        private val differ: AsyncListDiffer<D>,
        private val runnable: Runnable? = null
    ) : AdapterFunImp<D>(noTypeAdapter) {

        private val data: MutableList<D?>
            get() = differ.currentList

        override fun set(newData: List<D?>?) {
            //此方法在newData和data为null时会直接在当前线程回调，否则会在子线程中计算，在主线程中回调
            differ.submitList(newData, runnable)
        }

        override fun add(index: Int, element: D?) =
            set(ArrayList(data).apply { add(index, element) })

        override fun add(index: Int, elements: Collection<D?>) =
            set(ArrayList(data).apply { addAll(index, elements) })

        override fun remove(element: D) {
            val pos = data.indexOf(element)
            if (pos == -1) {
                return
            }
            set(ArrayList(data).apply { remove(element) })
        }

        override fun remove(startIndex: Int, size: Int) {
            val endIndex = startIndex + size
            if (endIndex <= data.size) {
                set(ArrayList(data).apply { data.removeAll(data.subList(startIndex, endIndex)) })
            }
        }

        override fun remove(element: Collection<D?>) {
            val newData = ArrayList(data)
            element.forEach {
                getIndex(it ?: return@forEach) ?: return@forEach
                newData.remove(it)
            }
            set(newData)
        }

        override fun update(index: Int, element: D?, payload: Any?) {
            val size = data.size
            if (index in 0 until size) {
                set(ArrayList(data).apply { set(index, element) })
            }
        }

        override fun update(startIndex: Int, elements: Collection<D?>, payload: Any?) {
            val size = data.size
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) in 0 until size) {
                val newData = ArrayList(data)
                elements.forEachIndexed { index, d -> data[startIndex + index] = d }
                set(newData)
            }
        }

        override fun add(element: D?) = add(data.size, element)
        override fun add(elements: Collection<D?>) = add(data.size, elements)
        override fun remove(index: Int) = remove(index, 1)

        override fun updateOrThrow(index: Int, element: D?, payload: Any?) {
            if (data.size <= index) {
                throw IndexOutOfBoundsException()
            }
            update(index, element)
        }

        override fun updateOrThrow(startIndex: Int, elements: Collection<D?>, payload: Any?) {
            val size = data.size
            val updateCount = elements.size
            //如果更改的数据在原有数据范围内
            if ((startIndex + updateCount) >= size) {
                throw IndexOutOfBoundsException()
            }
            update(startIndex, elements)
        }

        override fun get(index: Int) = if (data.size <= index) null else data[index]
        override fun getIndex(element: D): Int? = data.indexOf(element).takeIf { it != -1 }
        override fun contains(element: D): Boolean = data.contains(element)
    }
}