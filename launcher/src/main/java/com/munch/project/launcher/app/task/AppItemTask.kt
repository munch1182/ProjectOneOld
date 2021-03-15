package com.munch.project.launcher.app.task

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.munch.lib.BaseApp
import com.munch.lib.dag.Executor
import com.munch.lib.dag.Key
import com.munch.lib.dag.Task
import com.munch.lib.helper.AppHelper
import com.munch.lib.helper.AppInstallReceiver
import com.munch.project.launcher.R
import com.munch.project.launcher.app.App
import com.munch.project.launcher.appitem.AppShowBean
import com.munch.project.launcher.appitem.ShowParameter
import com.munch.project.launcher.db.AppBean
import com.munch.project.launcher.help.preload
import com.munch.project.launcher.set.SettingActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * task：
 * 1. 扫描或者从数据库中获取app列表
 * 2. 根据spanCount填充gridLayoutManager的数据
 * 3. 预加载图标资源
 *
 * Create by munch1182 on 2021/2/25 15:20.
 */
class AppItemHelper private constructor() {

    companion object {
        private val INSTANCE by lazy { AppItemHelper() }

        fun getInstance() = INSTANCE

        private const val SP_COUNT_DEF = 4
    }

    private var char: Char = ' '
    private var charIndex = -1
    private val appsInSpace = mutableListOf<AppShowBean>()
    fun getApps() = appsInSpace
    private val map = linkedMapOf<Char, Int>()
    fun getLetterMap() = map
    private var spanCount = -1
    fun getSpanCount() = spanCount
    private var registered = false

    private var updateFlag = 1
    private val isChange = MutableLiveData(updateFlag)

    /**
     * 用于监听数据更新
     */
    fun isChange(): LiveData<Boolean> = isChange.map { updateFlag % 2 == 0 }

    fun initScanAndSplit() {
        if (spanCount == -1) {
            spanCount = SP_COUNT_DEF
        }
        initScanAndSplit(spanCount)
    }

    private fun initScanAndSplit(spanCount: Int) {
        if (spanCount == this.spanCount && appsInSpace.isNotEmpty()) {
            return
        }
        this.spanCount = spanCount
        val apps = getAppItem()?.map { AppShowBean.new(it) }?.toMutableList() ?: return
        apps.forEach {
            if (it.appBean.icon is Drawable) {
                (it.appBean.icon as Drawable).preload()
            }
        }
        splitGridAppItem(spanCount, apps)
    }

    fun getAppItem(): List<AppBean>? {
        return scanAppFromPhone()
    }

    fun scanAppFromPhone(): List<AppBean>? {
        val pm = App.getInstance().packageManager ?: return null
        return AppHelper.getInstallApp()
            ?.mapIndexed { index, it ->
                val packageName = it.activityInfo.packageName
                if (packageName == App.getInstance().packageName) {
                    AppBean.new(
                        BaseApp.getContext().getString(R.string.app_set),
                        SettingActivity::class.java.canonicalName,
                        packageName,
                        index,
                        it.loadIcon(pm)
                    )
                } else {
                    AppBean.new(
                        /*此方法可以获取被应用更改后的label*/
                        it.loadLabel(pm).toString(),
                        it.activityInfo.name,
                        packageName,
                        index,
                        /*此方法可以获取被应用更改后的icon*/
                        it.loadIcon(pm)
                    )
                }
            }
    }

    fun splitGridAppItem(spanCount: Int, apps: MutableList<AppShowBean>) {
        apps.sort()

        appsInSpace.clear()
        map.clear()

        apps.forEach {
            charIndex++
            //如果该数据的char与上一个char不同，则需要另起一组
            if (it.letterChar != char && charIndex != 0) {
                //给每一组最后一个位置添加一个占据剩余整行的空数据来占位置，需要GridLayoutManager.SpanSizeLookup配合
                //此处计算上一个位置需要添加的空行数
                val i = spanCount - (charIndex - 1) % spanCount - 1
                //如果该组最后一个位置也是该行最后一个则不添加
                if (i != 0) {
                    val parameter = ShowParameter(charIndex)
                    parameter.space2End = i
                    //add
                    appsInSpace.add(AppShowBean.empty(char, parameter))
                }
                charIndex = 0
            }
            char = it.letterChar.toUpperCase()
            it.updateShowParameter(ShowParameter(charIndex))
            //add
            appsInSpace.add(it)
            if (charIndex == 0) {
                map[char] = appsInSpace.size - 1
            }
        }
    }

    private fun update() {
        char = ' '
        charIndex = -1
        appsInSpace.clear()
        initScanAndSplit()
        updateFlag++
        isChange.postValue(updateFlag)
        updateFlag++
    }

    fun registerReceiver(context: Context) {
        if (registered) {
            return
        }
        registered = true
        AppInstallReceiver(context).apply { add { _, _, _ -> update() } }.register()
    }

}

class AppItemTask : Task {

    companion object {

        val key by lazy { Key("app") }
    }

    override fun start(executor: Executor) {
        AppItemHelper.getInstance().initScanAndSplit()
    }

    override val dispatcher: CoroutineDispatcher
        get() = Dispatchers.IO
    override val uniqueKey: Key
        get() = key

    override fun dependsOn(): MutableList<Key> {
        return mutableListOf()
    }


}