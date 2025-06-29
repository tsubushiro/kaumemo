package com.tsubushiro.kaumemo.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsubushiro.kaumemo.data.ShoppingItem
import com.tsubushiro.kaumemo.data.ShoppingList
import com.tsubushiro.kaumemo.data.ShoppingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingItemsViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    savedStateHandle: SavedStateHandle // ナビゲーション引数を受け取るため
) : ViewModel() {

    // ナビゲーション引数からlistIdを取得
    // private val listId: Int = checkNotNull(savedStateHandle["listId"])
 //   private val listId: Int = 1

    // ナビゲーション引数から渡されるlistId (初回起動時は利用されないが、タブ切り替えで使われる)
    private val navListId: Int? = savedStateHandle["listId"]

    // 現在表示中のリストのIDを管理するStateFlow
    private val _currentListId = MutableStateFlow<Int?>(null)
    val currentListId = _currentListId.asStateFlow()

    init {
        viewModelScope.launch {
            Log.d("PerfLog", "ViewModel init block Start: ${System.currentTimeMillis()}")
            // アプリ起動時にデフォルトリストを作成/最初のリストを決定
//            try{
                // repository.createDefaultShoppingListIfNeeded() は Long を返すようになった
                // そのため、結果を Int に変換して _currentListId.value にセット
                val resolvedListIdLong: Long = navListId?.toLong() // navListIdがInt?なのでLong?に変換
                    ?: repository.createDefaultShoppingListIfNeeded()

                // Long を Int に安全に変換してセット
                _currentListId.value = resolvedListIdLong.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
//            }catch(e: Exception){
//                Log.d("ShopppingItemViewModel","よんだ？"+e.toString())
//            }
            Log.d("PerfLog", "ViewModel init block End: ${System.currentTimeMillis()}")

            // currentListId の変更を監視し、allShoppingLists のデータが揃ったら初期リストが選択されるようにする
            // allShoppingLists.collect { lists ->
            //     if (_currentListId.value == null && lists.isNotEmpty()) {
            //         _currentListId.value = navListId ?: lists.first().id // タブ切り替え後の初回起動リスト
            //     } else if (_currentListId.value == null && lists.isEmpty()) {
            //         // まだリストがない場合は自動作成
            //         val newDefaultListId = repository.createDefaultShoppingListIfNeeded().toInt()
            //         _currentListId.value = newDefaultListId
            //     }
            // }
            // 上のコメントアウト部分を init ロックの最初に書いても同じ
        }
    }
    val shoppingListName: StateFlow<String> =
        _currentListId.filterNotNull() // nullが流れてくることを防ぐ
            .flatMapLatest { listId -> // listIdの変更を監視し、その都度リスト名を取得
                repository.getShoppingListById(listId)
            }
            .map { shoppingList ->
                shoppingList?.name ?: "不明なリスト"
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = "読込中..."
            )

    val shoppingItems: StateFlow<List<ShoppingItem>> =
        _currentListId.filterNotNull() // nullが流れてくることを防ぐ
            .flatMapLatest { listId -> // listIdの変更を監視し、その都度アイテムを取得
                repository.getShoppingItemsForList(listId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * 新しい買い物アイテムを追加する
     * @param name 追加するアイテムの名前
     */
//    fun addShoppingItem(name: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val newItem = ShoppingItem(listId = listId, name = name)
//            repository.insertShoppingItem(newItem)
//        }
//    }
    fun addShoppingItem(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentListIdValue = _currentListId.value // 現在のリストIDを取得

            if (currentListIdValue != null) { // IDがnullでないことを確認
                val newItem = ShoppingItem(listId = currentListIdValue, name = name) // ★ここを修正★
                repository.insertShoppingItem(newItem)
            } else {
                // エラーハンドリング: リストIDがまだ設定されていない場合
                // 例: Log.e("ShoppingItemsViewModel", "Cannot add item: currentListId is null")
                // または、ユーザーに通知するUIを表示するStateを更新
            }
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

    val allShoppingLists: StateFlow<List<ShoppingList>> =
        repository.getAllShoppingListsSorted() // ★改めて確認★
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // onTabSelected はフェーズ1.4で既に実装済み
    fun onTabSelected(listId: Int) {
        _currentListId.value = listId
    }

    fun createNewListAndSwitchToIt() {
        Log.d("Debug","よんだ？")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newListName = repository.generateNewShoppingListName()
                // 最新のallShoppingListsから最大orderIndexを取得
                val newOrderIndex = (allShoppingLists.value.maxOfOrNull { it.orderIndex } ?: -1) + 1
                val newList = ShoppingList(name = newListName, orderIndex = newOrderIndex)
                val newListId = repository.insertShoppingList(newList)
                    .toInt() // insertShoppingListはLongを返すのでIntに変換
                Log.d("Debug","newListid : ${newListId.toString()}")
                delay(1000)
                _currentListId.value = newListId // 新しいリストに切り替える
            }catch(e: Exception){
                Log.d("Debug",e.message.toString())
            }
        }
    }

    /**
     * 現在の買い物リストを更新する
     * @param listId 更新するリストのID
     */
    fun updateCurrentListId(listId: Int) {
        _currentListId.value = listId
        Log.d("ShoppingItemsViewModel", "Current List ID updated to: $listId")
    }
}