package com.tsubushiro.kaumemo.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tsubushiro.kaumemo.data.ShoppingList


@OptIn(ExperimentalMaterial3Api::class)
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
    viewModel: ShoppingListsViewModel = hiltViewModel() // ここを変更
) {
    val shoppingLists by viewModel.shoppingLists.collectAsState() // ViewModelからリストの状態を収集
    var showAddListDialog by remember { mutableStateOf(false) } // リスト追加ダイアログの表示状態
    var showEditListDialog by remember { mutableStateOf(false) } // リスト編集ダイアログの表示状態
    var editingList by remember { mutableStateOf<ShoppingList?>(null) } // 編集中のリストを保持

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("買い物リスト") })
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                items(shoppingLists) { shoppingList ->
                    ShoppingListCard(
                        shoppingList = shoppingList,
                        onListClick = {
                            // リストをタップしたら、そのリストのアイテム画面へ遷移
                            navController.navigate("shopping_items_route/${shoppingList.id}")
                        },
                        // ★ ここから編集・削除のコールバック ★
                        onEditClick = { listToEdit -> // 編集ボタンが押されたら
                            editingList = listToEdit      // 編集対象のリストをセット
                            showEditListDialog = true     // 編集ダイアログを表示
                        },
                        onDeleteClick = { listToDelete -> // 削除ボタンが押されたら
                            viewModel.deleteShoppingList(listToDelete) // ViewModelの削除メソッドを呼び出し
                        }
                    )
                }
            }


        }
    }

    // リスト追加ダイアログ
    if (showAddListDialog) {
        AddListDialog(
            onAddList = { listName ->
                viewModel.addShoppingList(listName)
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
                viewModel.updateShoppingList(updatedList) // ViewModelの更新メソッドを呼び出し
                showEditListDialog = false // ダイアログを閉じる
                editingList = null         // 編集対象をクリア
            },
            onDismiss = { // キャンセルまたはダイアログ外タップで閉じる
                showEditListDialog = false
                editingList = null
            }
        )
    }
}

// 個々の買い物リスト表示用Composable
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingListCard(
    shoppingList: ShoppingList,
    onListClick: (ShoppingList) -> Unit, // リストがクリックされた時のコールバック
    onEditClick: (ShoppingList) -> Unit, // 追加: 編集ボタンクリック時のコールバック
    onDeleteClick: (ShoppingList) -> Unit // 追加: 削除ボタンクリック時のコールバック

) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable { onListClick(shoppingList) }, // クリック可能にする
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = shoppingList.name,
//                style = MaterialTheme.typography.headlineSmall
//            )
//            // ここに、リスト内のアイテム数など表示を後で追加可能
//        }
//    }
    var showOptionsDialog by remember { mutableStateOf(false) } // ダイアログ表示状態

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable( // ★ 長押しとタップの両方を処理するため combinedClickable を使用 ★
                onClick = { onListClick(shoppingList) },
                onLongClick = { showOptionsDialog = true } // 長押しでオプションダイアログを表示
            ),
//            .clickable { onListClick(shoppingList) } // 通常のタップはアイテム画面へ遷移
//            .longClickable { showOptionsDialog = true }, // 長押しでオプションダイアログ表示
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = shoppingList.name,
                style = MaterialTheme.typography.headlineSmall
            )
            // TODO: 後でリスト内の未購入アイテム数などを表示可能
        }
    }
    if (showOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showOptionsDialog = false },
            title = { Text(shoppingList.name) },
            text = { Text("リストをどうしますか？") },
            confirmButton = {
                Column {
                    Button(
                        onClick = {
                            onEditClick(shoppingList) // 編集コールバックを呼び出す
                            showOptionsDialog = false // ダイアログを閉じる
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("編集")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onDeleteClick(shoppingList) // 削除コールバックを呼び出す
                            showOptionsDialog = false // ダイアログを閉じる
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), // 削除ボタンは赤色に
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("削除")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // 削除ボタンとキャンセルが重なってしまうので、
                    // 「キャンセル」ボタンも Column の中に入れて、すべてのボタンを縦並びにする
                    // TextButtonからButtonコンポーザブルに変更
                    Button( // TextButtonではなくButtonを使用
                        onClick = { showOptionsDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), // 少し薄い色に
                            contentColor = MaterialTheme.colorScheme.onSurface // テキスト色
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("キャンセル")
                    }
                }
            },
            dismissButton = {
//                TextButton(onClick = { showOptionsDialog = false }) {
//                    Text("キャンセル")
//                }
            }
        )
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