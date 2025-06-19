package com.tsubushiro.kaumemo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tsubushiro.kaumemo.data.AppDatabase
import com.tsubushiro.kaumemo.data.ShoppingList
import com.tsubushiro.kaumemo.data.ShoppingRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    navController: NavController, // 画面遷移のためのNavController
    // ViewModelを引数で受け取るためのFactory
    viewModel: ShoppingListsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(navController.context) // コンテキストはNavControllerから取得
                val repository = ShoppingRepository(db.shoppingListDao(), db.shoppingItemDao())
                @Suppress("UNCHECKED_CAST")
                return ShoppingListsViewModel(repository) as T
            }
        }
    )
) {
    val shoppingLists by viewModel.shoppingLists.collectAsState() // ViewModelからリストの状態を収集
    var showAddListDialog by remember { mutableStateOf(false) } // リスト追加ダイアログの表示状態

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("買い物リスト") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddListDialog = true }) {
                Icon(Icons.Filled.Add, "新しいリストを追加")
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
}

// 個々の買い物リスト表示用Composable
@Composable
fun ShoppingListCard(
    shoppingList: ShoppingList,
    onListClick: (ShoppingList) -> Unit // リストがクリックされた時のコールバック
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onListClick(shoppingList) }, // クリック可能にする
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = shoppingList.name,
                style = MaterialTheme.typography.headlineSmall
            )
            // ここに、リスト内のアイテム数など表示を後で追加可能
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