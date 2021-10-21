package com.munch.lib.state

import androidx.annotation.IntDef
import java.lang.annotation.Inherited

/**
 * Create by munch1182 on 2021/10/21 15:43.
 */
class ViewDataStateHelper(
    private val viewChange: ViewChange,
    private val request: Request,
    private val FIRST_PAGE: Int = 0
) {

    @ViewState
    private var viewState: Int = ViewState.INIT
        set(value) {
            if (field != value) {
                field = value
                viewChange.invoke(field)
            }
        }

    @DataState
    private var dataState: Int = DataState.IDLE

    private var currentPage = FIRST_PAGE
        set(value) {
            if (field != value) {
                field = value
                request.invoke(field)
            }
        }

    /**
     * 执行刷新操作时调用
     */
    fun refresh() {
        if (viewState == ViewState.REFRESH) {
            return
        }
        viewState = ViewState.REFRESH
        dataState = DataState.REQUEST
        currentPage = FIRST_PAGE
    }

    /**
     * 执行加载更多时调用
     *
     * @param page 指定页数
     */
    fun more(page: Int) {
        if (page == currentPage) {
            return
        }
        viewState = ViewState.MORE
        dataState = DataState.REQUEST
        currentPage = page
    }

    /**
     * 执行下一页时调用
     */
    fun next() = more(currentPage + 1)

    /**
     * 加载成功时回调
     */
    fun success() {
        dataState = DataState.IDLE
        viewState = ViewState.LOADED
    }

    /**
     * 加载失败时回调
     */
    fun fail() {
        dataState = DataState.IDLE
        when (viewState) {
            ViewState.INIT -> viewState = ViewState.FAIL_INIT
            ViewState.REFRESH -> viewState = ViewState.FAIL_REFRESH
            ViewState.MORE -> {
                currentPage--
                viewState = ViewState.FAIL_MORE
            }
        }
    }
}

@IntDef(
    ViewState.INIT,
    ViewState.LOADED,
    ViewState.REFRESH,
    ViewState.MORE,
    ViewState.FAIL_INIT,
    ViewState.FAIL_REFRESH,
    ViewState.FAIL_MORE
)
@Retention(AnnotationRetention.SOURCE)
@Inherited
annotation class ViewState {
    companion object {

        //显示初始化
        const val INIT = 0

        //显示数据
        const val LOADED = 1

        //显示刷新中
        const val REFRESH = 2

        //显示加载中
        const val MORE = 3

        //显示初始化失败
        const val FAIL_INIT = 4

        //显示刷新失败
        const val FAIL_REFRESH = 5

        //显示加载更多失败
        const val FAIL_MORE = 6
    }
}

@IntDef(DataState.IDLE, DataState.REQUEST)
@Retention(AnnotationRetention.SOURCE)
@Inherited
annotation class DataState {

    companion object {
        //未执行数据操作
        const val IDLE = 0

        //正在请求数据
        const val REQUEST = 1
    }
}

typealias Request = (page: Int) -> Unit
typealias ViewChange = (state: Int) -> Unit