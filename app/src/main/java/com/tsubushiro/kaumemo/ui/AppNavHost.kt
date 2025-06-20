package com.tsubushiro.kaumemo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController() // デフォルトで新しいNavControllerを作成
) {
    NavHost(navController = navController, startDestination = "shopping_lists_route") {
        // 買い物リスト一覧画面
        composable("shopping_lists_route") {
            ShoppingListsScreen(navController = navController)
        }

        // 個別買い物アイテム詳細画面
        composable(
            "shopping_items_route/{listId}", // listIdを引数として受け取る
            arguments = listOf(navArgument("listId") { type = NavType.IntType })
        ) { backStackEntry ->
            val listId = backStackEntry.arguments?.getInt("listId")
            if (listId != null) {
                ShoppingItemsScreen(navController = navController, listId = listId)
            } else {
                // listIdがnullの場合のエラーハンドリング（例えば、エラー画面に遷移させるなど）
                // 現状はLogcatで警告を出す程度で良い
            }
        }
        // ★ その他の画面ルートがあればここに追加 ★
        // 例: composable("settings_route") { SettingsScreen(...) }
    }
}