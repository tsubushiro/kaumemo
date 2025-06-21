package com.tsubushiro.kaumemo.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ShoppingRepository(
    private val shoppingListDao: ShoppingListDao,
    private val shoppingItemDao: ShoppingItemDao
) {
    // --- ShoppingList関連の操作 ---
    fun getAllShoppingLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.getAllShoppingLists()
    }

    suspend fun insertShoppingList(shoppingList: ShoppingList) {
        shoppingListDao.insert(shoppingList)
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
    fun getShoppingItemsForList(listId: Int): Flow<List<ShoppingItem>> {
        return shoppingItemDao.getShoppingItemsForList(listId)
    }

    suspend fun insertShoppingItem(shoppingItem: ShoppingItem) {
        shoppingItemDao.insert(shoppingItem)
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

    fun getAllShoppingListsSorted(): Flow<List<ShoppingList>> {
        return shoppingListDao.getAllShoppingListsOrderByOrderIndex()
    }

    suspend fun createDefaultShoppingListIfNeeded(): Long {
        val existingLists = shoppingListDao.getAllShoppingListsOrderByOrderIndex().firstOrNull() // Flowを一度だけ収集
        return (if (existingLists.isNullOrEmpty()) {
            val defaultListName = "買い物メモ"
            val newOrderIndex = (shoppingListDao.getLastListOrderIndex() ?: -1) + 1
            val defaultList = ShoppingList(name = defaultListName, orderIndex = newOrderIndex)
            shoppingListDao.insert(defaultList)
        } else {
            existingLists.first().id // 既存の最初のリストのIDを返す
        }) as Long
    }

    suspend fun generateNewShoppingListName(baseName: String = "買い物リスト"): String {
        val count = shoppingListDao.getListNameCount(baseName)
        return if (count == 0) baseName else "$baseName${count + 1}"
    }
}