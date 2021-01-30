package com.munch.project.testsimple.net

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.munch.lib.BaseApp
import com.munch.lib.helper.ProcedureLog
import com.munch.lib.helper.ThreadHelper
import com.munch.lib.log
import okhttp3.internal.closeQuietly
import java.io.*
import java.net.*
import java.nio.ByteBuffer
import kotlin.random.Random

/**
 *
 * 两个同一协议的未知设备的局域网连接
 *
 * 设备A发送udp局域网广播，带有标识
 * 设备B接收到局域网广播，建立tcp并回复tcp端口
 * 设备A连接到设备B
 * Create by munch1182 on 2021/1/23 17:18.
 */
class TestClipViewModel : ViewModel() {

    //<editor-fold desc="LiveData">
    private val clipDataReceiver = MutableLiveData<MutableList<SocketContentBean>>(mutableListOf())
    private val clipData = MutableLiveData("")
    private val status = MutableLiveData(Status())
    fun getClipListData(): LiveData<MutableList<SocketContentBean>> = clipDataReceiver
    fun getClipData(): LiveData<String> = clipData
    fun getStatus(): LiveData<Status> = status
    //</editor-fold>

    private val closeable = arrayListOf<Closeable?>()
    private val log = ProcedureLog("TestSocketBroadcast".plus(Random.nextInt(10)), this::class.java)
    private val dataHelper = DataHelper()
    private val streamHelper by lazy { StreamHelper() }
    private fun Status.post() {
        status.postValue(this)
    }


    /**
     * 发送udp的端口，用来过滤
     */
    private var sendPort = -1
    private var tcpService: InetSocketAddress? = null
    private var manager: ClipboardManager? = null
    private val clipListener = {
        //需要应用获取焦点之后延迟一秒去获取剪切板内容
        if (manager?.hasPrimaryClip() == true) {
            val text = manager?.primaryClip?.getItemAt(0)?.text
            clipData.postValue(text.toString())
        }
    }

    init {
        manager = BaseApp.getContext()
            .getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
        ThreadHelper.getExecutor().execute {
            Thread.sleep(1000L)
            manager?.apply {
                addPrimaryClipChangedListener(clipListener)
            }
        }
    }

    //<editor-fold desc="view方法">
    fun copy2Clip(content: String) {
        manager?.setPrimaryClip(ClipData.newPlainText("", content))
    }

    fun startSearch() {
        if (!isConnect()) {
            close()
            log.start()
            listenUdpBroadcast()
            sendUdpBroadcast()
        }
    }

    /**
     * 断开连接，仅连接后时可用
     */
    fun disconnect() {
        if (!status.value!!.isConnected()) {
            return
        }
        streamHelper.write(dataHelper.exit())
    }

    fun close() {
        status.value!!.closed().post()
        //关闭所有能关闭的流
        closeable.forEach {
            it?.closeQuietly()
        }
        closeable.clear()
        tcpService = null
        log.end()

        manager?.removePrimaryClipChangedListener(clipListener)
    }

    fun sendText(s: String?) {
        s ?: return
        log.step("tcp发送：$s")
        ThreadHelper.getExecutor().execute {
            streamHelper.write(dataHelper.writeStr(s.trim()))
        }
    }
    //</editor-fold>

    private fun listenUdpBroadcast() {
        if (isConnect()) {
            return
        }
        ThreadHelper.getExecutor().execute {
            var socket: DatagramSocket? = DatagramSocket(Protocol.BROADCAST_PORT)
            val packet = DatagramPacket(ByteArray(125), 125)
            closeable.add(socket)
            log.step("开始监听udp")
            while (!isConnect()) {
                try {
                    socket?.receive(packet)
                } catch (e: IOException) {
                    return@execute
                }
                val port = packet.port
                //过滤掉自身发送的udp
                if (sendPort != -1 && port == sendPort) {
                    continue
                }
                if (!parseUdpReceiver(socket, packet, port)) {
                    //关闭udp广播
                    closeAndRemove(socket)
                    socket = null
                    log.step("关闭自身的udp广播和监听")
                }
                Thread.sleep(1000L)
            }
            log.step("udp监听关闭")
        }
    }

    private fun isConnect(): Boolean {
        synchronized(status) {
            return status.value!!.isStartConnect()
        }
    }

    private fun isCreateService(): Boolean {
        synchronized(status) {
            return status.value!!.isCreateService()
        }
    }

    private fun parseUdpReceiver(
        socket: DatagramSocket?,
        packet: DatagramPacket,
        port: Int
    ): Boolean {
        val bean = dataHelper.parseData(packet.data)
        log.step("接收到${packet.address}:${port}:${bean}")

        //无效token，则继续监听
        if (!bean.tokenValid()) {
            return true
        }

        //特殊设备，即使是收到消息的应用也不建立tcp//保留
        /*if (!bean.isService() && createTcp()) {
            return true
        }*/
        //获取已建立的tcp端口
        if (bean.isService()) {
            //如果收到由对方建立服务端但是服务端还未建立，则等待下一条消息
            if (bean.needWait()) {
                return true
            }

            log.step("接收到${packet.address}:${port}:${bean}")

            tcpService = InetSocketAddress(packet.address, bean.getTcpPort() ?: return false)
            //关闭udp，准备连接tcp
            startTcpService(true)
            return false
            //没有tcp端口则由我方建立tcp端口，如果不想由此应用建立tcp，则发送Protocol.SERVICE_N即可
        } else {
            var replayBean = dataHelper.replay()
            log.step("回复: $replayBean")
            packet.port = Protocol.BROADCAST_PORT
            packet.data = replayBean.toByteArray()
            //先发一条消息告知对方由我方建立服务端
            socket?.send(packet)

            //更新状态：创建服务
            status.value!!.serviceCreate().post()

            //启动自身的tcp服务
            startTcpService(false)
            //等待tcp建立
            while (isCreateService()) {
                log.step("等待服务创建")
                Thread.sleep(500L)
            }
            if (tcpService == null) {
                log.step("服务创建失败")
                return true
            }
            //再发一条消息告知对方服务端已建立
            replayBean = dataHelper.replay(tcpService!!.port)
            log.step("回复: $replayBean")
            packet.port = Protocol.BROADCAST_PORT
            packet.data = replayBean.toByteArray()
            //再发一次udp来回复
            socket?.send(packet)
            return false
        }
    }

    private fun startTcpService(isClient: Boolean) {
        if (status.value!!.isStartConnect()) {
            return
        }
        ThreadHelper.getExecutor().execute {
            log.step("tcp开始")

            if (isClient) {
                if (tcpService == null) {
                    log.step("tcp错误")
                    return@execute
                }
                val socket = Socket()
                //状态：连接中
                status.value!!.connecting().post()
                socket.connect(tcpService)
                streamHelper.init(socket.getOutputStream(), socket.getInputStream())
                //状态：已连接
                status.value!!.connected().post()
            } else {
                //状态：创建服务
                status.value!!.serviceCreate().post()
                //1对1连接
                val service = ServerSocket(0, 1)
                log.step("创建服务: ${service.inetAddress}:${service.localPort}")
                tcpService = InetSocketAddress(service.inetAddress, service.localPort)
                //状态：连接中
                status.value!!.connecting().post()
                val accept = service.accept()
                //状态：已连接
                status.value!!.connected().post()
                streamHelper.init(accept.getOutputStream(), accept.getInputStream())
            }
            streamHelper.manager(closeable)

            val byteArray = ByteArray(1024)
            while (isConnect()) {

                if (streamHelper.read(byteArray) != -1) {
                    val readStr = dataHelper.readStr(byteArray)
                    if (dataHelper.isExit(readStr)) {
                        status.value!!.closed()
                        close()
                        log.step("对方已关闭，连接已断开，重新开始搜索")
                        startSearch()
                        return@execute
                    }
                    log.step("读到数据: $readStr")
                }
            }
            log.step("tcp已关闭")
        }
    }

    private fun closeAndRemove(close: Closeable?) {
        close ?: return
        close.closeQuietly()
        closeable.remove(close)
    }

    private fun sendUdpBroadcast() {
        if (isConnect()) {
            return
        }
        ThreadHelper.getExecutor().execute {
            val socket = DatagramSocket()
            val byteArray = dataHelper.newScanData()
            val packet = DatagramPacket(
                byteArray,
                byteArray.size,
                InetAddress.getByName("255.255.255.255"),
                Protocol.BROADCAST_PORT
            )
            closeable.add(socket)
            sendPort = socket.localPort
            status.value!!.scanning().post()
            while (!isConnect()) {
                try {
                    socket.send(packet)
                } catch (e: IOException) {
                    //关闭后会报错，则直接退出
                    return@execute
                }
                log.step("发送udp255")
                Thread.sleep(5000L)
            }

            log.step("停止发送udp255")
            closeAndRemove(socket)
        }
    }

    private fun update(line: String) {
        clipDataReceiver.value?.add(SocketContentBean(line, "", -1)) ?: return
        clipDataReceiver.postValue(clipDataReceiver.value)
    }

    //<editor-fold desc="tcp">
    class StreamHelper(
        private var os: OutputStream? = null,
        private var iss: InputStream? = null
    ) : Closeable {

        fun init(os: OutputStream?, iss: InputStream?): StreamHelper {
            this.os = os
            this.iss = iss
            return this
        }

        fun manager(closeable: ArrayList<Closeable?>) {
            if (os != null) {
                closeable.add(os!!)
            }
            if (iss != null) {
                closeable.add(iss!!)
            }
        }

        @WorkerThread
        fun read(byteArray: ByteArray): Int {
            return iss?.read(byteArray) ?: -1
        }

        @WorkerThread
        fun write(b: ByteArray) {
            os ?: return
            os?.write(b)
        }

        override fun close() {
            os?.close()
            iss?.close()
            os = null
            iss = null
        }

    }
    //</editor-fold>

    //<editor-fold desc="Protocol">
    object Protocol {

        const val EXIT = "exit"

        const val TOKEN = 20211

        const val SERVICE_Y = 1
        const val SERVICE_N = 2

        const val STATUS_WAIT = 1
        const val STATUS_READ = 2

        const val BROADCAST_PORT = 20211

        const val START = '['
        const val END = ']'
        const val SPLIT = ':'
    }

    class ProtocolBean(
        private val token: Int,
        private val isService: Int,
        private val status: Int = Protocol.STATUS_WAIT,
        private val tcpPort: Int = -1
    ) {

        fun tokenValid() = token == Protocol.TOKEN

        fun isService() = isService == Protocol.SERVICE_Y

        fun needWait() = status == Protocol.STATUS_WAIT

        fun getTcpPort(): Int? = tcpPort.takeIf { it != -1 }

        fun toByteArray(): ByteArray {
            val buffer = ByteBuffer.allocate(MIN_LENGTH)
            //第一位int：token
            buffer.putInt(token)
            //第二位int：是否由发送方建立服务
            buffer.putInt(isService)
            //第三位int: 如果由我方建立服务，当前服务是否已经可用
            buffer.putInt(status)
            //第三位int：tcp的端口，无则为-1
            buffer.putInt(tcpPort)
            return buffer.array()
        }

        override fun toString(): String {
            return "ProtocolBean(token=${token},isService=${isService},status=${status},tcpPort=${getTcpPort()})"
        }

        companion object {

            /**
             * 4个int即12位
             */
            const val MIN_LENGTH = 16

            fun new(isService: Boolean, port: Int = -1): ProtocolBean {
                return ProtocolBean(
                    Protocol.TOKEN,
                    if (isService) Protocol.SERVICE_Y else Protocol.SERVICE_N,
                    if (port == -1) Protocol.STATUS_WAIT else Protocol.STATUS_READ,
                    port
                )
            }

            fun invalid(): ProtocolBean {
                return ProtocolBean(-1, -1)
            }

            fun parseData(data: ByteArray): ProtocolBean {
                //如果长度与协议不符则是无效数据
                if (data.size < MIN_LENGTH) {
                    return invalid()
                }
                val wrap = ByteBuffer.wrap(data)
                val token = wrap.int
                val isService = wrap.int
                val status = wrap.int
                val port = wrap.int
                return ProtocolBean(token, isService, status, port)
            }
        }
    }

    class DataHelper {

        private val buffer by lazy { ByteBuffer.allocate(1024) }

        fun newScanData(): ByteArray = ProtocolBean.new(false).toByteArray()

        fun parseData(data: ByteArray) = ProtocolBean.parseData(data)

        fun replay(port: Int = -1): ProtocolBean {
            return ProtocolBean.new(true, port)
        }

        /**
         * 123 -> [3:123]
         */
        fun writeStr(str: String): ByteArray {
            val size = str.toByteArray().size
            return "${Protocol.START}$size${Protocol.SPLIT}$str${Protocol.END}".toByteArray()
        }

        fun exit(): ByteArray {
            return writeStr(Protocol.EXIT)
        }

        fun isExit(str: String?): Boolean {
            return str == Protocol.EXIT
        }

        fun readStr(array: ByteArray): String? {
            if (array.size < 10 || array.size > 1024) {
                return null
            }
            buffer.clear()
            buffer.put(array)
            //写入后复位读取
            buffer.flip()

            log(buffer.toString())

            val start = buffer.char
            val length = buffer.int
            val split = buffer.char

            //标记的是第一个split之后的位置
            buffer.mark()
            //一个char一个int共6位
            buffer.position(array.size - 2)
            val end = buffer.char
            log(start, length, split, end)
            buffer.reset()
            //检验标识符
            if (start != Protocol.START || split != Protocol.SPLIT || end != Protocol.END
                //检验长度
                //传过来的数据长度+8位标识符应该小于等于有效数据的长度，否则数据不完整
                || length + 8 > buffer.limit()
            ) {
                return null
            }

            val byteArray = ByteArray(length)
            buffer.get(byteArray)
            return byteArray.toString()
        }

    }

    //</editor-fold>
    class Status {

        private var status: Int = STATUS_CLOSE

        fun closed(): Status {
            status = STATUS_CLOSE
            return this
        }

        fun scanning(): Status {
            status = STATUS_SCANNING
            return this
        }

        fun connecting(): Status {
            status = STATUS_CONNECTING
            return this
        }

        fun connected(): Status {
            status = STATUS_CONNECTED
            return this
        }

        fun serviceCreate(): Status {
            status = STATUS_CREATE_SERVICE
            return this
        }

        fun isCreateService() = status == STATUS_CREATE_SERVICE
        fun isClosed() = status == STATUS_CLOSE
        fun isScanning() = status == STATUS_SCANNING
        fun isConnecting() = status == STATUS_CONNECTING
        fun isConnected() = status == STATUS_CONNECTED
        fun isStartConnect() = isConnecting() || isConnected()

        companion object {
            /**
             * 状态：
             * 1. 初始    -》STATUS_CLOSED
             * 2. 开始udp -》STATUS_SCANNING
             * 3. 服务端先创建 -》 STATUS_CREATE_SERVICE
             * 4. 客户端连接 -》 STATUS_CONNECTING
             * 5. 连接成功 -》 STATUS_CONNECTED
             */
            const val STATUS_CLOSE = 0
            const val STATUS_SCANNING = 1
            const val STATUS_CREATE_SERVICE = 2
            const val STATUS_CONNECTING = 3
            const val STATUS_CONNECTED = 4
        }
    }

}