package com.nelsito.okhttpcachesample

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class NetworkAPI {

    fun createClient(context: Context): NetworkClient {

        val cacheSize = 10 * 1024 * 1024L

        val client: OkHttpClient.Builder = OkHttpClient.Builder()
            .cache(Cache(context.cacheDir, cacheSize))
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)


        client.addInterceptor(logging)

        val retriesWaits = longArrayOf(500, 1000, 3000)


        client.addInterceptor(RequestRetryInterceptor(retriesWaits))

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()

        return retrofit.create(NetworkClient::class.java)
    }

    companion object {
        private const val BASE_URL: String = "https://api.github.com/"
    }

}

interface NetworkClient {

    @GET("repositories")
    suspend fun getRepositories(): List<GithubResponse>
}