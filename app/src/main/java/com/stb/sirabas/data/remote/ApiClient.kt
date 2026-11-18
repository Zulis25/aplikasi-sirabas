package com.stb.sirabas.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://aryaganteng.sovereigns.my.id/"

    @Volatile
    private var apiInstance: SirabasApi? = null

    fun getApi(tokenManager: TokenManager): SirabasApi {
        return apiInstance ?: synchronized(this) {
            apiInstance ?: createApi(tokenManager).also { apiInstance = it }
        }
    }

    private fun createApi(tokenManager: TokenManager): SirabasApi {
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val token = tokenManager.getToken()

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            redactHeader("Authorization")
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SirabasApi::class.java)
    }
}
