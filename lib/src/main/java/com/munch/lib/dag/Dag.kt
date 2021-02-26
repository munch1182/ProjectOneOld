package com.munch.lib.dag

/**
 * Create by munch1182 on 2021/2/25 17:07.
 */
class Dag<T> {

    private val pointList = mutableListOf<Point<T>>()
    private val edgeList = mutableListOf<Edge<T>>()

    //0入度表
    private val zeroDegreeList = mutableListOf<Point<T>>()

    //同步edge表
    private val currentEdgeList = mutableListOf<Edge<T>>()

    //结果排序表
    private val resList = mutableListOf<Point<T>>()

    /**
     * 添加没有edge的点，否则应该使用[addEdge]
     */
    private fun addPoint(point: Point<T>) {
        if (!pointList.contains(point)) {
            pointList.add(point)
        }
    }

    /**
     * 添加方法
     *
     * 所有的顶点都应该通过添加边的方式添加
     *
     * @see Edge.point2Edge 顶点转为edge
     */
    fun addEdge(edge: Edge<T>) {
        if (!edgeList.contains(edge)) {
            edgeList.add(edge)
        }
    }

    /**
     * 添加有依赖的点
     *
     * 第一次添加入度为1
     * 后每添加一次入度加1
     */
    private fun addPointOrDegree(point: Point<T>) {
        val index = pointList.indexOf(point)
        if (index != -1) {
            pointList[index].inDegree++
        } else {
            point.inDegree = 1
            pointList.add(point)
        }
    }

    fun dump(): MutableList<Point<T>> {
        //计算入度
        edgeList.forEach {
            if (it.from != null) {
                addPoint(it.from)
                addPointOrDegree(it.to)
            } else {
                addPoint(it.to)
            }
        }
        pointList.sort()
        if (pointList.isEmpty()) {
            return mutableListOf()
        }
        //寻找入点
        pointList.forEach {
            if (it.inDegree == 0) {
                zeroDegreeList.add(it)
            }
        }
        if (zeroDegreeList.isEmpty()) {
            throw IllegalStateException("cannot find start point")
        }
        do {
            zeroDegreeList.forEach {
                resList.add(it)
                pointList.remove(it)

                currentEdgeList.clear()
                currentEdgeList.addAll(edgeList)
                edgeList.forEach { edge ->
                    if (edge.from == it) {
                        val index = pointList.indexOf(edge.to)
                        if (index != -1) {
                            pointList[index].inDegree--
                        }
                        currentEdgeList.remove(edge)
                    }
                }
                //清除以及计算的point的相关edge
                edgeList.clear()
                edgeList.addAll(currentEdgeList)
            }

            zeroDegreeList.clear()
            //更新下一个点
            pointList.forEach {
                if (it.inDegree == 0) {
                    zeroDegreeList.add(it)
                }
            }

        } while (zeroDegreeList.isNotEmpty())

        return resList
    }

    data class Edge<T>(val from: Point<T>? = null, val to: Point<T>, var weight: Int = 0) {

        companion object {

            fun <T> point2Edge(point: Point<T>) = Edge(to = point)
        }

        override fun equals(other: Any?): Boolean {
            other ?: return false
            if (other is Edge<*>) {
                return from == other.from && to == other.to
            }
            return false
        }

        override fun hashCode(): Int {
            return from.hashCode() * 31 + to.hashCode()
        }

        override fun toString(): String {
            return "$from -> $to"
        }
    }

    data class Point<T>(val point: T, var inDegree: Int = -1) : Comparable<Point<*>> {

        /**
         * 如果不能保证point地址一致，则应该重写T的equals方法来保持内容一致
         */
        override fun equals(other: Any?): Boolean {
            other ?: return false
            if (other is Point<*>) {
                return point == other.point
            }
            return false
        }

        override fun hashCode(): Int {
            return point.hashCode()
        }

        override fun compareTo(other: Point<*>): Int {
            return inDegree.compareTo(other.inDegree)
        }
    }
}