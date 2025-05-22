package ru.arisubest.smartshopper.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@RequiresApi(Build.VERSION_CODES.O)
@Database(entities = [ShoppingItem::class], version = 1)
@TypeConverters(Converters::class)
abstract class ShoppingListDatabase : RoomDatabase() {
    abstract fun shoppingItemDao(): ShoppingItemDao
} 