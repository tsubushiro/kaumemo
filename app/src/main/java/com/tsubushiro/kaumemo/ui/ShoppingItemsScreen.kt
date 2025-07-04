package com.tsubushiro.kaumemo.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tsubushiro.kaumemo.data.ShoppingItem
import kotlinx.coroutines.flow.collectLatest

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
        // ★修正点: listIdがnullでない場合のみ処理を実行★
        listId?.let { nonNullListId ->
            if (nonNullListId != currentShoppingList?.id) {
                shoppingItemsViewModel.updateCurrentListId(nonNullListId)
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
                                            color = MaterialTheme.colorScheme.background, // ★選択中のタブの背景色をアプリの背景色に★
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (currentShoppingList == null){
                item {
                    Text(
                        "アイテム読み込み中...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }else if (shoppingItems.isEmpty()) {
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
                items(shoppingItems) { item ->
                    ShoppingItemCard(
                        shoppingItem = item,
                        onTogglePurchased = { shoppingItemsViewModel.toggleItemPurchased(it) },
                        onEditClick = { itemToEdit ->
                            editingItem = itemToEdit
                            showEditItemDialog = true
                        },
                        // ★ 削除ボタンが押されたら（確認ダイアログ表示） ★
                        onDeleteClick = { itemToDeleteConfirm ->
                            itemToDelete = itemToDeleteConfirm
                            showConfirmDeleteDialog = true
                        }
//                        onDeleteClick = { itemToDelete ->
//                            shoppingItemsViewModel.deleteShoppingItem(itemToDelete)
//                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding())) // FABとBottomBarの高さ分を確保
            }
        }
    }

    // アイテム追加ダイアログ
    if (showAddItemDialog) {
        AddItemDialog(
            onAddItem = { itemName ->
                shoppingItemsViewModel.addShoppingItem(itemName)
                showAddItemDialog = false
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
    onDeleteClick: (ShoppingItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable( // 長押し対応
//                onClick = { onTogglePurchased(shoppingItem) }, // タップでチェック切り替え
//                onLongClick = { onEditClick(shoppingItem) } // 長押しで編集（簡易）
                onClick = { onEditClick(shoppingItem) }, // タップでチェック切り替え
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
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
    onAddItem: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新しいアイテムを追加") },
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
                        onAddItem(itemName)
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