package com.munch.project.one.skin

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.ColorStateList
import android.content.res.Resources
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
import androidx.core.view.ViewCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.munch.lib.extend.catch
import com.munch.project.one.R

/**
 * Create by munch1182 on 2022/6/17 10:42.
 */
class SkinHelper {

    companion object {
        private val supportAttrsName = mutableListOf(
            "textColor",
            "background",
            "backgroundTint",
            "foreground",
            "foregroundTint",
            "text"
        )
    }

    private val views = mutableListOf<SkinView>()

    fun apply(activity: AppCompatActivity) {
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                super.onCreate(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                views.clear()
            }
        })
        LayoutInflaterCompat.setFactory2(activity.layoutInflater, SkinFactory(activity.delegate))
    }

    fun update() {
        views.forEach { it.applySkinIfCan() }
    }

    private class SkinAttr(val name: String, val resId: Int)

    private class SkinView(
        val view: View,
        val attrs: MutableList<SkinAttr> = mutableListOf()
    ) {
        fun applySkinIfCan() {
            if (!ViewCompat.isAttachedToWindow(view)) {
                return
            }
            val ctx = view.context
            attrs.forEach { applyAttr(ctx, it) }
        }

        private fun applyAttr(ctx: Context, attr: SkinAttr) {
            when (ctx.resources.getResourceTypeName(attr.resId)) {
                "color" -> {
                    val color = Loader.getColor(ctx, attr.resId)
                    when (attr.name) {
                        "textColor" -> {
                            if (view is TextView) {
                                view.setTextColor(color)
                            }
                        }
                        "backgroundTint" -> {
                            view.backgroundTintList =
                                ColorStateList(arrayOf(), intArrayOf(color))
                        }
                        "foregroundTint" -> {
                            view.foregroundTintList =
                                ColorStateList(arrayOf(), intArrayOf(color))
                        }
                    }
                }
                "drawable" -> {
                    val drawable = Loader.getDrawable(ctx, attr.resId)
                    when (attr.name) {
                        "background" -> view.background = drawable
                        "foreground" -> view.foreground = drawable
                    }
                }
                "string" -> {
                    val str = Loader.getString(ctx, attr.resId)
                    if (view is TextView) {
                        view.text = str
                    }
                }
            }
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
        for (i in 0 until set.attributeCount) {
            val name = set.getAttributeName(i) ?: continue
            if (!isSupportSkinByAttrName(name)) {
                continue
            }
            val value = set.getAttributeValue(i)
            val skinAttr = analyzeAttr(view.context, name, value)
            if (skinAttr != null) {
                sv.attrs.add(skinAttr)
            }
        }
        views.add(sv)
    }

    private fun analyzeAttr(context: Context, attrName: String, attrValue: String): SkinAttr? {
        return if (attrName.startsWith("@")) {  //以@开头，如引用@color/white, 其attrValue即资源id
            SkinAttr(attrName, attrValue.substring(1).toInt())
        } else if (attrName.startsWith("?")) {  //以?开头，诸如?colorPrimary, 其attrValue为资源id，但需要通过主题去查找
            val resId = attrValue.substring(1).toInt()
            getResIdFromTheme(context, resId)?.let { SkinAttr(attrName, it) }
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

    private object Loader {

        private var skinPkgName: String? = null
        private var skinResources: Resources? = null

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
            } catch (e: Exception) {
                return false
            }
            return true
        }

        fun getColor(context: Context, resId: Int): Int {
            val id = getIdentifier(context, resId)
            if (skinResources == null || id == null) {
                return ContextCompat.getColor(context, resId)
            }
            return skinResources!!.getColor(id, null)
        }

        fun getString(context: Context, resId: Int): String {
            val id = getIdentifier(context, resId)
            if (skinResources == null || id == null) {
                return context.getString(resId)
            }
            return skinResources!!.getString(id)
        }

        fun getDrawable(context: Context, resId: Int): Drawable? {
            val id = getIdentifier(context, resId)
            if (skinResources == null || id == null) {
                return context.getDrawable(resId)
            }
            return skinResources!!.getDrawable(id, null)
        }

        private fun getIdentifier(context: Context, redId: Int): Int? {
            val resourceEntryName = context.resources.getResourceEntryName(redId)
            val resourceTypeName = context.resources.getResourceTypeName(redId)
            return skinResources?.getIdentifier(resourceEntryName, resourceTypeName, skinPkgName)
        }
    }
}