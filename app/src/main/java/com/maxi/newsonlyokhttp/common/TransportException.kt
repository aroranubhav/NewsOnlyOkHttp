package com.maxi.newsonlyokhttp.common

import java.io.IOException

sealed class TransportException(
    cause: Throwable? = null
) : IOException(cause) {

    class NoConnectivity(cause: Throwable? = null) : TransportException(cause)
    class Timeout(cause: Throwable? = null) : TransportException(cause)
    class Unknown(cause: Throwable? = null) : TransportException(cause)
}