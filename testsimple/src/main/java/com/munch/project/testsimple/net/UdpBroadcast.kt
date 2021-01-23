package com.munch.project.testsimple.net

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket

/**
 * Create by munch182 on 2021/1/22 13:43.
 */

/**
 * 在win下运行，即可监听局域网下[SocketUdpHelper.sendNetBroadcast]
 */
fun main() {
    try {
        DatagramSocket(55555).use { socket ->
            val buffer = ByteArray(512)
            val packet = DatagramPacket(buffer, buffer.size)
            print("开始接收消息")
            socket.receive(packet)
            print(
                "收到" + packet.address
                    .hostAddress + ":" + packet.port + ":" + String(packet.data)
            )
            print("--退出--")
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
/*对应java代码*/
/*
public class UdpBroadcast {

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(55555)) {
            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.print("开始接收消息");
            socket.receive(packet);
            System.out.print("收到" + packet.getAddress().getHostAddress() + ":" + packet.getPort() + ":" + (new String(packet.getData())));
            System.out.print("--退出--");
        } catch (IOException e) {
            e.printStackTrace();
        }
     }
}*/
