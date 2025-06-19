package com.tsubushiro.kaumemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tsubushiro.kaumemo.ui.ShoppingListsScreen
import com.tsubushiro.kaumemo.ui.theme.KaumemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // ここにHiltのエントリーポイントアノテーションを追加
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KaumemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ナビゲーションコントローラーを作成
                    val navController = rememberNavController()

                    // ナビゲーションホストを定義 (Day 8のAppNavHostの簡易版)
                    NavHost(navController = navController, startDestination = "shopping_lists_route") {
                        composable("shopping_lists_route") {
                            ShoppingListsScreen(navController = navController)
                        }
                        // Day 7で実装する個別リスト詳細画面のプレースホルダー
                        composable(
                            "shopping_items_route/{listId}",
                            arguments = listOf(navArgument("listId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val listId = backStackEntry.arguments?.getInt("listId")
                            // TODO: Day 7でShoppingItemsScreenを呼び出す
                            Text(text = "アイテム表示画面 (リストID: $listId)", modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
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