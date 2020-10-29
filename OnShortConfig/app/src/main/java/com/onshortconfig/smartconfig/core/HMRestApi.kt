package com.onshortconfig.smartconfig.core

import androidx.databinding.library.BuildConfig.DEBUG
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object HMRestApi {

    val config = HMRestConfig()

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(config.baseURL)
            .client(getHTTPClient())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(HMJsonParser.getDefaultGSONParser()))
            .build()
    }

    fun <T> get(apiService: Class<T>): T {
        return getRetrofit().create(apiService)
    }

    private fun getHTTPClient(isEmbedAppInterceptor: Boolean = true): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()

        if (DEBUG) {
            val okHttpDebugging = HttpLoggingInterceptor()
            okHttpDebugging.level = HttpLoggingInterceptor.Level.BODY

            okHttpClient.addInterceptor(okHttpDebugging)
            okHttpClient.addNetworkInterceptor(StethoInterceptor())
        }

        config.certificateResId?.let {
//            val sslSocketFactory = HMSocketFactory.getSocketFactory(
//                config.context!!,
//                it
//            )
//            okHttpClient.sslSocketFactory(sslSocketFactory)
//            okHttpClient.hostnameVerifier { _, _ -> true }
        }

        if (isEmbedAppInterceptor) {
            okHttpClient.addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()

                // set users access token if available
                config.authorizationToken?.let { unwrappedToken ->
                    if (unwrappedToken.isNotBlank()) {
                        requestBuilder.addHeader(
                            "Authorization",
                            "${config.authorizationTokenPrefix} $unwrappedToken"
                        )
                    }
                }

                // add custom users headers (of course that will override headers above)
                config.additionalHeaders.forEach { entry ->
                    requestBuilder.addHeader(entry.key, entry.value)
                }

                return@addInterceptor chain.proceed(requestBuilder.build())
            }
        }

        okHttpClient.connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)

        return okHttpClient.build()
    }
}