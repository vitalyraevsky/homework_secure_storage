package com.otus.securehomework.di

import com.otus.securehomework.BuildConfig
import com.otus.securehomework.data.repository.TokenAuthenticator
import com.otus.securehomework.data.repository.crypto.SecureUserPreferences
import com.otus.securehomework.data.source.local.UserPreferences
import com.otus.securehomework.data.source.network.TokenRefreshApi
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.net.ssl.*

private const val BASE_URL = "https://auth.tragltech.com/otus/api/"

class RemoteDataSource @Inject constructor(
    private val preferences: SecureUserPreferences
) {

    fun <Api> buildApi(
        api: Class<Api>,
    ): Api {
        val authenticator = TokenAuthenticator(buildTokenApi(), preferences)
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getRetrofitClient(authenticator))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }

    private fun buildTokenApi(): TokenRefreshApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getRetrofitClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TokenRefreshApi::class.java)
    }

    private fun getRetrofitClient(authenticator: Authenticator? = null): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().also {
                    it.addHeader("Accept", "application/json")
                }.build())
            }.also { client ->
                authenticator?.let { client.authenticator(it) }
                if (BuildConfig.DEBUG) {
                    val logging = HttpLoggingInterceptor()
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                    client.addInterceptor(logging)


//                    val trustManager = object : X509TrustManager {
//                        @Throws(CertificateException::class)
//                        override fun checkClientTrusted(
//                            chain: Array<X509Certificate?>?,
//                            authType: String?
//                        ) = Unit
//
//                        @Throws(CertificateException::class)
//                        override fun checkServerTrusted(
//                            chain: Array<X509Certificate?>?,
//                            authType: String?
//                        ) = Unit
//
//                        override fun getAcceptedIssuers(): Array<X509Certificate> {
//                            return arrayOf()
//                        }
//
//                    }
//                    val trustAllCerts: Array<TrustManager> = arrayOf(trustManager)
//
//                    val sslContext = SSLContext.getInstance("SSL")
//                    sslContext.init(null, trustAllCerts, SecureRandom())
//                    val sslSocketFactory = sslContext.socketFactory
//
//                    client.sslSocketFactory(sslSocketFactory, trustManager)
//
//                    client.hostnameVerifier { hostname, session -> true }
                }
            }.build()
    }
}