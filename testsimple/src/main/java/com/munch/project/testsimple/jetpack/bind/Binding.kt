package com.munch.project.testsimple.jetpack.bind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.get
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.munch.lib.base.BaseRootActivity
import com.munch.lib.test.TestBaseTopActivity
import com.munch.project.testsimple.R

/**
 * Create by munch1182 on 2020/12/7 13:42.
 */
inline fun <reified T : ViewDataBinding> BaseRootActivity.binding(@LayoutRes resId: Int): Lazy<T> =
    lazy { DataBindingUtil.setContentView(this, resId) }

/**
 * 因为[TestBaseTopActivity]中的[R.layout.activity_base_top]并没有进行viewbind即被layout包裹，
 * 因此不能直接使用[BaseRootActivity.binding]
 * 此处调用[DataBindingUtil.bind]方法直接绑定来解决这个问题，
 * 但是此方法必须要设置[DataBindingUtil.sDefaultComponent],此处直接设置全局[DataBindingUtil.setDefaultComponent]
 * 意味着所有的bind方法都需要写在[GlobeViewBinding]中
 * 如有单独的bind方法需要调用[DataBindingUtil.inflate]中带DataBindingComponent参数的方法而不是调用此方法
 *
 * 另一种解决办法是重写[TestBaseTopActivity]通过判断是否需要viewbind，通过动态添加的方式添加[R.layout.activity_base_top],
 * 这样[DataBindingUtil.sDefaultComponent]就交由as去生成判断了
 *
 */
inline fun <reified T : ViewDataBinding> TestBaseTopActivity.bindingTop(@LayoutRes resId: Int): Lazy<T> {
    return lazy {
        setContentView(resId)
        if (DataBindingUtil.getDefaultComponent() == null) {
            DataBindingUtil.setDefaultComponent(DefGlobeViewBinding())
        }
        return@lazy DataBindingUtil.bind<T>(findViewById<ViewGroup>(R.id.top_container)[1])!!
    }
}

inline fun <reified T : ViewDataBinding> binding(parent: ViewGroup, @LayoutRes resId: Int): T {
    return DataBindingUtil.inflate(LayoutInflater.from(parent.context), resId, parent, false)
}

inline fun <reified T : ViewDataBinding> ViewGroup.inflateByBing(@LayoutRes resId: Int) =
    binding<T>(this, resId)

/**
 * 注意：DataBindingComponent会在使用[BindingAdapter]时自动编译，因此需要依项目而定
 */
class DefGlobeViewBinding : DataBindingComponent {
    override fun getGlobeViewBinding(): GlobeViewBinding {
        return GlobeViewBinding()
    }
}
