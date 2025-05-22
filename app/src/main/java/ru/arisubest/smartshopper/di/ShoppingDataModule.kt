package ru.arisubest.smartshopper.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.arisubest.smartshopper.data.local.ShoppingItemDao
import ru.arisubest.smartshopper.data.local.ShoppingListDatabase
import javax.inject.Singleton
import androidx.room.Room

@InstallIn(SingletonComponent::class)
@Module
class ShoppingDataModule {
    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideShoppingListDatabase(@ApplicationContext appContext: Context): ShoppingListDatabase {
        return Room.databaseBuilder(
            appContext,
            ShoppingListDatabase::class.java,
            "shoppinglist_database"
        ).build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideShoppingItemDao(appDatabase: ShoppingListDatabase): ShoppingItemDao {
        return appDatabase.shoppingItemDao()
    }
}