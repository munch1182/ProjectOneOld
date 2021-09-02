package com.munch.lib.helper.net

import android.net.NetworkCapabilities

/**
 * Create by munch1182 on 2021/9/1 17:25.
 */
/**
 * 网络功能可用
 */
fun NetworkCapabilities.enableNet() = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

/**
 * 网络连接已验证
 */
fun NetworkCapabilities.hadValidated() = hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

/**
 * 是否有wifi通道
 */
fun NetworkCapabilities.hasWifi() = hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

/**
 * 是否有流量通道
 */
fun NetworkCapabilities.hasCellular() = hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

fun NetworkCapabilities.wifiAvailable() = hasWifi() && enableNet() && hadValidated()
fun NetworkCapabilities.cellularAvailable() = hasCellular() && enableNet() && hadValidated()