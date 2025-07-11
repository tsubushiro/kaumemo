package com.tsubushiro.kaumemo.data

import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ShoppingRepository(
    private val shoppingListDao: ShoppingListDao,
    private val shoppingItemDao: ShoppingItemDao
) {
    // --- ShoppingList関連の操作 ---
//    fun getAllShoppingLists(): Flow<List<ShoppingList>> {
//        return shoppingListDao.getAllShoppingLists()
//    }

//    suspend fun insertShoppingList(shoppingList: ShoppingList):Long {
//        shoppingListDao.insert(shoppingList)
//    }

    suspend fun insertShoppingList(shoppingList: ShoppingList): Long { // ★ここを 'Long' に変更★
        val newOrderIndex = (shoppingListDao.getLastListOrderIndex() ?: -1) + 1 // 後ろへ詰めるため
        val orderedShoppingList = shoppingList.copy(orderIndex = newOrderIndex)
        return shoppingListDao.insert(orderedShoppingList)
    }

    suspend fun updateShoppingList(shoppingList: ShoppingList) {
        shoppingListDao.update(shoppingList)
    }

    suspend fun deleteShoppingList(shoppingList: ShoppingList) {
        shoppingListDao.delete(shoppingList)
    }

    fun getShoppingListById(listId: Int): Flow<ShoppingList?> {
        return shoppingListDao.getShoppingListById(listId)
    }

    // --- ShoppingItem関連の操作 ---
//    fun getShoppingItemsForList(listId: Int): Flow<List<ShoppingItem>> {
//        return shoppingItemDao.getShoppingItemsForList(listId)
//    }

//    suspend fun insertShoppingItem(shoppingItem: ShoppingItem) {
//        shoppingItemDao.insert(shoppingItem)
//    }

    // ソートの実装
    suspend fun insertShoppingItem(shoppingItem: ShoppingItem) {
        val listId = shoppingItem.listId
        val newOrderIndex = (shoppingItemDao.getLastItemOrderIndex(listId) ?: -1) + 1 // 後ろへ詰めるため
        val orderedShoppingItem = shoppingItem.copy(orderIndex = newOrderIndex)
        shoppingItemDao.insert(orderedShoppingItem)
    }

    suspend fun updateShoppingItem(shoppingItem: ShoppingItem) {
        shoppingItemDao.update(shoppingItem)
    }

    suspend fun deleteShoppingItem(shoppingItem: ShoppingItem) {
        shoppingItemDao.delete(shoppingItem)
    }

    suspend fun getShoppingItemById(itemId: Int): ShoppingItem? {
        return shoppingItemDao.getShoppingItemById(itemId)
    }

    // ShoppingListDao の新しいソート済み取得メソッドをラップ。
    fun getAllShoppingListsSorted(): Flow<List<ShoppingList>> {
        return shoppingListDao.getAllShoppingListsOrderByOrderIndex()
    }

    // ShoppingItemDao の新しいソート済み取得メソッドをラップ(上記のItem版)。
    fun getAllShoppingItemsSorted(listId:Int): Flow<List<ShoppingItem>> {
        return shoppingItemDao.getAllShoppingItemsOrderByOrderIndex(listId)
    }

    // 初期リスト作成ロロジック。
    suspend fun createDefaultShoppingListIfNeeded(): Long { // ★返り値をLongに統一★
        Log.d("PerfLog", "createDefaultShoppingListIfNeeded Start: ${System.currentTimeMillis()}")
        Log.d("PerfLog", "getAllShoppingListsOrderByOrderIndex Start: ${System.currentTimeMillis()}")
        val existingLists = shoppingListDao.getAllShoppingListsOrderByOrderIndex().firstOrNull() // Flowを一度だけ収集
        Log.d("PerfLog", "getAllShoppingListsOrderByOrderIndex End: ${System.currentTimeMillis()}")
        // ToDo:パフォーマンス問題用 元に戻す
//        return if (existingLists.isNullOrEmpty()) {
//            val defaultListName = "買い物メモ"
//            val newOrderIndex = (shoppingListDao.getLastListOrderIndex() ?: -1) + 1
//            val defaultList = ShoppingList(name = defaultListName, orderIndex = newOrderIndex)
//            shoppingListDao.insert(defaultList) // この結果はLong
//        } else {
//            existingLists.first().id.toLong() // ★IntをLongに変換してから返す★
//        }
        return if (existingLists.isNullOrEmpty()) {
            val defaultListName = "新規リスト"
            val newOrderIndex = (shoppingListDao.getLastListOrderIndex() ?: -1) + 1
            val defaultList = ShoppingList(name = defaultListName, orderIndex = newOrderIndex)
            Log.d("PerfLog", "insert defaultList Start: ${System.currentTimeMillis()}")
            val id = shoppingListDao.insert(defaultList)
            Log.d("PerfLog", "insert defaultList End: ${System.currentTimeMillis()}")
            Log.d("PerfLog", "createDefaultShoppingListIfNeeded End (new): ${System.currentTimeMillis()}")
            id
        } else {
            val id = existingLists.first().id.toLong()
            Log.d("PerfLog", "createDefaultShoppingListIfNeeded End (existing): ${System.currentTimeMillis()}")
            id
        }
    }

    // ViewModel側のcreateNewListをレポジトリ側へ（createDefaultShoppingListIfNeededは不要)
    suspend fun createNewListAndSwitchToIt() :Int{
        Log.d("PerfLog", "createNewListAndSwitchToIt Start: ${System.currentTimeMillis()}")
        val newListName = generateNewShoppingListName()
        val newOrderIndex = (shoppingListDao.getLastListOrderIndex() ?: -1) + 1 // 後ろへ詰めるため
        val defaultList = ShoppingList(name = newListName, orderIndex = newOrderIndex)
        Log.d("PerfLog", "insert newlist Start: ${System.currentTimeMillis()}")
        val id = shoppingListDao.insert(defaultList)
        Log.d("PerfLog", "insert newList End: ${System.currentTimeMillis()}")
        Log.d("PerfLog", "createNewListAndSwitchToIt End (new): ${System.currentTimeMillis()}")
        return id.toInt()
    }



    // 連番リスト名生成ロジック。
    suspend fun generateNewShoppingListName(baseName: String = "新規リスト"): String {
        val count = shoppingListDao.getListNameCount(baseName)
        return if (count == 0) baseName else "$baseName${count + 1}"
    }

//    fun getAllShoppingListsOrderByOrderIndex(): Flow<List<ShoppingList>>{
//        return shoppingListDao.getAllShoppingListsOrderByOrderIndex()
//    }
//    suspend fun getLastListOrderIndex(): Int?{
//        return shoppingListDao.getLastListOrderIndex()
//    }
//
//    suspend fun getListNameCount(name: String): Int{
//        return shoppingListDao.getListNameCount(name = name)
//    }

    suspend fun updateShoppingListOrder(lists: List<ShoppingList>) {
        lists.forEach { list ->
            shoppingListDao.update(list)
        }
    }

    // 上のItem版 orderIndexの更新
    suspend fun updateShoppingItemOrder(lists: List<ShoppingItem>) {
        lists.forEach { list ->
            shoppingItemDao.update(list)
        }
    }

    // 空かどうか？
    suspend fun hasAnyShoppingLists(): Boolean {
        return shoppingListDao.hasAnyShoppingLists()
    }

    // Walのデータをチェックポイントするメソッド
    suspend fun forceWalCheckpoint(): Int {
        val query = SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")
        return shoppingListDao.checkpoint(query)
    }
}