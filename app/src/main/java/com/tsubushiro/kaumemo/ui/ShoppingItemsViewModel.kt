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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
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
//    private val navListId: Int? = savedStateHandle["listId"]

    // 現在表示中のリストのIDを管理するStateFlow
    private val _currentListId = MutableStateFlow<Int?>(null)
    val currentListId = _currentListId.asStateFlow()

//    init {
//        viewModelScope.launch {
//            Log.d("PerfLog", "ViewModel init block Start: ${System.currentTimeMillis()}")
//            val navListIdFromSavedState: Int? = savedStateHandle["listId"]
//
//            // 初回起動時にリストIDを決定し、設定する
//            val resolvedInitialId = determineAndSetInitialListId(navListIdFromSavedState)
//            _currentListId.value = resolvedInitialId
//
//            Log.d("PerfLog", "ViewModel init block End: ${System.currentTimeMillis()}")
//        }
//    }

    /**
     * 指定されたIDと現在のリストの状態に基づいて、最終的に設定すべきリストIDを決定する共通ロジック。
     * リストが存在しない場合は新規作成、指定IDが無効な場合は最初のリストにフォールバックします。
     *
     * @param preferredListId 優先したいリストID（ナビゲーション引数、またはタブ選択からのID）
     * @return 最終的に決定されたリストID
     */
    private suspend fun determineAndSetInitialListId(preferredListId: Int?): Int {
        val allExistingLists = repository.getAllShoppingListsSorted().first() // 最新のリストデータを取得

        return if (allExistingLists.isEmpty()) {
            // 1. ShoppingListにデータがない場合: 新規のデータを作成してそのidを返す
            val newDefaultListId = repository.createDefaultShoppingListIfNeeded().toInt()
            Log.d("ShoppingItemsViewModel", "DETERMINE: No lists found. Created new default list ID: $newDefaultListId")
            newDefaultListId
        } else {
            // preferredListIdが指定されており、かつそのIDのリストが存在するか確認
            val targetListExists = preferredListId != null && allExistingLists.any { it.id == preferredListId }

            if (targetListExists) {
                // 2. preferredListIdが有効な場合、それを返す
                Log.d("ShoppingItemsViewModel", "DETERMINE: Valid preferredListId found. Using ID: $preferredListId")
                preferredListId!! // nullチェック済みなので !! を使用
            } else {
                // 3. preferredListIdが無効な場合（存在しない場合）、ソート順で一番若いリストにフォールバック
                val firstListId = allExistingLists.first().id
                Log.d("ShoppingItemsViewModel", "DETERMINE: PreferredListId $preferredListId not found or null. Fallback to first sorted list: $firstListId")
                firstListId
            }
        }
    }

    /**
     * Composableからナビゲーション引数として受け取ったID、またはタブ選択で指定されたIDを処理し、
     * _currentListIdを更新する（存在しないIDの場合は最初のリストにフォールバック）。
     *
     * @param incomingListId ナビゲーションやタブ選択で指定されたリストID
     */
    fun resolveAndSetCurrentListId(incomingListId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 現在のViewModelの状態が既に最新であれば何もしない
            if (_currentListId.value == incomingListId) {
                Log.d("ShoppingItemsViewModel", "RESOLVE: Already on list ID: $incomingListId. No change needed.")
                return@launch
            }

            // ★ 既存のリストへの切り替えもローディング状態を考慮する場合はここに_isLoadingNewList.value = trueを追加 ★
            val resolvedId = determineAndSetInitialListId(incomingListId)
            _currentListId.value = resolvedId
            Log.d("ShoppingItemsViewModel", "RESOLVE: Set current list ID to resolved ID: $resolvedId (from incoming: $incomingListId)")
            // ★ ローディング状態を考慮する場合はここに_isLoadingNewList.value = falseを追加 ★
        }
    }

//    val shoppingListName: StateFlow<String> =
//        _currentListId.filterNotNull() // nullが流れてくることを防ぐ
//            .flatMapLatest { listId -> // listIdの変更を監視し、その都度リスト名を取得
//                repository.getShoppingListById(listId)
//            }
//            .map { shoppingList ->
//                shoppingList?.name ?: "不明なリスト"
//            }
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = "読込中..."
//            )

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
//            try {
                val newListName = repository.generateNewShoppingListName()
                // 最新のallShoppingListsから最大orderIndexを取得
                val newOrderIndex = (allShoppingLists.value.maxOfOrNull { it.orderIndex } ?: -1) + 1
                val newList = ShoppingList(name = newListName, orderIndex = newOrderIndex)
                val newListId = repository.insertShoppingList(newList)
                    .toInt() // insertShoppingListはLongを返すのでIntに変換
//                Log.d("Debug","newListid : ${newListId.toString()}")
                _currentListId.value = newListId // 新しいリストに切り替えある
//            }catch(e: Exception){
//                Log.d("Debug",e.message.toString())
//            }
        }
    }

    val currentShoppingList: StateFlow<ShoppingList?> =
        _currentListId.filterNotNull()
            .flatMapLatest { listId ->
                repository.getShoppingListById(listId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    /**
     * 現在の買い物リストを更新する
     * @param listId 更新するリストのID
     */
    fun updateCurrentListId(listId: Int) {
//        _currentListId.value = listId
        // updateCurrentListId は resolveAndSetCurrentListId を呼び出すようにする
        // これにより、タブ選択で無効なIDが渡された場合もフォールバックロジックが適用される
        resolveAndSetCurrentListId(listId)
        Log.d("ShoppingItemsViewModel", "Current List ID updated to: $listId")
    }
}