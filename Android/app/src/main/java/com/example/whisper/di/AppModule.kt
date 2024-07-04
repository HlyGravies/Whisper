package com.example.whisper.di

import android.app.Application
import com.example.whisper.MyApplication.MyApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().build()
    }
    @Provides
    @Singleton
    fun provideJsonMediaType(): MediaType {
        return "application/json; charset=utf-8".toMediaType()
    }

    @Provides
    @Singleton
    fun provideMyApplication(application: Application): MyApplication {
        return application as MyApplication
    }
}
