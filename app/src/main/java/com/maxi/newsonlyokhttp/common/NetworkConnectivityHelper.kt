package com.maxi.newsonlyokhttp.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

interface NetworkConnectivityHelper {

    fun hasNetworkConnectivity(): Boolean
}

class DefaultNetworkConnectivityHelper(
    private val context: Context
): NetworkConnectivityHelper {

    override fun hasNetworkConnectivity(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}