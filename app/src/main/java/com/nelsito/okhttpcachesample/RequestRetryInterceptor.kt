package com.nelsito.okhttpcachesample

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RequestRetryInterceptor(private val waits: LongArray) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return execute(chain)
    }

    @Throws(IOException::class)
    private fun execute(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var status = execute(chain, request)
        var shouldRetry = !status.isOk

        if (shouldRetry) {
            for (wait in waits) {
                try {
                    Thread.sleep(wait)
                } catch (e: InterruptedException) { // Intentionally left blank
                }
                status = execute(chain, request)
                if (status.isOk) {
                    return status.getResponse()!!
                }
            }
        }
        return status.getResponse()!!
    }

    private fun execute(chain: Interceptor.Chain, request: Request): ExecutionStatus {
        return try {
            val response = chain.proceed(request)
            ExecutionStatus(response)
        } catch (exception: IOException) {
            ExecutionStatus(exception)
        }
    }

    private inner class ExecutionStatus {
        private var response: Response? = null
        private var exception: IOException? = null

        internal constructor(response: Response?) {
            this.response = response
        }

        internal constructor(exception: IOException?) {
            this.exception = exception
        }

        val isOk: Boolean
            get() = response != null && response!!.code < INTERNAL_SERVER_ERROR

        @Throws(IOException::class)
        fun getResponse(): Response? {
            if (exception != null) {
                throw exception as IOException
            }
            return response
        }
    }

    companion object {
        private const val INTERNAL_SERVER_ERROR = 500
    }
}