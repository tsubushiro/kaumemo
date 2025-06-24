package com.tsubushiro.kaumemo.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "shopping_lists",
    indices = [Index(value = ["orderIndex"])] // ★この行を追加★
)
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val orderIndex: Int = 0
)
