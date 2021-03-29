package com.munch.lib.helper

import android.graphics.Path
import android.graphics.RectF
import android.view.View
import java.text.ParseException


/**
 * Create by munch1182 on 2021/3/29 16:50.
 */
class PathHelper {
    private var pathString: String? = null
    private var len = 0
    private var curIndex = 0
    private var last = '!' // 标志开头


    private var argNum = 0
    private var curNum = 0

    private val args = FloatArray(6)

    /**
     * 解析一段路径字符串并返回一个路径对象Path<br></br>
     * 目前支持的指令有：<br></br>
     * m、M：相当于执行Path.moveTo，后面带2个参数，分别为x和y坐标；例如："m50,50"<br></br>
     * l、L：相当于执行Path.lineTo，后面带2个参数，分别为x和y坐标；例如："l28.8,33.3"<br></br>
     * a、A：相当于执行Path.addArc，后面带6个参数，分别为left，top，right，bottom，startAngle和sweepAngle；例如："a-50,-50,50,50,45,359.9"<br></br>
     * o、O：相当于执行Path.addCircle，后面带4个参数，分别为圆心的x和y坐标、半径、方向，方向为1表示CW，2表示CCW(详见[android.graphics.Path.Direction])；例如："l30,30"<br></br>
     * q、Q：相当于执行Path.quadTo，二次贝塞尔曲线，后面带4个参数，(详见[Path.quadTo])；例如："q50,50,100,0"<br></br>
     * c、C：相当于执行Path.cubicTo，三次贝塞尔曲线，后面带6个参数，(详见[Path.cubicTo])；例如："q50,50,100,0"<br></br>
     * z、Z：相当于执行Path.close，不带参数，例如："z"<br></br>
     * 其中，指令为小写时的坐标为相对于父容器原点的坐标，大写时为绝对坐标；当parent参数为空时，按绝对坐标解析。
     *
     * @param str    要解析的路径字符串，每一种指令之间用空格来间隔，相同指令之间无需再指定指令字符，例如："m100,100 l150,150 100,150 z"
     * @param parent 父容器视图对象
     * @param factor 实际大小与指定大小的比例，用于正确生成路径位置，例如实际绘制大小为600x400，指定大小为300x200，此时factor为2
     * @return Path 解析完成后返回的路径对象
     * @throws ParseException 发生解析错误时抛出的异常
     */
    @Throws(ParseException::class)
    fun parse(str: String, parent: View? = null, factor: Float = 1f): Path? {
        val path = Path()
        pathString = str.trim { it <= ' ' }
        len = pathString?.length ?: return null
        curIndex = 0
        while (curIndex < len) {
            curNum = 0
            var ch: Char = pathString!![curIndex]

            // 跳过空格
            while (ch == ' ') {
                if (++curIndex < len) ch = pathString!![curIndex]
            }

            // 是否为同一个指令
            if (last != '!' && (ch in '0'..'9' || ch == '-')) {
                ch = last
                --curIndex
            }
            when (ch) {
                'M', 'm' -> {
                    argNum = 2
                    last = 'm'
                    nextToken()
                    if (ch == 'M' && null != parent) {
                        last = 'M'
                        args[0] -= parent.x
                        args[1] -= parent.y
                    }
                    path.moveTo(args[0] * factor, args[1] * factor)
                }
                'L', 'l' -> {
                    argNum = 2
                    last = 'l'
                    nextToken()
                    if (ch == 'L' && null != parent) {
                        last = 'L'
                        args[0] -= parent.x
                        args[1] -= parent.y
                    }
                    path.lineTo(args[0] * factor, args[1] * factor)
                }
                'A', 'a' -> {
                    argNum = 6
                    last = 'a'
                    nextToken()
                    if (ch == 'A' && null != parent) {
                        last = 'A'
                        args[0] -= parent.getX()
                        args[1] -= parent.getY()
                        args[2] -= parent.getX()
                        args[3] -= parent.getY()
                    }
                    val oval = RectF()
                    oval.left = args[0] * factor
                    oval.top = args[1] * factor
                    oval.right = args[2] * factor
                    oval.bottom = args[3] * factor
                    path.addArc(oval, args[4], args[5])
                }
                'O', 'o' -> {
                    argNum = 4
                    last = 'o'
                    nextToken()
                    if (ch == 'O' && null != parent) {
                        last = 'O'
                        args[0] -= parent.x
                        args[1] -= parent.y
                    }
                    val dir = if (args[3] == 1f) Path.Direction.CW else Path.Direction.CCW
                    path.addCircle(
                        args[0] * factor,
                        args[1] * factor,
                        args[2] * factor,
                        dir
                    )
                }
                'Q', 'q' -> {
                    argNum = 4
                    last = 'q'
                    nextToken()
                    if (ch == 'Q' && null != parent) {
                        last = 'Q'
                        args[0] -= parent.getX()
                        args[1] -= parent.getY()
                        args[2] -= parent.getX()
                        args[3] -= parent.getY()
                    }
                    path.quadTo(
                        args[0] * factor,
                        args[1] * factor,
                        args[2] * factor,
                        args[3] * factor
                    )
                }
                'C', 'c' -> {
                    argNum = 6
                    last = 'c'
                    nextToken()
                    if (ch == 'C' && null != parent) {
                        last = 'C'
                        args[0] -= parent.getX()
                        args[1] -= parent.getY()
                        args[2] -= parent.getX()
                        args[3] -= parent.getY()
                        args[4] -= parent.getX()
                        args[5] -= parent.getY()
                    }
                    path.cubicTo(
                        args[0] * factor,
                        args[1] * factor,
                        args[2] * factor,
                        args[3] * factor,
                        args[4] * factor,
                        args[5] * factor
                    )
                }
                'Z', 'z' -> {
                    last = 'z'
                    ++curIndex
                    path.close()
                }
                else -> throw ParseException("expected command character", curIndex)
            }
        }
        return path
    }

    @Throws(ParseException::class)
    private fun nextToken() {
        var isStart = true
        var seenDot = false
        var num = StringBuilder()
        ++curIndex
        while (curIndex < len) {
            val ch: Char = pathString!![curIndex]
            if (ch == '-' && !isStart) throw ParseException(
                "unexpected '-' but '$ch'",
                curIndex
            ) else if (ch == '.' && (isStart || seenDot)) throw ParseException(
                "unexpected '.' but '$ch'",
                curIndex
            )
            isStart = false
            if (ch in '0'..'9' || ch == '-') {
                num.append(ch)
            } else if (!seenDot && ch == '.') {
                seenDot = true
                num.append(ch)
            } else {
                consumeNum(num.toString())
                isStart = true
                seenDot = false
                num = StringBuilder()
                if (curNum == argNum) break
                if (ch != ',') throw ParseException("expected ',' but '$ch'", curIndex)
            }
            ++curIndex
        }
        if (curIndex == len) consumeNum(num.toString())
    }

    private fun consumeNum(num: String) {
        ++curNum
        args[curNum - 1] = java.lang.Float.valueOf(num)
    }

}