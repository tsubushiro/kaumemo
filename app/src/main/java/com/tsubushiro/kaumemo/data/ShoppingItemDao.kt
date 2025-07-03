package com.tsubushiro.kaumemo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shoppingItem: ShoppingItem) // アイテムの追加 (既存があれば置換)

    @Update
    suspend fun update(shoppingItem: ShoppingItem) // アイテムの更新

    @Delete
    suspend fun delete(shoppingItem: ShoppingItem) // アイテムの削除

    // 特定のリストに紐づくアイテムを未購入が上、購入済みが下、それぞれ作成日時降順で取得
//    @Query("""
//        SELECT * FROM shopping_items
//        WHERE listId = :listId
//        ORDER BY isPurchased ASC, createdAt DESC
//    """)
    @Query("""
        SELECT * FROM shopping_items 
        WHERE listId = :listId 
        ORDER BY createdAt ASC
    """)
    fun getShoppingItemsForList(listId: Int): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE id = :itemId")
    suspend fun getShoppingItemById(itemId: Int): ShoppingItem? // 特定IDのアイテムを取得
}
