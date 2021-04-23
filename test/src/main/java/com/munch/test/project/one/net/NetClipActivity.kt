package com.munch.test.project.one.net

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.annotation.IntDef
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.munch.lib.fast.base.BaseBindMultiAdapter
import com.munch.lib.fast.base.BaseBindViewHolder
import com.munch.lib.fast.extend.get
import com.munch.pre.lib.base.rv.BaseAdapter
import com.munch.pre.lib.base.rv.MultiTypeWithPos
import com.munch.pre.lib.extend.*
import com.munch.pre.lib.helper.AppHelper
import com.munch.pre.lib.helper.ImHelper
import com.munch.pre.lib.helper.NetStatusHelper
import com.munch.pre.lib.log.log
import com.munch.test.project.one.R
import com.munch.test.project.one.base.BaseTopActivity
import com.munch.test.project.one.databinding.ActivityNetClipBinding
import com.munch.test.project.one.databinding.ItemChatFromBinding
import com.munch.test.project.one.databinding.ItemChatSendBinding
import com.munch.test.project.one.databinding.ItemChatSystemBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Create by munch1182 on 2021/4/20 14:47.
 */
class NetClipActivity : BaseTopActivity() {

    private val model by get(NetClipViewModel::class.java)
    private val bind by bind<ActivityNetClipBinding>(R.layout.activity_net_clip)
    private val contentAdapter by lazy {
        object : BaseBindMultiAdapter<ClipData>() {
            init {
                register(ClipData.From.TYPE, R.layout.item_chat_from)
                    .register(ClipData.Send.TYPE, R.layout.item_chat_send)
                    .register(ClipData.System.TYPE, R.layout.item_chat_system)
                    .setType(object : MultiTypeWithPos {
                        override fun getItemTypeByPos(pos: Int): Int {
                            return getData()[pos].getType()
                        }
                    })
                val listener: (
                    adapter: BaseAdapter<ClipData, BaseBindViewHolder<ViewDataBinding>>,
                    bean: ClipData, view: View, pos: Int
                ) -> Unit =
                    { _, bean, _, _ ->
                        AppHelper.put2Clip(text = bean.content)
                        toast("内容已复制")
                    }
                setOnItemClickListener(listener)
                setOnItemLongClickListener(listener)
            }

            override fun onBindViewHolder(
                holder: BaseBindViewHolder<ViewDataBinding>,
                bean: ClipData,
                pos: Int
            ) {
                when (getItemViewType(pos)) {
                    ClipData.From.TYPE ->
                        holder.getVB<ItemChatFromBinding>().itemChatContent.text = bean.content
                    ClipData.Send.TYPE ->
                        holder.getVB<ItemChatSendBinding>().itemChatContent.text = bean.content
                    ClipData.System.TYPE ->
                        holder.getVB<ItemChatSystemBinding>().itemChatContent.text = bean.content
                    else -> {
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind.apply {
            lifecycleOwner = this@NetClipActivity
            vm = model

            netClipComment.setOnClickListener {
                showInput()
                AppHelper.showIm(netClipEt)
                hideMenuNoAnim()
                bind.netClipNsv.smoothScrollBy(0, netClipSend.height)
            }
            netClipEt.doAfterTextChanged {
                if (netClipEt.text.trim().isEmpty()) {
                    netClipSend.text = "cancel"
                } else {
                    netClipSend.text = "send"
                }
            }
            netClipExit.setOnClickListener {
                model.exit()
                hideInput()
                AppHelper.hideIm(this@NetClipActivity)
                netClipMenu.performClick()
            }
            netClipMenu.setOnClickListener {
                it as FloatingActionButton
                when (model.state().value) {
                    State.STATE_ALONE -> {
                        model.start()
                    }
                    State.STATE_CONNECTED -> {
                        val selected = it.isSelected
                        it.isSelected = !selected
                        switchMenu(!selected)
                    }
                    else -> {
                    }
                }
            }
            netClipRv.apply {
                layoutManager = LinearLayoutManager(this@NetClipActivity)

                adapter = contentAdapter
            }
            netClipSend.setOnClickListener {
                bind.netClipMenu.visibility = View.VISIBLE
                AppHelper.hideIm(this@NetClipActivity)
                val content = netClipEt.text.toString().trim()
                hideInput()
                if (content.isEmpty()) {
                    return@setOnClickListener
                }
                model.sendBySelf(content)
            }
            hideInput()
        }
        model.state().observeOnChanged(this) {
            when (it) {
                State.STATE_ALONE -> bind.netClipMenu.setImageResource(R.drawable.ic_baseline_start_24)
                State.STATE_SCANNING -> {
                    bind.netClipMenu.setImageResource(R.drawable.ic_baseline_loop_24)
                    bind.netClipMenu.apply {
                        //自旋转动画会引起surface更新，不适合进度之类的场景
                        //建议自定义view实现
                        startAnimation(RotateAnimation(
                            360f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f
                        ).apply {
                            repeatMode = Animation.RESTART
                            repeatCount = Animation.INFINITE
                            duration = 1500L
                        })
                    }
                }
                else -> {
                    bind.netClipMenu.setImageResource(R.drawable.ic_baseline_menu_24)
                    bind.netClipMenu.clearAnimation()
                }
            }
        }
        model.getData().observe(this) {
            contentAdapter.set(it)
        }
        ImHelper.watchChange(this) {
            bind.netClipNsv.smoothScrollBy(0, it)
        }
        NetStatusHelper.getInstance(this).apply {
            limitTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            setWhenResume(this@NetClipActivity, { available, _ ->
                model.updateIp()
                if (available) {
                    model.sendBySystem("wifi已连接")
                    model.sendBySystem("当前IP:${NetStatusHelper.getIpAddress()}")
                } else {
                    model.sendBySystem("wifi不可用")
                }
            }, { register() }, { unregister() })
        }
    }

    private fun hideMenuNoAnim() {
        bind.netClipMenu.isSelected = !bind.netClipMenu.isSelected
        val items =
            bind.netClipContainer.children.filter { it is FloatingActionButton && it != bind.netClipMenu }
        items.forEach { it.alpha = 0f }
        bind.netClipMenu.visibility = View.GONE
    }

    override fun setContentView(view: View) {
        super.setContentView(view, ViewHelper.newMarginParamsMM())
    }

    private fun hideInput() {
        bind.netClipEt.setText("")
        bind.netClipEt.nonInput()
        bind.netClipEt.visibility = View.GONE
        bind.netClipSend.visibility = View.GONE
    }

    private fun showInput() {
        bind.netClipSend.visibility = View.VISIBLE
        bind.netClipEt.apply {
            visibility = View.VISIBLE
            inputActive()
            requestFocus()
        }
    }

    private val radius by lazy { dp2Px(80f) }

    private fun switchMenu(extend: Boolean) {
        val items =
            bind.netClipContainer.children.filter { it is FloatingActionButton && it != bind.netClipMenu }
        val anim = ValueAnimator.ofInt(
            if (extend) 0 else radius.toInt(),
            if (extend) radius.toInt() else 0
        )
        anim.addUpdateListener {
            val progress = it.animatedValue
            items.forEach { v ->
                val lp = v.layoutParams as ConstraintLayout.LayoutParams
                lp.circleRadius = progress as Int
                v.layoutParams = lp

                v.alpha = progress / radius
            }
        }
        anim.start()
    }

    class NetClipViewModel : ViewModel() {

        private val state = MutableLiveData(State.STATE_ALONE)
        fun state() = state.toLiveData()
        private var ip: String? = null
        private val clipList = mutableListOf<ClipData>()
        private val clip = MutableLiveData(clipList)
        fun getData() = clip.toLiveData()

        private val helper = NetClipHelper().apply {
            listen { msg, i ->
                log(msg)
                showSendBy(msg, ip, ip == i)
            }.state { state, ip ->
                when {
                    isConnected(state) -> {
                        this@NetClipViewModel.state.postValue(State.STATE_CONNECTED)
                        showSendBy("$ip joined", null)
                    }
                    isDisconnected(state) -> {
                        if (ip == this@NetClipViewModel.ip) {
                            this@NetClipViewModel.state.postValue(State.STATE_ALONE)
                        } else {
                            this@NetClipViewModel.state.postValue(State.STATE_CONNECTED)
                        }
                        showSendBy("$ip leaved", null)
                    }
                    isExit(state) -> {
                        this@NetClipViewModel.state.postValue(State.STATE_ALONE)
                        showSendBy("exit", null)
                    }
                    isStart(state) -> {
                        showSendBy("start", null)
                    }
                }
            }
        }

        fun start() {
            state.postValue(State.STATE_SCANNING)
            sendBySystem("开始扫描")
            viewModelScope.launch { helper.start() }
        }

        fun exit() {
            helper.stop()
            state.postValue(State.STATE_ALONE)
        }

        fun sendBySelf(content: String) {
            if (content.isEmpty()) {
                return
            }
            send(content)
        }

        fun sendBySystem(s: String) {
            showSendBy(s, null)
        }

        private fun send(content: String) {
            helper.send(content)
        }

        private fun showSendBy(content: String, ip: String?, isSelf: Boolean = false) {
            clipList.add(
                when {
                    isSelf -> ClipData.Send(content, ip)
                    ip == null -> ClipData.System(content)
                    else -> ClipData.From(content, ip)
                }
            )
            clip.postValue(clipList)
        }

        fun updateIp() {
            ip = NetStatusHelper.getIpAddress()
        }

    }

    sealed class ClipData {
        abstract val content: String
        abstract fun getType(): Int

        internal data class Send(override val content: String, var ip: String?) : ClipData() {

            companion object {
                const val TYPE = 0
            }

            override fun getType() = TYPE
        }

        internal data class From(override val content: String, var ip: String) : ClipData() {
            companion object {
                const val TYPE = 1
            }

            override fun getType() = TYPE
        }

        internal data class System(override val content: String) : ClipData() {
            companion object {
                const val TYPE = 2
            }

            override fun getType() = TYPE
        }
    }

    @IntDef(
        State.STATE_ALONE,
        State.STATE_SCANNING,
        State.STATE_SCANNED,
        State.STATE_CONNECTING,
        State.STATE_CONNECTED
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class State {

        companion object {
            const val STATE_ALONE = 0
            const val STATE_SCANNING = 1
            const val STATE_SCANNED = 2
            const val STATE_CONNECTING = 3
            const val STATE_CONNECTED = 4
        }
    }
}