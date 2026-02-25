package com.maxi.newsonlyokhttp.data.common

import com.maxi.newsonlyokhttp.common.ErrorType
import com.maxi.newsonlyokhttp.common.HttpException
import com.maxi.newsonlyokhttp.common.Resource
import com.maxi.newsonlyokhttp.common.TransportException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
    return try {
        val response = apiCall()
        val responseCode = response.code()

        if (responseCode == 304) {
            return Resource.NoChange
        }

        response.body()?.let {
            Resource.Success(it)
        } ?: Resource.Error(
            ErrorType.UNKNOWN,
            "Empty response body!",
            responseCode
        )
    } catch (ce: CancellationException) {
        throw ce
    } catch (e: TransportException.NoConnectivity) {
        Resource.Error(ErrorType.NO_CONNECTIVITY, e.message, null)
    } catch (e: TransportException.Timeout) {
        Resource.Error(ErrorType.TIMEOUT, e.message, null)
    } catch (e: HttpException.Unauthorized) {
        Resource.Error(ErrorType.UNAUTHORISED, e.errorMessage, e.errorCode)
    } catch (e: HttpException.Forbidden) {
        Resource.Error(ErrorType.FORBIDDEN, e.errorMessage, e.errorCode)
    } catch (e: HttpException.NotFound) {
        Resource.Error(ErrorType.NOT_FOUND, e.errorMessage, e.errorCode)
    } catch (e: HttpException.ServerError) {
        Resource.Error(ErrorType.SERVER_ERROR, e.errorMessage, e.errorCode)
    } catch (e: HttpException) {
        Resource.Error(ErrorType.UNKNOWN, e.errorMessage, e.errorCode)
    } catch (e: IOException) {
        Resource.Error(ErrorType.UNKNOWN, e.message, null)
    }
}

/**
 * The reason CancellationException specifically must never be swallowed is
 * that it's how Kotlin's coroutine machinery signals that a coroutine should stop.
 * When you cancel a coroutine scope — for example when a user navigates
 * away from a screen — Kotlin throws CancellationException inside any suspended function.
 * If you swallow it, the coroutine doesn't know it's been cancelled and keeps running,
 * holding onto resources that should have been released.
 * In the above code catch-all catch (e: Exception) at the bottom is actually the danger here —
 * CancellationException is a subclass of Exception, so without the explicit rethrow above it,
 * it would be silently converted into a Resource.Error and the coroutine would continue running
 * after cancellation.
 * This is why the CancellationException catch must always come first and always rethrow.
 */
