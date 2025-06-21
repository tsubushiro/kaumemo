package com.tsubushiro.kaumemo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tsubushiro.kaumemo.data.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingItemsScreen(
    navController: NavController,
    listId: Int, // ナビゲーション引数としてリストIDを受け取る
    viewModel: ShoppingItemsViewModel = hiltViewModel()
) {
    // ★ ViewModelからリスト名を収集 ★
    val shoppingListName by viewModel.shoppingListName.collectAsState()

    val shoppingItems by viewModel.shoppingItems.collectAsState() // ViewModelからアイテムの状態を収集
    var showAddItemDialog by remember { mutableStateOf(false) } // アイテム追加ダイアログ表示状態
    var showEditItemDialog by remember { mutableStateOf(false) } // アイテム編集ダイアログ表示状態
    var editingItem by remember { mutableStateOf<ShoppingItem?>(null) } // 編集中のアイテム
    var showConfirmDeleteDialog by remember { mutableStateOf(false) } // 削除確認ダイアログの表示状態
    var itemToDelete by remember { mutableStateOf<ShoppingItem?>(null) } // 削除対象のアイテム

    // TODO: リスト名を表示するために、別途ShoppingListDaoからリスト名を取得するロジックが必要になる
    // 今回は簡易的に「リストID: $listId」をタイトルにする
//    val listName = "リストID: $listId" // 仮のリスト名

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
            TopAppBar(
                title = { Text(shoppingListName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // 戻るボタン
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("shopping_lists_route") }) { // ★追加★
                        Icon(Icons.AutoMirrored.Filled.List, "リスト管理") // リスト管理画面へのアイコン
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddItemDialog = true }) {
                Icon(Icons.Filled.Add, "新しいアイテムを追加")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (shoppingItems.isEmpty()) {
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
                        onTogglePurchased = { viewModel.toggleItemPurchased(it) },
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
//                            viewModel.deleteShoppingItem(itemToDelete)
//                        }
                    )
                }
            }
        }
    }

    // アイテム追加ダイアログ
    if (showAddItemDialog) {
        AddItemDialog(
            onAddItem = { itemName ->
                viewModel.addShoppingItem(itemName)
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
                viewModel.updateShoppingItem(updatedItem)
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
                viewModel.deleteShoppingItem(itemToDelete!!) // 削除実行
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
                onClick = { onTogglePurchased(shoppingItem) }, // タップでチェック切り替え
                onLongClick = { onEditClick(shoppingItem) } // 長押しで編集（簡易）
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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