package com.example.ominous.di

import android.content.Context
import com.example.ominous.utils.ExportHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    @Provides
    @Singleton
    fun provideExportHelper(@ApplicationContext context: Context): ExportHelper {
        return ExportHelper(context)
    }
}