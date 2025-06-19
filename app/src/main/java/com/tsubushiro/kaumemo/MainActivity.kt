package com.tsubushiro.kaumemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.tsubushiro.kaumemo.data.AppDatabase
import com.tsubushiro.kaumemo.data.ShoppingItem
import com.tsubushiro.kaumemo.data.ShoppingList
import com.tsubushiro.kaumemo.data.ShoppingRepository
import com.tsubushiro.kaumemo.ui.theme.KaumemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaumemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        // --- ここからDay4のDBテストコード ---
        val db = AppDatabase.getDatabase(this) // AppDatabaseのインスタンスを取得
        val shoppingListDao = db.shoppingListDao()
        val shoppingItemDao = db.shoppingItemDao()
        val repository = ShoppingRepository(shoppingListDao, shoppingItemDao) // Repositoryのインスタンスを作成

        // コルーチンを使って非同期でDB操作を実行
        lifecycleScope.launch(Dispatchers.IO) { // UIスレッドをブロックしないようIOディスパッチャを使用
            // 1. 新しい買い物リストを追加
            val newList = ShoppingList(name = "今週の食料品")
            repository.insertShoppingList(newList)
            Log.d("DB_TEST", "リストを追加: ${newList.name}")

            // 追加されたリストのIDを取得（念のため、Flowをcollectするか、別途取得クエリを使うのがより正確だが、今回は簡易テスト）
            // 簡易テストのため、再度リストを取得してIDを確認
            val lists = shoppingListDao.getAllShoppingLists().first() // Flowから最初の値を取得
            val addedList = lists.find { it.name == "今週の食料品" }

            addedList?.let { list ->
                Log.d("DB_TEST", "追加されたリストのID: ${list.id}")

                // 2. そのリストにアイテムを追加
                val item1 = ShoppingItem(listId = list.id, name = "牛乳")
                val item2 = ShoppingItem(listId = list.id, name = "パン", isPurchased = true)
                repository.insertShoppingItem(item1)
                repository.insertShoppingItem(item2)
                Log.d("DB_TEST", "アイテムを追加: ${item1.name}, ${item2.name}")

                // 3. 追加したアイテムを再度取得して確認
                val itemsInList = shoppingItemDao.getShoppingItemsForList(list.id).first()
                Log.d("DB_TEST", "${list.name} のアイテム:")
                itemsInList.forEach { item ->
                    Log.d("DB_TEST", "- ID: ${item.id}, 名前: ${item.name}, 購入済み: ${item.isPurchased}, リストID: ${item.listId}")
                }

                // 4. アイテムの更新テスト
                val updatedItem1 = item1.copy(isPurchased = true)
                repository.updateShoppingItem(updatedItem1)
                Log.d("DB_TEST", "${updatedItem1.name} を購入済みに更新")

                val itemsAfterUpdate = shoppingItemDao.getShoppingItemsForList(list.id).first()
                Log.d("DB_TEST", "更新後のアイテム:")
                itemsAfterUpdate.forEach { item ->
                    Log.d("DB_TEST", "- ID: ${item.id}, 名前: ${item.name}, 購入済み: ${item.isPurchased}, リストID: ${item.listId}")
                }

                // 5. アイテムの削除テスト (例: パンを削除)
                val itemToDelete = itemsInList.find { it.name == "パン" }
                itemToDelete?.let {
                    repository.deleteShoppingItem(it)
                    Log.d("DB_TEST", "${it.name} を削除")
                }

                val itemsAfterDelete = shoppingItemDao.getShoppingItemsForList(list.id).first()
                Log.d("DB_TEST", "削除後のアイテム:")
                itemsAfterDelete.forEach { item ->
                    Log.d("DB_TEST", "- ID: ${item.id}, 名前: ${item.name}, 購入済み: ${item.isPurchased}, リストID: ${item.listId}")
                }
            } ?: Log.e("DB_TEST", "リストが見つかりませんでした")
        }
        // --- Day4のDBテストコードここまで ---
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hi $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KaumemoTheme {
        Greeting("Android")
    }
}