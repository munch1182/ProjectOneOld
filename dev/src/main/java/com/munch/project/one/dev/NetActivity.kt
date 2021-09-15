package com.munch.project.one.dev

import android.os.Bundle
import android.view.View
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.munch.lib.fast.base.BaseBigTextTitleActivity
import com.munch.lib.fast.base.get
import com.munch.lib.fast.recyclerview.SimpleAdapter
import com.munch.project.one.dev.databinding.ActivityNetBinding
import com.munch.project.one.dev.databinding.ItemNetBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Create by munch1182 on 2021/9/15 14:15.
 */
class NetActivity : BaseBigTextTitleActivity() {

    private val bind by bind<ActivityNetBinding>(R.layout.activity_net)
    private val vm by get(NetViewModel::class.java)
    private val artAdapter by lazy {
        SimpleAdapter<Articles, ItemNetBinding>(R.layout.item_net) { _, db, bean -> db.art = bean }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setView()
        onStateInitLoad()
        registerOnStateResult()

        //TEST
        /*onStateSuccess(mutableListOf(Articles("123", "123", "123", "123", "123")))*/
    }

    @Suppress("UNCHECKED_CAST")
    private fun registerOnStateResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.articles().collect {
                    when (it) {
                        is StateUIByLoad.Success<*> -> onStateSuccess(it.value as MutableList<Articles>)
                        is StateUIByLoad.Error -> onStateError(it.e)
                        is StateUIByLoad.None -> {
                        }
                    }
                }
            }
        }
    }

    private fun onStateError(e: Throwable) {
        showInitedView()
    }

    private fun onStateSuccess(list: MutableList<Articles>) {
        artAdapter.set(list.toMutableList())
        showInitedView()
    }

    private fun showInitedView() {
        bind.apply {
            /*netSrl.visibility = View.VISIBLE
            netSrl.isRefreshing = false*/
            netInitSrl.visibility = View.GONE
        }
    }

    private fun setView() {
        bind.netRv.apply {
            layoutManager = LinearLayoutManager(this@NetActivity)
            adapter = artAdapter
        }
        /*bind.netSrl.setOnRefreshListener { vm.refresh() }*/
    }

    //初始化加载即xml默认状态
    private fun onStateInitLoad() {
    }
}

class NetViewModel : ViewModel() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.wanandroid.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    companion object {
        private const val PAGE_FIRST = 0
    }

    private var page = PAGE_FIRST
    private val api = retrofit.create(Api::class.java)

    private val list: MutableList<Articles> = mutableListOf()
    private val articles = MutableStateFlow<StateUIByLoad>(StateUIByLoad.None)
    fun articles(): StateFlow<StateUIByLoad> = articles
    private var isQueried = false

    init {
        query()
    }

    private fun query() {
        if (isQueried) {
            return
        }
        isQueried = true
        viewModelScope.launch {
            val res = api.queryArticles(page)
            if (res.errorCode == 0) {
                articles.emit(StateUIByLoad.Success(res.data?.datas ?: emptyList()))
            } else {
                articles.emit(StateUIByLoad.Error(IllegalStateException(res.errorMsg)))
            }
            isQueried = false
        }
    }

    fun loadMode() {
        page++
        query()
    }

    fun refresh() {
        page = PAGE_FIRST
        list.clear()
        query()
    }

}

sealed class StateUIByLoad {
    object None : StateUIByLoad()
    data class Success<T>(val value: T) : StateUIByLoad()
    data class Error(val e: Throwable) : StateUIByLoad()

}

interface Api {

    @GET("article/list/{page}/json")
    suspend fun queryArticles(
        @Path("page") page: Int,
        @Query("page_size") pageSize: Int = 15
    ): ResWrapper<ResArticles>
}

data class ResWrapper<T>(val errorCode: Int, val errorMsg: String, val data: T?)

data class ResArticles(val curPage: Int, val datas: List<Articles>?)

data class Articles(
    val title: String,
    val link: String,
    val author: String,
    val shareUser: String,
    val superChapterName: String
) {

    val showName: String
        get() = if (author.isEmpty()) shareUser else author
}