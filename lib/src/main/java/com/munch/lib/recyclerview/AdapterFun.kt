package com.munch.lib.recyclerview

/**
 * Create by munch1182 on 2021/8/5 17:01.
 */
interface AdapterFun<D> : IsAdapter {

    val data: MutableList<D?>

    //<editor-fold desc="add">
    fun add(element: D?) = add(data.size, element)

    fun add(index: Int, element: D?) {
        data.add(index, element)
        noTypeAdapter.notifyItemInserted(index)
    }

    fun add(elements: Collection<D?>) = add(data.size, elements)

    /**
     * 从[index]位置起，插入数据列表[elements]
     */
    fun add(index: Int, elements: Collection<D?>) {
        data.addAll(index, elements)
        noTypeAdapter.notifyItemRangeInserted(index, elements.size)
    }
    //</editor-fold>

    //<editor-fold desc="remove">
    fun remove(element: D) {
        val pos = data.indexOf(element)
        if (pos == -1) {
            return
        }
        data.remove(element)
        noTypeAdapter.notifyItemRemoved(pos)
    }

    fun remove(index: Int) = remove(index, 1)

    /**
     * 从[startIndex]开始删除[size]个元素
     */
    fun remove(startIndex: Int, size: Int) {
        val subList = data.subList(startIndex, startIndex + size)
        data.removeAll(subList)
        noTypeAdapter.notifyItemRangeRemoved(startIndex, size)
    }

    /**
     * 删除与[element]相同的元素:相同取决于元素的equal实现
     * [element]不必连续，如果连续，使用[remove]的带索引的方法
     */
    fun remove(element: Collection<D?>) {
        element.forEach {
            val index = getIndex(it ?: return@forEach) ?: return@forEach
            noTypeAdapter.notifyItemRemoved(index)
        }
    }
    //</editor-fold>

    //<editor-fold desc="update">
    /**
     * 如果有该元素的话，则更改该元素为[element]
     *
     * 如果该元素可能为null，需要使用索引[update]
     */
    fun update(element: D) {
        val index = getIndex(element) ?: return
        data[index] = element
        noTypeAdapter.notifyItemChanged(index)
    }

    /**
     * 如果[index]在数据范围内，则更新[index]位置的数据为[element]
     * 如果[index]在数据尾，则增加数据[element]
     *
     * @see updateOrThrow
     */
    fun update(index: Int, element: D?) {
        val size = data.size
        when {
            //索引超出数据范围则不操作
            size < index -> return
            //索引为数据末尾则增加
            index == size -> {
                add(element)
                noTypeAdapter.notifyItemInserted(index)
            }
            else -> {
                data[index] = element
                noTypeAdapter.notifyItemChanged(index)
            }
        }
    }

    /**
     * 更新[index]位置的数据为[element]，如果[index]超出数据范围，则抛出异常，其余逻辑同[update]
     *
     * @see update
     */
    fun updateOrThrow(index: Int, element: D?) {
        if (data.size < index) {
            throw IndexOutOfBoundsException()
        }
        update(index, element)
    }

    /**
     * 更新[elements]中的数据，数据可不连续，如果连续，则使用另一个带索引的[update]
     */
    fun update(elements: Collection<D>) {
        elements.forEach {
            val index = getIndex(it) ?: return@forEach
            data[index] = it
            noTypeAdapter.notifyItemChanged(index)
        }
    }

    /**
     * 从[startIndex]开始，更改[elements]长度的元素，如果[elements]元素超出原有数据长度，则会增加多出的元素
     */
    fun update(startIndex: Int, elements: Collection<D?>) {
        val size = data.size
        when {
            //索引超出数据范围则不操作
            size < startIndex -> return
            //索引为数据末尾则增加
            startIndex == size -> {
                data.addAll(elements)
                noTypeAdapter.notifyItemRangeInserted(startIndex, elements.size)
            }
            else -> {
                val length = elements.size
                //如果所有的数据都在原数据范围内
                if (startIndex + length < size) {
                    elements.forEachIndexed { index, d ->
                        data[startIndex + index] = d
                    }
                    noTypeAdapter.notifyItemRangeChanged(startIndex, length)
                } else {
                    val split = startIndex + length - size
                    elements.forEachIndexed { index, d ->
                        if (index < split) {
                            data[startIndex + index] = d
                        } else {
                            data.add(d)
                        }
                    }
                    noTypeAdapter.notifyItemChanged(startIndex, split - 1)
                    noTypeAdapter.notifyItemRangeInserted(startIndex + split - 1, length - split)
                }
            }
        }
    }

    /**
     * 从[startIndex]开始，更改[elements]长度的元素，如果[elements]元素超出原有数据长度，则会抛出异常
     */
    fun updateOrThrow(startIndex: Int, elements: Collection<D?>) {
        val size = data.size
        when {
            //索引超出数据范围则抛出异常
            size < startIndex -> throw IndexOutOfBoundsException()
            //索引为数据末尾则增加
            startIndex != size -> {
                val length = elements.size
                if (startIndex + length > size) {
                    throw IndexOutOfBoundsException()
                }
            }
        }
        update(startIndex, elements)
    }
    //</editor-fold>

    //<editor-fold desc="get">
    fun get(index: Int) = if (data.size <= index) null else data[index]
    fun getIndex(element: D): Int? = data.indexOf(element).takeIf { it != -1 }
    fun contains(element: D): Boolean = data.contains(element)
    //</editor-fold>
}