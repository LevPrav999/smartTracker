package ru.arisubest.smartshopper.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.arisubest.smartshopper.R
import java.io.Serializable
import java.time.DayOfWeek

@Entity(tableName = "shoppingtable")
@RequiresApi(Build.VERSION_CODES.O)
data class ShoppingItem @RequiresApi(Build.VERSION_CODES.O) constructor(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "category") var category: ItemCategory,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "estimatedprice") var estimatedPrice: Float,
    @ColumnInfo(name = "status") var status: Boolean,
    @ColumnInfo(name = "dayofweek") var dayOfWeek: DayOfWeek = DayOfWeek.MONDAY
): Serializable

enum class ItemCategory {
    FOOD, HEALTH, CLOTHES, ELECTRONICS, CLEANING, RECREATION, MISC;

    fun getIcon(): Int {
        return when (this) {
            FOOD -> R.drawable.food
            HEALTH -> R.drawable.health
            CLOTHES -> R.drawable.clothes
            ELECTRONICS -> R.drawable.electronics
            CLEANING -> R.drawable.cleaning
            RECREATION -> R.drawable.recreation
            MISC -> R.drawable.misc
        }
    }
}