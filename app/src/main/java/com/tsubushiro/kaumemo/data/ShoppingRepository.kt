package com.tsubushiro.kaumemo.data

import kotlinx.coroutines.flow.Flow

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

    suspend fun getShoppingListById(listId: Int): ShoppingList? {
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
}