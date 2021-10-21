package com.munch.lib.fast.base

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.munch.lib.base.toImmutable
import com.munch.lib.state.ViewDataStateHelper
import com.munch.lib.state.ViewState

/**
 * Create by munch182 on 2021/10/21 16:21.
 */
interface IViewState {

    fun onInit() {}

    fun onLoaded() {}

    fun onRefresh() {}

    fun onMore() {}

    fun onFailInit() {}

    fun onFailRefresh() {}

    fun onFailMore() {}
}

open class BaseBigTextTitleWithStateActivity : BaseBigTextTitleActivity(), IViewState {

    private var stateVm: ViewStateViewModel? = null

    fun <VM : ViewStateViewModel> ViewModelStoreOwner.get(target: Class<VM>): Lazy<VM> {
        return lazy { ViewModelProvider(this).get(target).apply { stateVm = this } }
    }

    protected open val vh = ViewDataStateHelper({
        when (it) {
            ViewState.INIT -> onInit()
            ViewState.LOADED -> onLoaded()
            ViewState.REFRESH -> onRefresh()
            ViewState.MORE -> onMore()
            ViewState.FAIL_INIT -> onFailInit()
            ViewState.FAIL_REFRESH -> onFailRefresh()
            ViewState.FAIL_MORE -> onFailMore()
        }
    }, { stateVm?.request(it) })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateVm?.loadResult()?.observe(this) {
            if (it) vh.success() else vh.fail()
        }
    }
}

open class ViewStateViewModel : ViewModel() {

    private val loadResult = MutableLiveData<Boolean>()
    fun loadResult() = loadResult.toImmutable()
    fun request(page: Int) {}


}