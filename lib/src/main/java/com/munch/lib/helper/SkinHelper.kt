package com.munch.lib.helper

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.LayoutInflaterCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.R
import com.munch.lib.extend.catch
import com.munch.lib.extend.idStr
import com.munch.lib.log.LogStyle
import com.munch.lib.log.Logger

/**
 * 用于实时更换特定的属性值
 *
 * 通过设置LayoutInflater.Factory2来拦截xml的解析，收集view和其可更换属性，并在调用[update]是更换
 *
 * [add]方法同理
 *
 * Create by munch1182 on 2022/6/17 10:42.
 */
class SkinHelper {

    companion object {

        private const val attr_textColor = "textColor"
        private const val attr_background = "background"
        private const val attr_backgroundTint = "backgroundTint"
        private const val attr_foreground = "foreground"
        private const val attr_foregroundTint = "foregroundTint"
        private const val attr_text = "text"

        private val supportAttrsName = mutableListOf(
            attr_textColor, attr_background, attr_backgroundTint,
            attr_foreground, attr_foregroundTint, attr_text
        )

        internal val log = Logger("skin", style = LogStyle.THREAD, enable = false)

        fun loadSkin(context: Context, path: String) = Loader.loadResource(context, path)

        fun getColor(context: Context, resId: Int) = Loader.getColor(context, resId)

        fun getString(context: Context, resId: Int) = Loader.getString(context, resId)

        fun getDrawable(context: Context, resId: Int) = Loader.getDrawable(context, resId)

        fun hasSkin() = Loader.hasSkin()
    }

    private val views = mutableListOf<SkinView>()
    private var update: (SkinHelper.() -> Unit)? = null
    private var currResFlog = -1

    /**
     * 设置资源解析，必须在Activity的onCreate之前调用
     */
    fun apply(activity: AppCompatActivity) {
        log.log { "SkinHelper apply." }
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                log.log { "SkinHelper update check." }
                updateIfChange()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                log.log { "SkinHelper clear." }
                views.clear()
            }
        })
        LayoutInflaterCompat.setFactory2(activity.layoutInflater, SkinFactory(activity.delegate))
    }

    fun add(view: View, attr: MutableSet<SkinAttr>) {
        val sv = SkinView(view, attr)
        sv.applySkinIfCan()
        collectSV(sv)
    }

    /**
     * 更新当前[Loader]所加载的资源
     */
    fun update() {
        views.forEach { it.applySkinIfCan() }
        update?.invoke(this)
        currResFlog = Loader.resourceFlag
    }

    fun reset() {
        Loader.resetSkin()
        update()
    }

    fun onUpdate(update: SkinHelper.() -> Unit): SkinHelper {
        this.update = update
        return this
    }

    private fun updateIfChange() {
        if (currResFlog != Loader.resourceFlag) {
            log.log { "update when change: $currResFlog, ${Loader.resourceFlag}" }
            update()
        }
    }

    private inner class SkinFactory(private val delegate: AppCompatDelegate?) :
        LayoutInflater.Factory2 {
        override fun onCreateView(
            parent: View?,
            name: String,
            context: Context,
            attrs: AttributeSet
        ): View? {
            var isSupportSkin = false
            context.obtainStyledAttributes(attrs, R.styleable.SkinSupport).apply {
                catch { isSupportSkin = this.getBoolean(R.styleable.SkinSupport_skinEnable, false) }
            }.recycle()
            if (!isSupportSkin) {
                return null
            }
            val view = delegate?.createView(parent, name, context, attrs) ?: return null
            analyzeAttr2SkinView(view, attrs)
            return view
        }

        override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
            return null
        }
    }

    private fun analyzeAttr2SkinView(view: View, set: AttributeSet) {
        val sv = SkinView(view)
        //注意：此处的set必须注明才能被解析到，此处是不能解析到view的默认设置的
        for (i in 0 until set.attributeCount) {
            val name = set.getAttributeName(i) ?: continue
            if (!isSupportSkinByAttrName(name)) {
                continue
            }
            val value = set.getAttributeValue(i)
            val skinAttr = analyzeAttr(view.context, name, value)
            log.log {
                "analyzeAttr ${view::class.simpleName}(${view.idStr()} $name($value)): ${skinAttr != null}."
            }
            if (skinAttr != null) {
                sv.attrs.add(skinAttr)
            }
        }
        collectSV(sv)
    }

    private fun collectSV(sv: SkinView) {
        if (views.contains(sv)) {
            views[views.indexOf(sv)].attrs.addAll(sv.attrs)
        } else {
            views.add(sv)
        }
    }

    private fun analyzeAttr(context: Context, attrName: String, attrValue: String): SkinAttr? {
        return if (attrValue.startsWith("@")) {  //以@开头，如引用@color/white, 其attrValue即资源id
            SkinAttrFactory.toSkinAttr(attrName, attrValue.substring(1).toInt())
        } else if (attrValue.startsWith("?")) {  //以?开头，诸如?colorPrimary, 其attrValue为资源id，但需要通过主题去查找
            val resId = attrValue.substring(1).toInt()
            getResIdFromTheme(context, resId)?.let { SkinAttrFactory.toSkinAttr(attrName, it) }
        } else { //否则则是被写死的资源
            null
        }
    }

    private fun getResIdFromTheme(context: Context, attrId: Int): Int? {
        val typedValue = TypedValue()
        val success = context.theme.resolveAttribute(attrId, typedValue, true)
        return if (success) typedValue.resourceId else null
    }

    private fun isSupportSkinByAttrName(attrName: String): Boolean {
        return attrName in supportAttrsName
    }

    sealed class SkinAttr(val name: String, val resId: Int) {
        class TextColor(resId: Int) : SkinAttr(attr_textColor, resId)
        class Background(resId: Int) : SkinAttr(attr_background, resId)
        class BackgroundTint(resId: Int) : SkinAttr(attr_backgroundTint, resId)
        class Foreground(resId: Int) : SkinAttr(attr_foreground, resId)
        class ForegroundTint(resId: Int) : SkinAttr(attr_foregroundTint, resId)
        class Text(resId: Int) : SkinAttr(attr_text, resId)

        override fun toString() = "$name($resId)"

        override fun hashCode(): Int = resId

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SkinAttr) return false
            return other.hashCode() == hashCode()
        }
    }

    private object SkinAttrFactory {

        fun toSkinAttr(attrName: String, attrId: Int): SkinAttr {
            return when (attrName) {
                attr_textColor -> SkinAttr.TextColor(attrId)
                attr_background -> SkinAttr.Background(attrId)
                attr_backgroundTint -> SkinAttr.BackgroundTint(attrId)
                attr_foreground -> SkinAttr.Foreground(attrId)
                attr_foregroundTint -> SkinAttr.ForegroundTint(attrId)
                attr_text -> SkinAttr.Text(attrId)
                else -> throw IllegalStateException()
            }
        }

        fun applySkinAttr(view: View, attr: SkinAttr) {
            log.log { "applySkinAttr: ${view::class.simpleName}(${view.idStr()} $attr)" }
            val ctx = view.context
            val name = catch { ctx.resources.getResourceTypeName(attr.resId) } ?: return
            when (attr) {
                is SkinAttr.Background -> {
                    if (name == "color") {
                        view.setBackgroundColor(Loader.getColor(ctx, attr.resId))
                    } else if (name == "drawable") {
                        view.background = Loader.getDrawable(ctx, attr.resId)
                    }
                }
                is SkinAttr.BackgroundTint -> {
                    view.backgroundTintList = ColorStateList(
                        arrayOf(intArrayOf()),
                        intArrayOf(Loader.getColor(ctx, attr.resId))
                    )
                }
                is SkinAttr.Foreground -> {
                    if (name == "color") {
                        view.foreground = ColorDrawable(Loader.getColor(ctx, attr.resId))
                    } else if (name == "drawable") {
                        view.foreground = Loader.getDrawable(ctx, attr.resId)
                    }
                }
                is SkinAttr.ForegroundTint -> {
                    view.foregroundTintList = ColorStateList(
                        arrayOf(intArrayOf()),
                        intArrayOf(Loader.getColor(ctx, attr.resId))
                    )
                }
                is SkinAttr.Text -> {
                    if (view is TextView) {
                        view.text = Loader.getString(ctx, attr.resId)
                    }
                }
                is SkinAttr.TextColor -> {
                    if (view is TextView) {
                        view.setTextColor(Loader.getColor(ctx, attr.resId))
                    }
                }
            }
        }
    }

    private class SkinView(
        val view: View,
        val attrs: MutableSet<SkinAttr> = mutableSetOf()
    ) {

        fun applySkinIfCan() {
            attrs.forEach { SkinAttrFactory.applySkinAttr(view, it) }
        }

        override fun hashCode(): Int {
            return view.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SkinView) return false
            return hashCode() == other.hashCode()
        }
    }

    /**
     * 通过皮肤包加载Resources做替换
     */
    private object Loader {

        private var skinPkgName: String? = null
        private var skinResources: Resources? = null
        var resourceFlag = -1

        fun hasSkin() = skinPkgName != null && skinResources != null

        @Suppress("DEPRECATION")
        fun loadResource(context: Context, skinPath: String): Boolean {
            try {
                val info = context.packageManager.getPackageArchiveInfo(
                    skinPath,
                    PackageManager.GET_ACTIVITIES
                ) ?: return false

                skinPkgName = info.packageName

                val assetManager = AssetManager::class.java.newInstance()
                val method = AssetManager::class.java.getMethod("addAssetPath", String::class.java)
                method.invoke(assetManager, skinPath)

                skinResources = Resources(
                    assetManager,
                    context.resources.displayMetrics,
                    context.resources.configuration
                )
                resourceFlag = skinResources?.hashCode() ?: -1
                log.log { "load Resource($skinPath) success." }
            } catch (e: Exception) {
                log.log { "load Resource($skinPath) fail: ${e.localizedMessage}." }
                return false
            }
            return true
        }

        fun getColor(context: Context, resId: Int): Int {
            val id = getIdFromSkin(context, resId) ?: return ContextCompat.getColor(context, resId)
            return skinResources!!.getColor(id, null)
        }

        fun getString(context: Context, resId: Int): String {
            val id = getIdFromSkin(context, resId) ?: return context.getString(resId)
            return skinResources!!.getString(id)
        }

        fun getDrawable(context: Context, resId: Int): Drawable? {
            val id = getIdFromSkin(context, resId) ?: return context.getDrawable(resId)
            return skinResources!!.getDrawable(id, null)
        }

        private fun getIdFromSkin(context: Context, redId: Int): Int? {
            val resourceEntryName = context.resources.getResourceEntryName(redId)
            val resourceTypeName = context.resources.getResourceTypeName(redId)
            return skinResources?.getIdentifier(resourceEntryName, resourceTypeName, skinPkgName)
                ?.takeIf { it != 0 }
        }

        fun resetSkin() {
            skinPkgName = null
            skinResources = null
        }
    }
}