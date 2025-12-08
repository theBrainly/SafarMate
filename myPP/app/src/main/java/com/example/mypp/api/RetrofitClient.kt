package com.example.mypp.api

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Enhanced RetrofitClient with caching, logging, and timeout configurations
 */
object RetrofitClient {
    private const val BASE_URL = "http://52.66.245.173:2000/api/"
    private const val CACHE_SIZE = 10 * 1024 * 1024L // 10 MB cache
    private const val CONNECTION_TIMEOUT = 30L // 30 seconds
    
    private var httpClient: OkHttpClient? = null
    private var retrofit: Retrofit? = null
    private var gson: Gson = GsonBuilder().create()
    
    /**
     * Initialize RetrofitClient with application context for caching
     */
    fun init(context: Context) {
        if (httpClient == null) {
            // Cache configuration
            val cache = Cache(context.cacheDir, CACHE_SIZE)
            
            // Logging interceptor for debugging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            // Cache control interceptor
            val cacheInterceptor = CacheInterceptor(context)
            
            // Create OkHttpClient with all configurations
            httpClient = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(cacheInterceptor)
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build()
            
            // Create Retrofit instance
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient!!)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
    }
    
    // Access to ApiService with lazy initialization
    val instance: ApiService by lazy {
        if (retrofit == null) {
            throw IllegalStateException("RetrofitClient must be initialized before use. Call RetrofitClient.init(context) in your Application class.")
        }
        retrofit!!.create(ApiService::class.java)
    }
}