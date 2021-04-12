@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.munch.lib.fast.weight

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.lib.fast.R

/**
 * Create by munch1182 on 2021/4/12 11:03.
 */
class LetterNavigationBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {

    private val textPaint: Paint
    private var space: Float = 10f
        set(value) {
            field = if (maxSpace != -1f && value > maxSpace) {
                maxSpace
            } else {
                value
            }
        }
    private val letterRect = Rect()
    private val selectRect = Rect()


    private var selectIndex: Int = -1
    private val chars = mutableListOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
    )
    private val letters = mutableListOf<String>()
        get() {
            if (isInEditMode) {
                return chars
            }
            return field
        }
    private val selectLetters = mutableListOf<String>()
        get() {
            if (isInEditMode) {
                return arrayListOf("A", "B", "C", "D", "E")
            }
            return field
        }

    var textColor: Int = Color.TRANSPARENT
    var selectColor: Int = Color.TRANSPARENT
    var maxSpace: Float = -1f

    /**
     * 返回值：true则需要自行调用[select]
     */
    var handleListener: ((letter: String, rect: Rect) -> Boolean)? = null
    var clickListener: ((letter: String, rect: Rect) -> Unit)? = null
    var selectEndListener: ((letter: String, rect: Rect) -> Unit)? = null

    init {
        val colorGray = Color.parseColor("#574A4A")

        val attrsSet =
            context.obtainStyledAttributes(attrs, R.styleable.LetterNavigationBarView)
        textColor =
            attrsSet.getColor(R.styleable.LetterNavigationBarView_letter_text_color, colorGray)
        val textSize =
            attrsSet.getDimension(R.styleable.LetterNavigationBarView_letter_text_size, 40f)
        selectColor =
            attrsSet.getColor(R.styleable.LetterNavigationBarView_letter_select_color, colorGray)
        maxSpace = attrsSet.getDimension(R.styleable.LetterNavigationBarView_letter_max_space, -1f)
        attrsSet.recycle()
        this.textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.textSize = textSize
        }
    }

    fun setLetters(letters: MutableList<String>) {
        this.letters.clear()
        this.letters.addAll(letters)
        requestLayout()
        invalidate()
    }

    fun select(vararg letter: String) {
        selectLetters.clear()
        letter.forEach {
            if (!letters.contains(it)) {
                return@forEach
            }
            if (!selectLetters.contains(it)) {
                selectLetters.add(it)
            }
        }
        if (selectLetters.isNotEmpty()) {
            invalidate()
        }
    }

    fun select(vararg pos: Int) {
        if (letters.isEmpty()) {
            return
        }
        select(*Array(pos.size) { letters[pos[it]] })
    }

    fun selectRange(from: Int, to: Int) {
        val size = letters.size
        if (from > to || from < 0 || from > size || to < 0 || to > size) {
            return
        }
        select(*Array(to - from + 1) { letters[from + it] })
    }

    fun selectRange(from: String, to: String) =
        selectRange(letters.indexOf(from), letters.indexOf(to))

    fun getLettersList() = letters

    fun addLetters(vararg letter: String) {
        letters.addAll(letter)
        invalidate()
    }

    fun addLetter(index: Int, letter: String) {
        letters.add(index, letter)
        invalidate()
    }

    fun setAllLetters() = setLetters(chars)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        /*super.onMeasure(widthMeasureSpec, heightMeasureSpec)*/
        var maxWidth = letterRect.width()
        letters.forEach {
            textPaint.getTextBounds(it, 0, it.length, letterRect)
            if (letterRect.width() > maxWidth) {
                maxWidth = letterRect.width()
            }
        }
        letterRect.right = letterRect.left + maxWidth
        if (letters.size <= 1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        var sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        var sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val letterWidth = letterRect.width() + paddingLeft + paddingRight
        if (modeWidth != MeasureSpec.EXACTLY || sizeWidth < letterWidth) {
            sizeWidth = letterWidth
        }
        val letterAllHeight = letterRect.height() * letters.size + paddingTop + paddingBottom

        space = if (sizeHeight > letterAllHeight) {
            ((sizeHeight - letterAllHeight) / (letters.size - 1f)).coerceAtMost(maxSpace)
        } else {
            0f
        }
        if (modeHeight != MeasureSpec.EXACTLY) {
            sizeHeight = (letterAllHeight + space * (letters.size - 1)).toInt()
        }
        setMeasuredDimension(sizeWidth, sizeHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null || letters.isEmpty()) {
            return
        }
        val height = letterRect.height()
        val width = width - paddingLeft - paddingRight
        letters.forEachIndexed { index, s ->
            textPaint.getTextBounds(s, 0, s.length, letterRect)

            if (selectLetters.contains(s)) {
                textPaint.color = selectColor
            } else {
                textPaint.color = textColor
            }

            canvas.drawText(
                s,
                //为了让诸如i或者其它情形下瘦文字居中
                paddingLeft + (width - letterRect.width()) / 2f,
                paddingTop + (height + space) * index + height,
                textPaint
            )
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event ?: return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (selectIndex != -1) {
                    selectEndListener?.invoke(letters[selectIndex], letterRect)
                }
            }
            MotionEvent.ACTION_DOWN -> {
                if (handleListener == null && clickListener == null) {
                    performClick()
                }
                handleTouch(event)
            }
            MotionEvent.ACTION_MOVE -> {
                handleTouch(event)
            }
        }
        return true
    }

    private fun handleTouch(event: MotionEvent) {
        val y = event.y - paddingTop
        var index = (y / (letterRect.height() + space)).toInt()
        index = index.coerceAtLeast(0)
        index = index.coerceAtMost(letters.size - 1)
        if (index < 0 || index > letters.size) {
            return
        }
        val s = letters[index]
        if (selectIndex != index) {
            selectIndex = index
        } else {
            return
        }
        selectLetters.clear()
        selectLetters.add(s)
        selectRect(index)
        if (handleListener?.invoke(s, selectRect) != true) {
            defHandSelect(s, index)
            clickListener?.invoke(s, selectRect)
        }
    }

    private fun selectRect(index: Int) {
        selectRect.set(letterRect)
        selectRect.bottom =
            (paddingTop + letterRect.height() * (index + 1) + space * index).toInt()
        selectRect.top = selectRect.bottom - letterRect.height()
    }

    private fun defHandSelect(s: String, index: Int) {
        if (letters.size <= 9) {
            select(s)
        } else {
            when {
                index < 2 -> {
                    select(letters[0], letters[1], letters[2], letters[3], letters[4])
                }
                index > letters.size - 3 -> {
                    select(
                        letters[letters.size - 5],
                        letters[letters.size - 4],
                        letters[letters.size - 3],
                        letters[letters.size - 2],
                        letters[letters.size - 1]
                    )
                }
                else -> {
                    select(
                        letters[index - 2],
                        letters[index - 1],
                        s,
                        letters[index + 1],
                        letters[index + 2]
                    )
                }
            }
        }
    }
}