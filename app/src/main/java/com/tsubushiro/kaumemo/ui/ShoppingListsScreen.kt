package com.tsubushiro.kaumemo.ui


//import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tsubushiro.kaumemo.data.ShoppingList
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // ExperimentalFoundationApi を追加
@Composable
fun ShoppingListsScreen(
    navController: NavController, // 画面遷移のためのNavController
    // ViewModelを引数で受け取るためのFactory
//    viewModel: ShoppingListsViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : ViewModel> create(modelClass: Class<T>): T {
//                val db = AppDatabase.getDatabase(navController.context) // コンテキストはNavControllerから取得
//                val repository = ShoppingRepository(db.shoppingListDao(), db.shoppingItemDao())
//                @Suppress("UNCHECKED_CAST")
//                return ShoppingListsViewModel(repository) as T
//            }
//        }
//    )
    shoppingListsViewModel: ShoppingViewModel // ここを変更
) {
    val shoppingLists by shoppingListsViewModel.shoppingLists.collectAsState() // ViewModelからリストの状態を収集
    var showAddListDialog by remember { mutableStateOf(false) } // リスト追加ダイアログの表示状態
    var showEditListDialog by remember { mutableStateOf(false) } // リスト編集ダイアログの表示状態
    var showConfirmDeleteDialog by remember { mutableStateOf(false) } // 削除確認ダイアログの状態
    var editingList by remember { mutableStateOf<ShoppingList?>(null) } // 編集中のリストを保持
    var deletingList by remember { mutableStateOf<ShoppingList?>(null) } // 削除対象のリストを保持

    val currentListId by shoppingListsViewModel.currentListId.collectAsState() // ★追加★

    // ★Compose-Reorderableの状態を管理するState★
    val haptic = LocalHapticFeedback.current // 触覚フィードバック
    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            // ViewModelの並び替えメソッドを呼び出す
//            Log.d("ShoppingListScreen","よんだ？_onMove")
            shoppingListsViewModel.onListReordered(from.index, to.index)
        },
        onDragEnd = { start, end ->
            // ドラッグ終了時に触覚フィードバック
//            Log.d("ShoppingListScreen","よんだ？_onDragEnd")
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "リスト編集"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = { navController.navigate("shopping_items_route/${currentListId}") }) {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "リスト一覧",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = "アイテム",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = (-4).dp) // ここを調整
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddListDialog = true }) {
                Icon(Icons.Filled.Add, "新しいリストを追加")
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
            state = state.listState, // ReorderableLazyListStateからLazyListStateを取得
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .reorderable(state) // reorderable修飾子を適
        ) {
            if (shoppingLists.isEmpty()) {
                item {
                    Text(
                        "まだリストがありません。\n右下のボタンから新しいリストを作成しましょう！",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                itemsIndexed(shoppingLists, key = { _, item -> item.id }) { index, shoppingList ->
                    ReorderableItem(state, key = shoppingList.id) { isDragging ->
                        val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "elevationAnimation").value
                        val alpha = animateFloatAsState(if (isDragging) 0.5f else 1f, label = "alphaAnimation").value
                        val scale = animateFloatAsState(if (isDragging) 1.05f else 1f, label = "scaleAnimation").value

                        ShoppingListCard( // ★ShoppingListCard Composableを呼び出す★
                            shoppingList = shoppingList,
                            onListClick = {
                                // タップで詳細画面へ遷移
                                navController.navigate("shopping_items_route/${it.id}")
                            },
                            onEditClick = { listToEdit ->
                                // 編集ダイアログ表示
                                editingList = listToEdit
                                showEditListDialog = true
                            },
                            onDeleteClick = { listToDelete ->
                                // 削除確認ダイアログ表示
                                deletingList = listToDelete
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
//                        Card(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(horizontal = 8.dp, vertical = 4.dp)
//                                .graphicsLayer { // ドラッグ時の視覚効果
//                                    scaleX = scale
//                                    scaleY = scale
//                                    this.alpha = alpha
//                                }
//                                .shadow(elevation, RoundedCornerShape(8.dp)) // マテリアルデザインの影
//                                .detectReorderAfterLongPress(state)
//                                .clickable { navController.navigate("shopping_items_route/${shoppingList.id}") }, // タップで詳細へ
//                            shape = RoundedCornerShape(8.dp),
//                  //          containerColor = MaterialTheme.colorScheme.surfaceVariant
//                        ) {
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                            //    verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Text(
//                                    text = shoppingList.name,
//                                    style = MaterialTheme.typography.titleMedium,
//                                    modifier = Modifier.weight(1f)
//                                )
//                                // ★ドラッグハンドルアイコンを追加★
//                                Icon(
//                                    Icons.Filled.MoreVert ,
//                                    contentDescription = "ドラッグして並べ替え",
//                                    modifier = Modifier
//                                        .size(24.dp)
//                                    // detectReorderAfterLongPress をカード全体ではなくハンドルに適用する場合
//                                    // .detectReorderAfterLongPress(state)
//                                )
//                                }
//                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding())) // FABとBottomBarの高さ分を確保
            }
        }
    }
    // リスト追加ダイアログ
    if (showAddListDialog) {
        AddListDialog(
            onAddList = { listName ->
                shoppingListsViewModel.addShoppingList(listName)
                showAddListDialog = false
            },
            onDismiss = { showAddListDialog = false }
        )
    }

    // ★ 新規追加: リスト編集ダイアログ ★
    if (showEditListDialog && editingList != null) {
        EditListDialog(
            shoppingList = editingList!!, // ! を使ってnullでないことを保証
            onEditList = { updatedList ->
                shoppingListsViewModel.updateShoppingList(updatedList) // ViewModelの更新メソッドを呼び出し
                showEditListDialog = false // ダイアログを閉じる
                editingList = null         // 編集対象をクリア
            },
            onDismiss = { // キャンセルまたはダイアログ外タップで閉じる
                showEditListDialog = false
                editingList = null
            }
        )
    }

    // ★削除確認ダイアログ★ ※Itemと同じ
    if (showConfirmDeleteDialog && deletingList != null) {
        ConfirmDeleteDialog(
            itemName = deletingList!!.name,
            onConfirmDelete = {
                shoppingListsViewModel.deleteShoppingList(deletingList!!) // ViewModelの削除メソッドを呼び出す
                showConfirmDeleteDialog = false
                deletingList = null
            },
            onDismiss = {
                showConfirmDeleteDialog = false
                deletingList = null
            }
        )
    }
}


// 個々の買い物リスト表示用Composable (大幅に修正)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListCard(
    shoppingList: ShoppingList,
    onListClick: (ShoppingList) -> Unit, // リストがクリックされた時のコールバック (詳細画面遷移)
    onEditClick: (ShoppingList) -> Unit, // 編集ボタンクリック時のコールバック
    onDeleteClick: (ShoppingList) -> Unit, // 削除ボタンクリック時のコールバック
    modifier: Modifier // 外側から渡される修飾子
) {
    Card(
        modifier = modifier // 外側から渡されるModifierを適用
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp) // CardDefaultsからコピー
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // アイテム全体をタップ可能にするためのModifier。
                // detectReorderAfterLongPressとは競合しないため、併用可能。
                // IconButtonは自身のonClickを持つため、ここでのclickableはリスト名タップに有効
                .clickable { onListClick(shoppingList) }
                .padding(4.dp), // 全体的なパディングを調整
            verticalAlignment = Alignment.CenterVertically // 垂直方向中央揃え
        ) {
            // 左側の編集ボタン
            IconButton(
                onClick = { onEditClick(shoppingList) },
                modifier = Modifier.size(48.dp) // タップターゲットを大きくする
            ) {
                Icon(Icons.Default.Edit, contentDescription = "リストを編集")
            }

            // リスト名 (中央に配置し、残りのスペースを占める)
            Text(
                text = shoppingList.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f) // 残りのスペースを占める
                    .padding(horizontal = 8.dp) // テキストとアイコンの間にパディング
            )

            // 右側の削除ボタン
            IconButton(
                onClick = { onDeleteClick(shoppingList) },
                modifier = Modifier.size(48.dp) // タップターゲットを大きくする
            ) {
                Icon(Icons.Default.Delete, contentDescription = "リストを削除")
            }

            // ドラッグハンドル (一番右に配置)
            // このアイコンは視覚的なインジケーターとしてのみ機能し、
            // detectReorderAfterLongPressはShoppingListCardのmodifierに適用済み
//            Icon(
//                Icons.Filled.MoreVert, // ドラッグ可能であることを示すアイコン
//                contentDescription = "ドラッグして並べ替え",
//                modifier = Modifier
//                    .size(24.dp) // アイコン自体のサイズ
//                    .padding(start = 4.dp) // 他のアイコンとの間隔
//            )
        }
    }
}

// リスト追加ダイアログComposable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddListDialog(
    onAddList: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var listName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新しいリストを作成") },
        text = {
            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                label = { Text("リスト名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (listName.isNotBlank()) {
                        onAddList(listName)
                    }
                }
            ) {
                Text("作成")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

// ★ 新規追加: リスト編集ダイアログComposable ★
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListDialog(
    shoppingList: ShoppingList, // 編集対象のリストを受け取る
    onEditList: (ShoppingList) -> Unit, // 編集後のリストを渡すコールバック
    onDismiss: () -> Unit // ダイアログを閉じるコールバック
) {
    var listName by remember { mutableStateOf(shoppingList.name) } // 初期値は現在のリスト名

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("リスト名を編集") },
        text = {
            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                label = { Text("リスト名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (listName.isNotBlank()) {
                        onEditList(shoppingList.copy(name = listName)) // コピーして名前だけ変更したリストを返す
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

