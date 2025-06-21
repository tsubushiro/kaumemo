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
    suspend fun insert(shoppingList: ShoppingList):Long // リストの追加 (既存があれば置換)

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

    // 順に全てのリストを取得。
    @Query("SELECT * FROM shopping_lists ORDER BY orderIndex ASC, createdAt DESC") // orderIndexを優先、次に作成日時でソート
    fun getAllShoppingListsOrderByOrderIndex(): Flow<List<ShoppingList>>

    // 新規リスト作成時に orderIndex を設定するため、既存リストの最大 orderIndex を取得
    @Query("SELECT MAX(orderIndex) FROM shopping_lists")
    suspend fun getLastListOrderIndex(): Int? // suspendにする (Flowではなく一度きりの取得のため)

    // 連番リスト名生成のために、指定された名前で始まるリストの数をカウント。
    @Query("SELECT COUNT(*) FROM shopping_lists WHERE name LIKE :name || '%'") // "買い物リスト%"のようなパターンにマッチ
    suspend fun getListNameCount(name: String): Int // suspendにする
}