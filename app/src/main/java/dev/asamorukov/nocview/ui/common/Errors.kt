package dev.asamorukov.nocview.ui.common

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

fun friendlyError(throwable: Throwable): String = when (throwable) {
    is UnknownHostException -> "Server not found. Check the URL."
    is ConnectException -> "Cannot reach the server."
    is SocketTimeoutException -> "Connection timed out."
    is SSLException -> "TLS error. If the server uses a self-signed certificate, enable that option in Settings."
    is HttpException -> when (throwable.code()) {
        401 -> "Authentication failed (401). Check username/password."
        403 -> "Access denied (403)."
        404 -> "Endpoint not found (404). Check the base URL (should point at the monitoring web root)."
        else -> "Server returned HTTP ${throwable.code()}."
    }
    is SerializationException -> "Unexpected response. Is the JSON CGI enabled on the server?"
    else -> throwable.message ?: throwable::class.simpleName ?: "Unknown error"
}
