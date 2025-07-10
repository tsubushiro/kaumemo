package com.tsubushiro.kaumemo.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tsubushiro.kaumemo.BuildConfig
import com.tsubushiro.kaumemo.data.ShoppingItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsScreen(
    navController: NavController,
    listId: Int?, // ナビゲーション引数としてリストIDを受け取る
    shoppingItemsViewModel: ShoppingViewModel // = hiltViewModel()
) {
    // ★ ViewModelからリスト名を収集 ★
//    val shoppingListName by shoppingItemsViewModel.shoppingListName.collectAsState()
    val currentShoppingList by shoppingItemsViewModel.currentShoppingList.collectAsStateWithLifecycle()

    val shoppingItems by shoppingItemsViewModel.shoppingItems.collectAsState() // ViewModelからアイテムの状態を収集
    var showAddItemDialog by remember { mutableStateOf(false) } // アイテム追加ダイアログ表示状態
    var showEditItemDialog by remember { mutableStateOf(false) } // アイテム編集ダイアログ表示状態
    var editingItem by remember { mutableStateOf<ShoppingItem?>(null) } // 編集中のアイテム
    var showConfirmDeleteDialog by remember { mutableStateOf(false) } // 削除確認ダイアログの表示状態
    var itemToDelete by remember { mutableStateOf<ShoppingItem?>(null) } // 削除対象のアイテム

    val shoppingLists by shoppingItemsViewModel.shoppingLists.collectAsState() // ★追加★
//    val currentListId by shoppingItemsViewModel.currentListId.collectAsState() // ★追加★

    // 現在選択されているタブのインデックスを見つける
    val selectedTabIndex = remember(currentShoppingList?.id, shoppingLists) {
        shoppingLists.indexOfFirst { it.id == currentShoppingList?.id }
    }

    // Navigation経由でlistIdが変更された場合に、ViewModelの現在のlistIdを更新する
    // これにより、タブ切り替えなどではなく、完全に別のリスト詳細画面に遷移した場合にVMが正しく対応できる
    LaunchedEffect(listId) {
        Log.d("PerfLog", "ShoppingItemScreen LaunchedEffect listId: ${System.currentTimeMillis()}")
        // ★修正点: listIdがnullでない場合のみ処理を実行★
        listId?.let { nonNullListId ->
            if (nonNullListId != currentShoppingList?.id) {
                shoppingItemsViewModel.updateCurrentListId(nonNullListId)
            }
        }
    }
    val snackbarHostState = remember { SnackbarHostState() } // ★スナックバーホストの状態を記憶★
    val scope = rememberCoroutineScope() // スナックバー表示のためのコルーチンスコープ

    // ViewModelからのスナックバーメッセージを監視
    LaunchedEffect(Unit) {
        shoppingItemsViewModel.snackbarMessage.collectLatest { message ->
            scope.launch { // コルーチン内でスナックバーを表示
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val context = LocalContext.current // ★追加: Contextを取得

    // ★追加: トーストメッセージを監視し表示するLaunchedEffect
    LaunchedEffect(Unit) {
        shoppingItemsViewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Listで使っている版
    val haptic = LocalHapticFeedback.current // 触覚フィードバック
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            // ViewModelの並び替えメソッドを呼び出す
//           Log.d("ShoppingItemScreen","よんだ？_onMove")
            shoppingItemsViewModel.onItemReordered(from.index, to.index)
        },
        onDragEnd = { start, end ->
            // ドラッグ終了時に触覚フィードバック
//            Log.d("ShoppingItemScreen","よんだ？_onDragEnd")
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    )

    // ★追加: アイテム追加イベントを監視し、最新行にスクロール★
    LaunchedEffect(Unit) { // このLaunchedEffectはコンポーネントのライフサイクルに一度だけ起動
        shoppingItemsViewModel.scrollEvent.collectLatest {
            // アイテムが追加された後、リストが更新されるまで少し待つ
            delay(50) // UIレンダリングのラグを考慮
            if (shoppingItems.isNotEmpty()) {
                state.listState.animateScrollToItem(shoppingItems.lastIndex)
            }
        }
    }

    Scaffold(
//        topBar = {
//            TopAppBar(
////                title = { Text(listName) },
//                title = { Text(shoppingListName) },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
//                    }
//                }
//            )
//        },
        topBar = {
            Column { // TopAppBarとTabRowをまとめるためにColumnを追加
//                TopAppBar(
//                    title = { Text(shoppingListName) },
//                    navigationIcon = {
//                        IconButton(onClick = { navController.popBackStack() }) { // 戻るボタン
//                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
//                        }
//                    },
//                    actions = {
//                        IconButton(onClick = { navController.navigate("shopping_lists_route") }) { // ★追加★
//                            Icon(Icons.AutoMirrored.Filled.List, "リスト管理") // リスト管理画面へのアイコン
//                        }
//                    }
//                )
                TopAppBar(
//                    title = { Text(shoppingListName ?: "読み込み中...") },
//                    title = { Text(currentShoppingList?.name ?: "読み込み中...") },
                    title = {
                        Text(
                            text = if (currentShoppingList == null) {
                                "読み込み中..."
                            } else {
                                "アイテム編集"
                            }
                        )
                            },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
//                    navigationIcon = {
//                        IconButton(onClick = { navController.popBackStack() }) {
//                            Icon(
//                                Icons.AutoMirrored.Filled.ArrowBack,
//                                contentDescription = "戻る",
//                                tint = MaterialTheme.colorScheme.onPrimary
//                            )
//                        }
//                    },
                    actions = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(onClick = { shoppingItemsViewModel.createNewListAndSwitchToIt() }) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "新しいリストを作成",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Text(
                                text = "リスト",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.offset(y = (-4).dp) // ここを調整
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp)) // アイコン間のスペース調整
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(onClick = { navController.navigate("shopping_lists_route") }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = "リスト一覧",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Text(
                                text = "リスト",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.offset(y = (-4).dp) // ここを調整
                            )
                        }
//                        IconButton(onClick = { shoppingItemsViewModel.createNewListAndSwitchToIt() }) {
//                            Icon(
//                                Icons.Filled.Add,
//                                contentDescription = "新しいリストを作成",
//                                tint = MaterialTheme.colorScheme.onPrimary
//                            )
//                        }
////                        IconButton(onClick = { showConfirmDeleteListDialog = true }) {
////                            Icon(
////                                Icons.Filled.Delete,
////                                contentDescription = "現在のリストを削除",
////                                tint = MaterialTheme.colorScheme.onPrimary
////                            )
////                        }
//                        IconButton(onClick = { navController.navigate("shopping_lists_route") }) {
//                            Icon(
//                                Icons.AutoMirrored.Filled.List,
//                                contentDescription = "リスト一覧",
//                                tint = MaterialTheme.colorScheme.onPrimary
//                            )
//                        }
                    }
                )

                // ★追加: タブバー（リスト切り替え用）
                if (shoppingLists.isNotEmpty()) { // リストが一つも無い場合はタブを表示しない
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex.coerceAtLeast(0), // インデックスが-1にならないように
                        containerColor = MaterialTheme.colorScheme.primaryContainer, // タブバー全体の背景色（そのまま）
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer, // 未選択タブのテキスト色など（そのまま）
                        // ★ここから追加・変更★
                        edgePadding = 0.dp, // 端のパディングをなくして、タブが左右いっぱいに使えるように（好みで調整）
                        indicator = {}
//                         indicator = { tabPositions ->
//                             TabRowDefaults.SecondaryIndicator(
//                                 modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex.coerceAtLeast(0)]),
//                                 color = MaterialTheme.colorScheme.primaryContainer,
//                             )
//                         },
                    ) {
                        shoppingLists.forEachIndexed { index, shoppingList ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { shoppingItemsViewModel.updateCurrentListId(shoppingList.id) }, // タブクリックでリストを切り替え
                                text = {
                                    Text(
                                        text = shoppingList.name,
                                        color = if (selectedTabIndex == index) {
                                            // ★選択中のタブの文字色 (背景がSecondaryなのでonSecondaryが最適)★
                                            MaterialTheme.colorScheme.secondary
                                        } else {
                                            MaterialTheme.colorScheme.onPrimaryContainer // 非選択のタブの文字色
                                        }
                                    )
                                },
                                // ★ここを修正: 選択中のタブの背景色を設定★
                                modifier = if (selectedTabIndex == index) {
                                    Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
//                                            shape = MaterialTheme.shapes.small // テーマで定義された角の丸みを使用
                                            // または RoundedCornerShape(8.dp) のように直接指定することも可能
                                        )
                                        // 背景色を適用した後に、Tab全体のパディングを調整すると見栄えが良くなります
                                       // .padding(horizontal = 16.dp, vertical = 8.dp) // 例: タブ内のコンテンツのパディングを調整
                                } else {
                                    // 未選択のタブには背景色なし（または別の色を指定）
//                                    Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // 未選択のタブにも同じパディングを適用して高さを揃える
                                    Modifier
                                }
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }, // ★ScaffoldにSnackbarHostを設定★
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddItemDialog = true }) {
                Icon(Icons.Filled.Add, "新しいアイテムを追加")
            }
        },
        // ★ ここにbottomBarを追加し、広告Composableを配置 ★
        bottomBar = {
            // Spacerなどを使い、広告の下にパディングを入れると見栄えが良くなる
            Column {
                AdViewComposable(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp)) // 広告の下に余白
            }
        }
    ) { paddingValues ->
        Surface( // ★ ここにSurfaceを追加 ★
           color = MaterialTheme.colorScheme.surfaceVariant // ★ ここで背景色を指定 ★
            // 例: 一般的なTodoアプリの背景色にするなら MaterialTheme.colorScheme.background
            // 例: カスタムカラーなら Color(0xFFE0E0E0) など
        ) {
            LazyColumn(
                state = state.listState, // ReorderableLazyListStateからLazyListStateを取得
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .reorderable(state) // reorderable修飾子を適
            ) {
                if (currentShoppingList == null) {
                    item {
                        Text(
                            "アイテム読み込み中...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else if (shoppingItems.isEmpty()) {
                    item {
                        Text(
                            "まだアイテムがありません。\n右下のボタンから新しいアイテムを追加しましょう！",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    itemsIndexed(
                        shoppingItems,
                        key = { _, item -> item.id }) { index, shoppingItem ->
                        ReorderableItem(state, key = shoppingItem.id) { isDragging ->
                            val elevation = animateDpAsState(
                                if (isDragging) 16.dp else 0.dp,
                                label = "elevationAnimation"
                            ).value
                            val alpha = animateFloatAsState(
                                if (isDragging) 0.5f else 1f,
                                label = "alphaAnimation"
                            ).value
                            val scale = animateFloatAsState(
                                if (isDragging) 1.05f else 1f,
                                label = "scaleAnimation"
                            ).value

                            LaunchedEffect(isDragging) {
                                if (isDragging) {
//                                Log.d("DragDebug", "ドラッグ開始: アイテム名 = ${shoppingItem.name}, ID = ${shoppingItem.id}")
                                    // ドラッグ開始の振動
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress) // 長押し相当のフィードバック
                                } else {
                                    // ドラッグ終了時（ドロップまたはキャンセル）にもログが出ます
//                                Log.d("DragDebug", "ドラッグ終了: アイテム名 = ${shoppingItem.name}, ID = ${shoppingItem.id}")
                                }
                            }

                            ShoppingItemCard( // ★ShoppingItemCard Composableを呼び出す★
                                shoppingItem = shoppingItem,
                                onTogglePurchased = { shoppingItemsViewModel.toggleItemPurchased(it) },
                                onEditClick = { itemToEdit ->
                                    editingItem = itemToEdit
                                    showEditItemDialog = true
                                },
                                onDeleteClick = { itemToDeleteConfirm ->
                                    itemToDelete = itemToDeleteConfirm
                                    showConfirmDeleteDialog = true
                                },
                                modifier = Modifier
                                    .graphicsLayer { // ドラッグ時の視覚効果
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                    }
                                    .shadow(elevation, RoundedCornerShape(8.dp)) // マテリアルデザインの影
                                    .detectReorderAfterLongPress(state) // ★ドラッグ開始のジェスチャーをここに適用★
                            )
                        }
                    }
//                 ドラッグアンドドロップによるソート対応前
//                items(shoppingItems) { item ->
//                    ShoppingItemCard(
//                        shoppingItem = item,
//                        onTogglePurchased = { shoppingItemsViewModel.toggleItemPurchased(it) },
//                        onEditClick = { itemToEdit ->
//                            editingItem = itemToEdit
//                            showEditItemDialog = true
//                        },
//                        // ★ 削除ボタンが押されたら（確認ダイアログ表示） ★
//                        onDeleteClick = { itemToDeleteConfirm ->
//                            itemToDelete = itemToDeleteConfirm
//                            showConfirmDeleteDialog = true
//                        }
////                        onDeleteClick = { itemToDelete ->
////                            shoppingItemsViewModel.deleteShoppingItem(itemToDelete)
////                        }
//                    )
//                }
                }
                item {
                    Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding())) // FABとBottomBarの高さ分を確保
                    DebugCheckpointButton(shoppingViewModel = shoppingItemsViewModel)
                }
            }
        }
    }

    // アイテム追加ダイアログ
    if (showAddItemDialog) {
        AddItemDialog(
            onAddItem = { itemName, isContinuous -> // isContinuous を受け取るように変更
                shoppingItemsViewModel.addShoppingItem(itemName)
                if (!isContinuous) { // 連続入力モードでなければダイアログを閉じる
                    showAddItemDialog = false
                }
            },
            onDismiss = { showAddItemDialog = false }
        )
    }

    // アイテム編集ダイアログ
    if (showEditItemDialog && editingItem != null) {
        EditItemDialog(
            shoppingItem = editingItem!!,
            onEditItem = { updatedItem ->
                shoppingItemsViewModel.updateShoppingItem(updatedItem)
                showEditItemDialog = false
                editingItem = null
            },
            onDismiss = {
                showEditItemDialog = false
                editingItem = null
            }
        )
    }

    // ★ 新規追加: 削除確認ダイアログ ★
    if (showConfirmDeleteDialog && itemToDelete != null) {
        ConfirmDeleteDialog(
            itemName = itemToDelete!!.name,
            onConfirmDelete = {
                shoppingItemsViewModel.deleteShoppingItem(itemToDelete!!) // 削除実行
                showConfirmDeleteDialog = false
                itemToDelete = null
            },
            onDismiss = {
                showConfirmDeleteDialog = false
                itemToDelete = null
            }
        )
    }
}

// 個々の買い物アイテム表示用Composable
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingItemCard(
    shoppingItem: ShoppingItem,
    onTogglePurchased: (ShoppingItem) -> Unit,
    onEditClick: (ShoppingItem) -> Unit,
    onDeleteClick: (ShoppingItem) -> Unit,
    modifier: Modifier // 外側から渡される修飾子
) {
    Card(
        modifier = modifier // 外側から渡されるModifierを適用
            .fillMaxWidth()
            .padding(vertical = 4.dp),
//          .clickable{ onEditClick(shoppingItem) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
                containerColor = if (shoppingItem.isPurchased) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surface, // ★ 背景を白に変更 ★
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .clickable { onEditClick(shoppingItem) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // チェックボックス
            Checkbox(
                checked = shoppingItem.isPurchased,
                onCheckedChange = { onTogglePurchased(shoppingItem) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            // アイテム名 (購入済みなら取り消し線)
            Text(
                text = shoppingItem.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (shoppingItem.isPurchased) TextDecoration.LineThrough else null,
                    color = if (shoppingItem.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f) // 残りのスペースを埋める
            )
            // 削除ボタン (簡易的にアイコンボタンを配置)
            IconButton(onClick = { onDeleteClick(shoppingItem) }) {
                Icon(Icons.Default.Delete, contentDescription = "削除")
            }
        }
    }
}


// 新規追加: アイテム追加ダイアログComposable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    onAddItem: (String , Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var isContinuousMode by remember { mutableStateOf(false) } // ★連続入力モードの状態★
    var showConfirmationMessage by remember { mutableStateOf(false) }
    var lastAddedItemName by remember { mutableStateOf("") } // ★新たに追加: 最後に追加したアイテム名を保持★

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新しいアイテムを追加") },
        text = {
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("アイテム名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 「連続入力」チェックボックスをここに追加
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End // 右寄せ
                ) {
                    // ★追加通知メッセージ★
                    if (showConfirmationMessage) {
                        // ★メッセージを「〇〇を追加しました！」に変更★
                        Text(
                            text = "「${lastAddedItemName}」を追加しました！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                        )
                        // 短時間でメッセージを消す
                        LaunchedEffect(lastAddedItemName) { // キーをlastAddedItemNameに変更 (メッセージ表示ごとにリコンポーズ)
                            kotlinx.coroutines.delay(1500) // 1.5秒表示
                            showConfirmationMessage = false
                        }
                    }
                    Checkbox(
                        checked = isContinuousMode,
                        onCheckedChange = { isContinuousMode = it }
                    )
                    Text("連続入力") // テキストを「連続入力」に短縮

                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotBlank()) {
                        onAddItem(itemName, isContinuousMode) // ★isContinuousMode を渡す★
                        lastAddedItemName = itemName // ★追加: 追加するアイテム名を保存★
                        itemName = "" // テキストフィールドをクリア
                        showConfirmationMessage = true // メッセージ表示をトリガー
                    }
                }
            ) {
                Text("追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

// 新規追加: アイテム編集ダイアログComposable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(
    shoppingItem: ShoppingItem,
    onEditItem: (ShoppingItem) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf(shoppingItem.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("アイテムを編集") },
        text = {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("アイテム名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (itemName.isNotBlank()) {
                        onEditItem(shoppingItem.copy(name = itemName))
                    }
                }
            ) {
                Text("更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    itemName: String, // 削除するアイテムの名前
    onConfirmDelete: () -> Unit, // 削除確定時のコールバック
    onDismiss: () -> Unit // ダイアログを閉じるコールバック
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("削除の確認") },
        text = { Text("「$itemName」を削除してもよろしいですか？") },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // 削除ボタンは赤色に
            ) {
                Text("削除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

// デバッグ用チェックポイントボタン
@Composable
fun DebugCheckpointButton(
    shoppingViewModel: ShoppingViewModel,
    modifier: Modifier = Modifier
) {
    if (BuildConfig.DEBUG) { // デバッグビルド時のみ表示
        Button(
            onClick = { shoppingViewModel.runCheckpointForDebugging() },
            modifier = modifier,
            // ボタンの色を警戒色に
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
        ) {
            Text("DBを強制チェックポイント (DEBUG)")
        }
    }
}