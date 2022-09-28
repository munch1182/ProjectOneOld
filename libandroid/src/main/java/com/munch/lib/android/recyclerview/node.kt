package com.munch.lib.android.recyclerview

import androidx.recyclerview.widget.RecyclerView
import com.munch.lib.android.extend.to

/**
 * Create by munch1182 on 2022/9/28 9:09.
 */
abstract class BaseNodeRecyclerViewAdapter<D : BaseNode, VH : RecyclerView.ViewHolder>(
    dataHelper: AdapterFunHelper<D> = SimpleAdapterFun(),
    eventHelper: AdapterEventHelper<VH> = ClickHelper()
) :
    BaseMultiRecyclerViewAdapter<D, VH>(dataHelper = dataHelper, eventHelper = eventHelper),
    INodeFunHelper<D> {

    // todo error 当已经展开一个时, 再展开另一个, 展开的位置错误
    override fun toggle(pos: Int) {
        val node = getNode<BaseNode>(pos)
        if (node !is BaseExpendNode) return // 如果该节点不可折叠
        if (node.isExpanded) {
            collapse(pos)
        } else {
            expend(pos)
        }
    }

    override fun collapse(pos: Int) {
        val node = getNode<BaseNode>(pos)
        if (node !is BaseExpendNode || !node.isExpanded) return // 如果该节点不可折叠, 或者已经折叠

        node.isExpanded = false
        notifyItemChanged(pos) // 通知该item的isExpanded已更改

        node.childNode()?.let { remove(it.to<MutableList<D>>()) } // 移除其下所有子节点
    }

    override fun expend(pos: Int) {
        val node = getNode<BaseNode>(pos)
        if (node !is BaseExpendNode || node.isExpanded) return // 如果该节点不可展开, 或者已经展开

        node.isExpanded = true
        notifyItemChanged(pos) // 通知该item的isExpanded已更改

        node.childNode()
            ?.let {
                add((find(node.to()) ?: return) + 1, it.to<List<D>>()) // 添加其下所有子节点
            }
    }

    /**
     * 只需要传入父节点, 子节点在父节点的数据中
     */
    override fun set(data: Collection<D>?) {
        super.set(adaptiveData(data))
    }

    // todo imp
    override fun remove(index: Int) {
    }

    /**
     * 获取根节点上的第[pos]个节点
     */
    fun <NODE : BaseNode> getNode(pos: Int): NODE? {
        return getNode(pos, getData())?.to()
    }

    /**
     * 获取某个节点上的第[pos]个子节点
     */
    fun <NODE : BaseNode> getNode(baseNode: BaseNode, pos: Int): NODE? {
        return getNode(pos, baseNode.childNode())?.to()
    }

    /**
     * 获取[list]上的第[pos]个根节点
     *
     * todo 是否需要在数据中记录当前的位置, 在更新时调整?
     */
    private fun getNode(pos: Int, list: List<BaseNode>?): BaseNode? {
        if (list.isNullOrEmpty()) return null

        var topNodeIndex = 0 // 当前循环到的第几个根节点
        var index = 0
        while (index < list.size) {
            val node = list[index]
            if (topNodeIndex == pos) {
                return node
            }
            if (node is BaseExpendNode && node.isExpanded) { // 如果该节点可以展开且已展开, 则跳过其子节点
                index += node.childNode()?.size ?: 0
            }

            index++
            topNodeIndex++
        }
        return null
    }

    /**
     * 处理Node数据相关显示节点的数据调整
     *
     * @param data 要显示的数据
     * @param isExpand 是否要通过方法(而不是数据)来更改展开状态
     */
    private fun adaptiveData(
        data: Collection<D>?,
        isExpand: Boolean? = null
    ): Collection<D>? {
        if (data == null || data.isEmpty()) return data
        val newData = mutableListOf<D>()
        data.forEach {
            newData.add(it)
            if (it is BaseExpendNode) {
                if (isExpand == true || it.isExpanded) { // 如果要展开子节点
                    val child = it.childNode()
                    if (!child.isNullOrEmpty()) {
                        adaptiveData(data, isExpand)?.let { d -> newData.addAll(d) } // 嵌套展开子节点
                    }
                }
                isExpand?.let { isExpand -> it.isExpanded = isExpand } // 更新展开状态
            } else {
                val child = it.childNode()
                if (!child.isNullOrEmpty()) {
                    adaptiveData(child.to(), isExpand)?.let { d -> newData.addAll(d) } // 嵌套展开子节点
                }
            }
        }
        return newData
    }
}

interface BaseNode {
    /**
     * 该节点所拥有的子节点, 如果没有返回null或者空
     */
    fun childNode(): MutableList<BaseNode>?

}

interface BaseExpendNode : BaseNode {

    /**
     * 当前节点是否展开
     */
    var isExpanded: Boolean
}

interface INodeFunHelper<D> : AdapterDataFun<D> {

    /**
     * 如果该节点是[BaseExpendNode], 则折叠该节点
     */
    fun collapse(pos: Int)

    /**
     * 如果该节点是[BaseExpendNode], 则展开该节点
     */
    fun expend(pos: Int)

    /**
     * 如果展开, 则折叠; 如果折叠, 则展开
     */
    fun toggle(pos: Int)

    /**
     * 如果该节点持有子节点, 其子节点也会被删除, 否则, 只会删除该节点
     */
    override fun remove(index: Int)
}