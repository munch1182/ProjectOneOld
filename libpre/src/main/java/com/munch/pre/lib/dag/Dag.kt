package com.munch.pre.lib.dag

import androidx.annotation.IntDef

/**
 * 有向无环图排序
 *
 * [KEY]是所有point的区分标志，因此相同逻辑的点要被多次添加应该有不同的[KEY]
 *
 * @see addEdge 对于此类来说，不允许直接添加点，只允许添加边
 * @see generaDag 生成方法，返回依赖顺序
 *
 * Create by munch1182 on 2021/4/1 14:17.
 */
// TODO: 2021/4/2 需要保证线程安全
class Dag<KEY> {

    @IntDef(REPLACE_HIGHER_PRIORITY, REPLACE_LOWER_PRIORITY, NO_REPLACE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ReplaceStrategy

    companion object {

        const val REPLACE_HIGHER_PRIORITY = 2
        const val REPLACE_LOWER_PRIORITY = 1
        const val NO_REPLACE = 0
    }

    /**
     * @param key 区分各个point的标志，相同[KEY]的会被视为是同一point，与[priority]无关
     * @param priority 同一层的point的执行优先级，优先级越高越先排序，相同则先添加的先排序
     * @param replaceStrategy 替换策略，相同[KEY]被视为相同的point，如果先后添加的相同[KEY]的point是不同对象，指定替换的策略，主要用于更新优先级
     */
    data class Point<KEY>(
        val key: KEY,
        var priority: Int = 0,
        @ReplaceStrategy var replaceStrategy: Int = NO_REPLACE
    ) : Comparable<Point<KEY>> {

        internal var inDegree: Int = -1

        internal fun update(point: Point<KEY>): Point<KEY> {
            if (point.key == key) {
                when (point.replaceStrategy) {
                    REPLACE_HIGHER_PRIORITY -> {
                        if (priority > point.priority) {
                            return this
                        }
                    }
                    REPLACE_LOWER_PRIORITY -> {
                        if (priority < point.priority) {
                            return this
                        }
                    }
                    NO_REPLACE -> {

                    }
                }
                priority = point.priority
                replaceStrategy = point.replaceStrategy
            }
            return this
        }

        override fun equals(other: Any?): Boolean {
            other ?: return false
            if (other is Point<*>) {
                return key == other.key
            }
            return false
        }

        override fun hashCode(): Int {
            return key.hashCode()
        }

        override fun compareTo(other: Point<KEY>): Int {
            return other.priority.compareTo(priority)
        }
    }

    data class Edge<KEY>(val from: Point<KEY>, val to: Point<KEY>) {


        override fun hashCode(): Int {
            return from.hashCode() * 31 + to.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            other ?: return false
            if (other is Edge<*>) {
                return from == other.from && to == other.to
            }
            return false
        }

        override fun toString(): String = "$from -> $to"
    }

    private val edgeList = mutableListOf<Edge<KEY>>()
    private val edgeOnlyList = mutableListOf<Edge<KEY>>()
    private val pointList = mutableListOf<Point<KEY>>()
    private val zeroDegreeList = mutableListOf<Point<KEY>>()
    private val resSortList = mutableListOf<Point<KEY>>()

    fun addEdge(edge: Edge<KEY>): Dag<KEY> {
        if (!edgeList.contains(edge)) {
            edgeList.add(edge)
        }
        return this
    }

    fun generaDag(): MutableList<Point<KEY>> {
        putEdgeOnlyAndPoint()
        edgeList.clear()

        if (pointList.isEmpty()) {
            return mutableListOf()
        }
        val size = pointList.size
        pointList.forEach {
            if (it.inDegree == 0) {
                zeroDegreeList.add(it)
            }
        }
        if (zeroDegreeList.isEmpty()) {
            throw IllegalStateException("cannot find start point")
        }
        zeroDegreeList.sort()
        do {
            zeroDegreeList.forEach {
                resSortList.add(it)
                pointList.remove(it)
                //通过添加时保证每个点对象唯一，即可同步更改inDegree
                it.inDegree = -1
            }
            val iterator = edgeOnlyList.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.from.inDegree < 0) {
                    next.to.inDegree--
                    iterator.remove()
                }
            }
            zeroDegreeList.clear()
            pointList.forEach {
                if (it.inDegree == 0) {
                    zeroDegreeList.add(it)
                }
            }
            zeroDegreeList.sort()
        } while (zeroDegreeList.isNotEmpty())

        if (resSortList.size != size) {
            throw IllegalStateException("circular dependency")
        }
        return resSortList
    }

    private fun putEdgeOnlyAndPoint() {
        edgeList.forEach {
            edgeOnlyList.add(Edge(addZeroPoint(it.from), addDegreePoint(it.to)))
        }
    }

    /**
     * 添加有依赖的点
     *
     * 第一次添加入度为1
     * 后每添加一次入度加1
     */
    private fun addDegreePoint(point: Point<KEY>): Point<KEY> {
        val index = pointList.indexOf(point)
        return if (index == -1) {
            point.inDegree = 1
            pointList.add(point)
            point
        } else {
            //不是同一个对象但是equals方法比较是相同的
            val pointReal = pointList[index]
            pointReal.inDegree++
            pointReal.update(point)
        }
    }

    private fun addZeroPoint(point: Point<KEY>): Point<KEY> {
        if (!pointList.contains(point)) {
            point.inDegree = 0
            pointList.add(point)
            return point
        }
        return pointList[pointList.indexOf(point)].update(point)
    }

}