package ru.arisubest.smartshopper.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideBalanceState(@ApplicationContext context: Context): MutableStateFlow<Float> {
        val prefs = context.getSharedPreferences("balance_prefs", Context.MODE_PRIVATE)
        return MutableStateFlow(prefs.getFloat("balance", 0f))
    }
} 