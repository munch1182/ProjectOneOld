package com.munch.project.test.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.munch.project.test.R

/**
 * 也可以通过ItemDecoration来绘制左侧letters，参考[HeaderItemDecoration]
 *
 * Create by munch1182 on 2020/12/24 15:59.
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

    private val chars = arrayListOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
    )
    private val letters: ArrayList<String> = arrayListOf()
        get() {
            if (isInEditMode) {
                return chars
            }
            return field
        }
    private val selectLetters: ArrayList<String> = arrayListOf()
        get() {
            if (isInEditMode) {
                return arrayListOf("A", "B", "C", "D", "E")
            }
            return field
        }
    private val textPaint: Paint
    private var space: Float = 10f
        set(value) {
            field = if (maxSpace != -1f && value > maxSpace) {
                maxSpace
            } else {
                value
            }
        }
    private var maxSpace: Float = -1f
    private val letterRect = Rect()
    private val selectRect = Rect()
    private var textColor: Int = Color.TRANSPARENT
    private var selectColor: Int = Color.TRANSPARENT
    private var selectIndex: Int = -1

    /**
     * 返回值：true则需要自行调用[select]
     */
    var handleListener: ((letter: String, rect: Rect) -> Boolean)? = null
    var clickListener: ((letter: String, rect: Rect) -> Unit)? = null
    var selectEndListener: ((letter: String, rect: Rect) -> Unit)? = null

    fun setLetters(letters: List<String>) {
        this.letters.clear()
        this.letters.addAll(letters)
        requestLayout()
        invalidate()
    }

    fun setAllLetters() {
        setLetters(chars)
    }

    fun select(vararg pos: Int) {
        select(*Array(letters.size) {
            letters[pos[it]]
        })
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

    fun getLettersList() = letters

    fun addLetters(vararg letter: String) {
        letters.addAll(letter)
        invalidate()
    }

    fun addLetter(index: Int, letter: String) {
        letters.add(index, letter)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
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

        if (modeHeight == MeasureSpec.EXACTLY) {
            space = if (sizeHeight > letterAllHeight) {
                (sizeHeight - letterAllHeight) / (letters.size - 1f)
            } else {
                0f
            }
        } else {
            sizeHeight = (letterAllHeight + space * (letters.size - 1)).toInt()
        }
        setMeasuredDimension(sizeWidth, sizeHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (letters.isEmpty()) {
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

            canvas?.drawText(
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
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
            clickListener?.invoke(s, selectRect)
        }
    }

    private fun selectRect(index: Int) {
        selectRect.set(letterRect)
        selectRect.bottom =
            (paddingTop + letterRect.height() * (index + 1) + space * index).toInt()
        selectRect.top = selectRect.bottom - letterRect.height()
    }

    init {
        val attrsSet =
            context.obtainStyledAttributes(attrs, R.styleable.LetterNavigationBarView)
        textColor =
            attrsSet.getColor(
                R.styleable.LetterNavigationBarView_letter_textColor,
                Color.parseColor("#574A4A")
            )
        val textSize =
            attrsSet.getDimension(R.styleable.LetterNavigationBarView_letter_textSize, 40f)
        selectColor =
            attrsSet.getColor(
                R.styleable.LetterNavigationBarView_letter_selectColor,
                Color.parseColor("#574A4A")
            )
        maxSpace = attrsSet.getDimension(R.styleable.LetterNavigationBarView_letter_max_space, -1f)
        attrsSet.recycle()
        this.textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.textSize = textSize
        }
    }
}