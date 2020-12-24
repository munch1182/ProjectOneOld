package com.munch.project.test.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.munch.project.test.R

/**
 * Create by munch1182 on 2020/12/24 15:59.
 */
class LetterNavigationBarView : View {

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        val attrsSet =
            context.obtainStyledAttributes(attrs, R.styleable.LetterNavigationBarView)
        val color =
            attrsSet.getColor(
                R.styleable.LetterNavigationBarView_letter_textColor,
                Color.RED
            )
        val textSize =
            attrsSet.getDimension(R.styleable.LetterNavigationBarView_letter_textSize, 40f)
        val selectColor =
            attrsSet.getColor(
                R.styleable.LetterNavigationBarView_letter_selectColor,
                Color.parseColor("#40e0d6")
            )
        attrsSet.recycle()
        this.textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.textSize = textSize
        }
        this.selectPaint = Paint(textPaint).apply {
            this.color = selectColor
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    private val chars = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
    )
    private val letters: ArrayList<String> = arrayListOf()
        get() {
            if (isInEditMode) {
                letters.addAll(chars)
            }
            return field
        }
    private var listener: OnChoseListener? = null
    private val textPaint: Paint
    private val selectPaint: Paint
    private var space: Float = 20f
    private val letterRect = Rect()

    fun setLetters(letters: List<String>) {
        this.letters.clear()
        this.letters.addAll(letters)
        invalidate()
    }

    fun setAllLetters() {
        setLetters(chars)
    }

    fun choseOne(letter: String) {
        if (!letters.contains(letter)) {
            return
        }
        scrollTo(letters.indexOf(letter))
    }

    fun scrollTo(pos: Int) {

    }

    fun addLetters(vararg letter: String) {
        letters.addAll(letter)
        /*letters.sort()*/
        invalidate()
    }

    fun setOnChoseListener(listener: OnChoseListener): LetterNavigationBarView {
        this.listener = listener
        return this
    }

    interface OnChoseListener {
        fun onChoseStart(letters: List<String>)
        fun onLetterChose(letter: String)
        fun onChoseEnd(letter: String)
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
        val letterWidth = letterRect.width() + paddingLeft + paddingRight
        if (sizeWidth < letterWidth || modeWidth != MeasureSpec.EXACTLY) {
            sizeWidth = letterWidth
        }
        val letterOneHeight = letterRect.height() + space
        val letterAllHeight = letterOneHeight * letters.size + paddingTop + paddingBottom
        if (sizeHeight > letterAllHeight) {
            space = (sizeHeight - letterAllHeight) / (letters.size - 1)
        } else {
            space = 0f
            sizeHeight = letterAllHeight.toInt()
        }
        setMeasuredDimension(sizeWidth, sizeHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (letters.isEmpty()) {
            return
        }
        val height = letterRect.height() + space
        val width = letterRect.width()
        letters.forEachIndexed { index, s ->
            canvas?.drawText(s, 0F, (height + space) * index + height, textPaint)
        }
    }
}