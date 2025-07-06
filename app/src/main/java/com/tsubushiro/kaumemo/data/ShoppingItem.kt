package com.tsubushiro.kaumemo.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "shopping_items",
    foreignKeys = [ForeignKey(
        entity = ShoppingList::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE // 親リスト削除時にアイテムも削除
    )],
    indices = [Index(value = ["listId"])] // listIdでの検索を高速化
)
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listId: Int, // 所属するShoppingListのID (外部キー)
    val name: String,
    val isPurchased: Boolean = false, // 購入済みかどうか
    val createdAt: Long = Instant.now().toEpochMilli(), // 作成日時
    val orderIndex: Int = 0 // ★追加★
)