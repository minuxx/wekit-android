package com.coconutplace.wekit.di

import com.coconutplace.wekit.data.remote.auth.AuthService
import com.coconutplace.wekit.data.remote.badge.BadgeService
import com.coconutplace.wekit.data.remote.body.BodyService
import com.coconutplace.wekit.data.remote.diary.DiaryService
import com.coconutplace.wekit.data.remote.channel.ChannelService
import com.coconutplace.wekit.data.remote.chat.ChatService
import com.coconutplace.wekit.data.remote.gallery.GalleryService
import com.coconutplace.wekit.data.remote.home.HomeService
import com.coconutplace.wekit.data.remote.notice.NoticeService
import com.coconutplace.wekit.utils.SharedPreferencesManager
import com.coconutplace.wekit.utils.SharedPreferencesManager.Companion.X_ACCESS_TOKEN
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

const val PRODUCTION_URL = "https://api.wekitlife.com/"
const val TEST_URL = " http://13.124.127.29:9003/"
private val base_url: String = TEST_URL

fun getBaseUrl() = base_url

val networkModule: Module = module {
    fun provideHeaderInterceptor(sharedPreferenceManager: SharedPreferencesManager) =
        Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader(X_ACCESS_TOKEN, "${sharedPreferenceManager.getJwtToken()}")
                .build()

            chain.proceed(request)
        }

    fun provideHttpLoggingInterceptor() =
        HttpLoggingInterceptor().apply { HttpLoggingInterceptor.Level.BODY }

    fun provideOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        headerInterceptor: Interceptor
    ) = OkHttpClient.Builder()
        .readTimeout(5000, TimeUnit.MILLISECONDS)
        .connectTimeout(5000, TimeUnit.MILLISECONDS)
        .addInterceptor(headerInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .build()

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(getBaseUrl())
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()


    fun provideAuthService(retrofit: Retrofit): AuthService =
        retrofit.create(AuthService::class.java)

    fun provideHomeService(retrofit: Retrofit): HomeService =
        retrofit.create(HomeService::class.java)

    fun provideDiaryService(retrofit: Retrofit): DiaryService =
        retrofit.create(DiaryService::class.java)

    fun provideChannelService(retrofit: Retrofit): ChannelService =
        retrofit.create(ChannelService::class.java)

    fun provideChatService(retrofit: Retrofit): ChatService =
        retrofit.create(ChatService::class.java)

    fun provideBodyGraphService(retrofit: Retrofit): BodyService =
        retrofit.create(BodyService::class.java)

    fun provideNoticeService(retrofit: Retrofit): NoticeService =
        retrofit.create(NoticeService::class.java)

    fun provideGalleryService(retrofit: Retrofit): GalleryService =
        retrofit.create(GalleryService::class.java)

    fun provideBadgeService(retrofit: Retrofit): BadgeService =
        retrofit.create(BadgeService::class.java)

    single { provideHeaderInterceptor(get()) }
    single { provideHttpLoggingInterceptor() }
    single { provideOkHttpClient(get(), get()) }
    single { provideRetrofit(get()) }
    single { provideAuthService(get()) }
    single { provideHomeService(get()) }
    single { provideDiaryService(get()) }
    single { provideChannelService(get()) }
    single { provideChatService(get()) }
    single { provideBodyGraphService(get()) }
    single { provideNoticeService(get()) }
    single { provideGalleryService(get()) }
    single { provideBadgeService(get()) }
}