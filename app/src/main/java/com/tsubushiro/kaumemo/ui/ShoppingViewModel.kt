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
class ShoppingViewModel @Inject constructor(
    private val repository: ShoppingRepository,
    savedStateHandle: SavedStateHandle // ナビゲーション引数を受け取るため
) : ViewModel() {

    // 現在表示中のリストのIDを管理するStateFlow
    private val _currentListId = MutableStateFlow<Int?>(null)
    val currentListId = _currentListId.asStateFlow()

    // リスト処理
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
            val newDefaultListId = repository.createNewListAndSwitchToIt()
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

    // リスト処理
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

    // アイテム処理
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

    // アイテム処理
    /**
     * 新しい買い物アイテムを追加する
     * @param name 追加するアイテムの名前
     */
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

    // アイテム処理
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

    // アイテム処理
    /**
     * 買い物アイテムを更新する
     * @param item 更新するアイテムオブジェクト
     */
    fun updateShoppingItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShoppingItem(item)
        }
    }

    // アイテム処理
    /**
     * 買い物アイテムを削除する
     * @param item 削除するアイテムオブジェクト
     */
    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteShoppingItem(item)
        }
    }


    // リスト処理
    // 新規リスト
    fun createNewListAndSwitchToIt() {
        viewModelScope.launch(Dispatchers.IO) {
            _currentListId.value = repository.createNewListAndSwitchToIt()
        }
    }

    // アイテム・リスト処理
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

    //　リスト処理
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

    // リスト処理
    // 全ての買い物リストをFlowとして公開し、UIが監視できるようにする
    val shoppingLists: StateFlow<List<ShoppingList>> =
//        repository.getAllShoppingLists()
        repository.getAllShoppingListsSorted() // ★変更★
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000), // UIが表示されている間購読を続ける
                initialValue = emptyList() // 初期値は空のリスト
            )

    // リスト処理
    // ★追加: リストの並び替えロジック★
    fun onListReordered(fromIndex: Int, toIndex: Int) {
        val currentList = shoppingLists.value.toMutableList()
        if (fromIndex < 0 || fromIndex >= currentList.size ||
            toIndex < 0 || toIndex >= currentList.size) {
            return // 無効なインデックス
        }

        val movedItem = currentList.removeAt(fromIndex)
        currentList.add(toIndex, movedItem)

        // 新しい orderIndex を割り当て
        val updatedLists = currentList.mapIndexed { index, list ->
            list.copy(orderIndex = index)
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShoppingListOrder(updatedLists) // リポジトリを通じて永続化
        }
    }

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