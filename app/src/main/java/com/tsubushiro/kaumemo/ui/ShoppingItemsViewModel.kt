package com.tsubushiro.kaumemo.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsubushiro.kaumemo.data.ShoppingItem
import com.tsubushiro.kaumemo.data.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingItemsViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    savedStateHandle: SavedStateHandle // ナビゲーション引数を受け取るため
) : ViewModel() {

    // ナビゲーション引数からlistIdを取得
    private val listId: Int = checkNotNull(savedStateHandle["listId"])

    // 特定のリストに紐づく買い物アイテムを公開
    val shoppingItems: StateFlow<List<ShoppingItem>> =
        repository.getShoppingItemsForList(listId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * 新しい買い物アイテムを追加する
     * @param name 追加するアイテムの名前
     */
    fun addShoppingItem(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = ShoppingItem(listId = listId, name = name)
            repository.insertShoppingItem(newItem)
        }
    }

    /**
     * 買い物アイテムの購入状態を切り替える
     * @param item 切り替えるアイテムオブジェクト
     */
    fun toggleItemPurchased(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedItem = item.copy(isPurchased = !item.isPurchased)
            repository.updateShoppingItem(updatedItem)
        }
    }

    /**
     * 買い物アイテムを更新する
     * @param item 更新するアイテムオブジェクト
     */
    fun updateShoppingItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShoppingItem(item)
        }
    }

    /**
     * 買い物アイテムを削除する
     * @param item 削除するアイテムオブジェクト
     */
    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteShoppingItem(item)
        }
    }
}