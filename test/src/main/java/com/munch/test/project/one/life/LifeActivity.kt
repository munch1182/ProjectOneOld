package com.munch.test.project.one.life

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import com.munch.pre.lib.extend.obOnResume
import com.munch.pre.lib.extend.obOnStart
import com.munch.pre.lib.log.Logger
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseFragment
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityLifeBinding
import com.munch.test.project.one.databinding.FragmentLifeBinding

/**
 * Create by munch1182 on 2021/5/20 9:19.
 */
class LifeActivity : BaseTopActivity() {

    companion object {

        var list: MutableList<String>? = null
            get() {
                if (field == null) {
                    field = mutableListOf()
                }
                return field
            }
    }

    private val log = Logger().apply {
        noInfo = true
        tag = "life"
        setListener { msg, _ -> list?.add(msg) }
    }
    private val bind by bind<ActivityLifeBinding>(R.layout.activity_life)

    init {
        log.log("Activity structure")
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        log.log("Activity attachBaseContext")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        log.log("Activity onWindowFocusChanged")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log.log("Activity onCreate")
        bind.lifecycleOwner = this
        supportFragmentManager.beginTransaction()
            .replace(R.id.life_container, LifeFragment())
            .commitNowAllowingStateLoss()
        delayLoad { log.log("Activity delayLoad") }

        obOnStart({ log.log("Lifecycle onStart") },
            { log.log("Lifecycle onStop") },
            { log.log("Lifecycle onDestroy") })
        obOnResume({ log.log("Lifecycle onResume") },
            { log.log("Lifecycle onPause") },
            { log.log("Lifecycle onDestroy2") })
    }

    override fun onStart() {
        super.onStart()
        log.log("Activity onStart")
    }

    override fun onResume() {
        super.onResume()
        log.log("Activity onResume")
    }

    override fun onStop() {
        super.onStop()
        log.log("Activity onStop")
    }

    override fun onPause() {
        super.onPause()
        log.log("Activity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        log.log("Activity onDestroy")
        list?.clear()
        list = null
    }

}

class LifeFragment : BaseFragment() {

    private val log = Logger().apply {
        noInfo = true
        tag = "life"
        setListener { msg, _ -> LifeActivity.list?.add(msg) }
    }

    private val bind by bind<FragmentLifeBinding>(R.layout.fragment_life)

    init {
        log.log("Fragment structure")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log.log("Fragment onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        log.log("Fragment onCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        log.log("Fragment onViewCreated")
        delayLoad {
            log.log("Fragment delayLoad")
            val list = arrayOf(
                "==============",
                "setText()",
                "View onMeasure",
                "ViewGroup onMeasure",
                "ViewGroup onSizeChanged",
                "View onSizeChanged",
                "View onLayout",
                "ViewGroup onLayout",
                "ViewGroup onDraw",
                "View onDraw",
                "==============",
                "Lifecycle onPause",
                "Fragment onPause",
                "Activity onPause",
                "Activity onWindowFocusChanged",
                "Lifecycle onStop",
                "Fragment onStop",
                "Activity onStop",
                "Lifecycle onDestroy2",
                "Lifecycle onDestroy",
                "View onDetachedFromWindow",
                "ViewGroup onDetachedFromWindow",
                "Fragment onDestroyView",
                "Fragment onDestroy",
                "Fragment onDetach",
                "Activity onDestroy"
            )
            LifeActivity.list?.addAll(list)
            val text = LifeActivity.list?.joinToString("\n") ?: "null"
            bind.lifeTv.setText(text)
            bind.lifeTv.removeListener()
            bind.lifeVg.removeListener()
        }
    }

    override fun onResume() {
        super.onResume()
        log.log("Fragment onResume")
    }

    override fun onStart() {
        super.onStart()
        log.log("Fragment onStart")
    }

    override fun onStop() {
        super.onStop()
        log.log("Fragment onStop")
    }

    override fun onPause() {
        super.onPause()
        log.log("Fragment onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        log.log("Fragment onDestroyView")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        log.log("Fragment onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        log.log("Fragment onDetach")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        log.log("Fragment onHiddenChanged:$hidden")
    }

    override fun onDestroy() {
        super.onDestroy()
        log.log("Fragment onDestroy")
        log.setListener()
    }
}

class TestVG @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

    private val log = Logger().apply {
        noInfo = true
        tag = "life"
        setListener { msg, _ -> LifeActivity.list?.add(msg) }
    }

    init {
        log.log("ViewGroup structure")
        setWillNotDraw(false)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        log.log("ViewGroup onLayout")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        log.log("ViewGroup onMeasure")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        log.log("ViewGroup onDraw")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        log.log("ViewGroup onSizeChanged")
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        log.log("ViewGroup onFinishInflate")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        log.log("ViewGroup onAttachedToWindow")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        log.log("ViewGroup onDetachedFromWindow")
        log.setListener()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        log.log("ViewGroup dispatchTouchEvent")
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        log.log("ViewGroup dispatchTouchEvent")
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        log.log("ViewGroup onInterceptTouchEvent")
        return super.onInterceptTouchEvent(ev)
    }

    fun removeListener() {
        log.setListener()
    }
}

class TestView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val log = Logger().apply {
        noInfo = true
        tag = "life"
        setListener { msg, _ -> LifeActivity.list?.add(msg) }
    }
    private var text: String? = null
    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
    }

    init {
        log.log("View structure")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        log.log("View onLayout")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        log.log("View onMeasure")
        val fm = paint.fontMetrics
        val h = (LifeActivity.list?.size ?: 0) * (fm.descent - fm.ascent)
        val w = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(w, h.toInt())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        log.log("View onDraw")
        canvas ?: return
        text ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            canvas.save()
            StaticLayout.Builder.obtain(text!!, 0, text!!.length, paint, width).build().draw(canvas)
            canvas.restore()
        } else {
            canvas.drawText("版本低于23", 0f, 30f, paint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        log.log("View onSizeChanged")
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        log.log("View onFinishInflate")
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        log.log("View onAttachedToWindow")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        log.log("View onDetachedFromWindow")
        log.setListener()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        log.log("View dispatchTouchEvent")
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        log.log("View dispatchTouchEvent")
        return super.onTouchEvent(event)
    }

    fun setText(text: String) {
        this.text = text
        requestLayout()
        invalidate()
    }

    fun removeListener() {
        log.setListener()
    }
}