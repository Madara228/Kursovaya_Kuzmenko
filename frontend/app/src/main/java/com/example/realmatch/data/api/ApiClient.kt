package com.example.realmatch.data.api

import android.content.Context
import com.example.realmatch.BuildConfig
import com.example.realmatch.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    @Volatile
    private var retrofit: Retrofit? = null

    fun retrofit(context: Context): Retrofit =
        retrofit ?: synchronized(this) {
            retrofit ?: build(context.applicationContext).also { retrofit = it }
        }

    fun <T> create(context: Context, service: Class<T>): T = retrofit(context).create(service)

    private fun build(context: Context): Retrofit {
        val tokens = TokenManager.getInstance(context)
        val authInterceptor = okhttp3.Interceptor { chain ->
            val token = tokens.token
            val request = if (token == null) {
                chain.request()
            } else {
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            }
            val response = chain.proceed(request)
            if (response.code == 401 && !request.url.encodedPath.contains("/auth/")) {
                SessionEvents.notifyUnauthorized()
            }
            response
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    // BASIC, чтобы в Logcat не попадали пароли и токены
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
