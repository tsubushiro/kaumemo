package com.tsubushiro.kaumemo.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "shopping_lists")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = Instant.now().toEpochMilli(), // 作成日時 (Unixタイムスタンプ)
    val orderIndex: Int = 0 // ★追加★
)
