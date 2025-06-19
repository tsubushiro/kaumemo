package com.tsubushiro.kaumemo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsubushiro.kaumemo.data.ShoppingList
import com.tsubushiro.kaumemo.data.ShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers // 追加

class ShoppingListsViewModel(private val repository: ShoppingRepository) : ViewModel() {

    // 全ての買い物リストをFlowとして公開し、UIが監視できるようにする
    val shoppingLists: StateFlow<List<ShoppingList>> =
        repository.getAllShoppingLists()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // UIが表示されている間購読を続ける
                initialValue = emptyList() // 初期値は空のリスト
            )

    /**
     * 新しい買い物リストを追加する
     * @param name 追加するリストの名前
     */
    fun addShoppingList(name: String) {
        // ViewModelScopeを使ってコルーチンを起動し、IOディスパッチャでDB操作を行う
        viewModelScope.launch(Dispatchers.IO) {
            val newList = ShoppingList(name = name)
            repository.insertShoppingList(newList)
        }
    }

    /**
     * 買い物リストを更新する
     * @param shoppingList 更新するリストオブジェクト
     */
    fun updateShoppingList(shoppingList: ShoppingList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShoppingList(shoppingList)
        }
    }

    /**
     * 買い物リストを削除する
     * @param shoppingList 削除するリストオブジェクト
     */
    fun deleteShoppingList(shoppingList: ShoppingList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteShoppingList(shoppingList)
        }
    }
}