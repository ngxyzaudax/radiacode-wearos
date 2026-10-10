package com.ngxyzaudax.radiacode.export

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import java.net.Inet4Address

private const val WIFI_TIMEOUT_MS: Int = 30_000

internal sealed interface WifiRequestResult {
    data class Address(val host: String) : WifiRequestResult
    data object Unavailable : WifiRequestResult
}

internal class WifiNetworkRequester(
    private val connectivity: ConnectivityManager,
) {
    private var callback: ConnectivityManager.NetworkCallback? = null

    fun request(onResult: (WifiRequestResult) -> Unit): Unit {
        release()
        val networkCallback = addressCallback(onResult)
        callback = networkCallback
        connectivity.requestNetwork(wifiRequest(), networkCallback, WIFI_TIMEOUT_MS)
    }

    fun release(): Unit {
        val current = callback ?: return
        callback = null
        runCatching { connectivity.unregisterNetworkCallback(current) }
    }

    private fun addressCallback(onResult: (WifiRequestResult) -> Unit): ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network): Unit {
                deliverHost(connectivity.getLinkProperties(network), onResult)
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties): Unit {
                deliverHost(linkProperties, onResult)
            }

            override fun onUnavailable(): Unit {
                onResult(WifiRequestResult.Unavailable)
            }
        }
}

private fun wifiRequest(): NetworkRequest =
    NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

private fun deliverHost(linkProperties: LinkProperties?, onResult: (WifiRequestResult) -> Unit): Unit {
    val host = linkProperties?.let(::ipv4Host) ?: return
    onResult(WifiRequestResult.Address(host))
}

internal fun ipv4Host(linkProperties: LinkProperties): String? =
    linkProperties.linkAddresses
        .asSequence()
        .map { it.address }
        .filterIsInstance<Inet4Address>()
        .firstOrNull { address -> !address.isLoopbackAddress && !address.isLinkLocalAddress }
        ?.hostAddress
