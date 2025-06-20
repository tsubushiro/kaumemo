package com.tsubushiro.kaumemo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shoppingList: ShoppingList) // リストの追加 (既存があれば置換)

    @Update
    suspend fun update(shoppingList: ShoppingList) // リストの更新

    @Delete
    suspend fun delete(shoppingList: ShoppingList) // リストの削除

    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun getAllShoppingLists(): Flow<List<ShoppingList>> // 全てのリストを日付降順で取得、Flowで変更を監視

//    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
//    suspend fun getShoppingListById(listId: Int): ShoppingList? // 特定IDのリストを取得

    // ★ 新規追加: IDで特定の買い物リストを取得するメソッド ★
    @Query("SELECT * FROM shopping_lists WHERE id = :listId")
    fun getShoppingListById(listId: Int): Flow<ShoppingList?> // Flow<ShoppingList?> を返す

}