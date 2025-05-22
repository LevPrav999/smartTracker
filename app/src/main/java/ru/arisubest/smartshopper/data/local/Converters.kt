package ru.arisubest.smartshopper.data.local

import androidx.room.TypeConverter
import ru.arisubest.smartshopper.data.local.ItemCategory

class Converters {
    @TypeConverter
    fun fromItemCategory(category: ItemCategory): String {
        return category.name
    }
    @TypeConverter
    fun toItemCategory(value: String): ItemCategory {
        return ItemCategory.valueOf(value)
    }
} 