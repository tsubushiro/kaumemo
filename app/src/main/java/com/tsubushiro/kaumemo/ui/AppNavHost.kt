package com.tsubushiro.kaumemo.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tsubushiro.kaumemo.common.AppContextProviderEntryPoint
import dagger.hilt.android.EntryPointAccessors

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController() // デフォルトで新しいNavControllerを作成
    ,
    // ★ ViewModelを引数として受け取るように変更 ★
//    shoppingItemsViewModel: ShoppingItemsViewModel,
//    shoppingListsViewModel: ShoppingListsViewModel
    shoppingItemsViewModel: ShoppingViewModel,
    shoppingListsViewModel: ShoppingViewModel
) {
//    Log.d("AppNavHost","よんだ？")
//    val shoppingItemsViewModel: ShoppingItemsViewModel = viewModel() // Hiltの場合は hiltViewModel()
    //   val initialListId by shoppingItemsViewModel.currentListId.collectAsStateWithLifecycle() // ViewModelからIDを収集
//    val initialListIdState = shoppingItemsViewModel.currentListId.collectAsStateWithLifecycle()
//    val initialListId = initialListIdState.value // Stateオブジェクトから値を取得
//    Log.d("AppNavHost",initialListId.toString())

    // initialListId が確定するまでNavHostを構築しないか、初期値を考慮
//    if (initialListId != null) {
        Log.d("PerfLog", "AppNavHost NavHost construction Start: ${System.currentTimeMillis()}")
        //        NavHost(navController = navController, startDestination = "shopping_items_route/{${initialListId}}") { // ダミーのスタートデスティネーション
        NavHost(navController = navController, startDestination = "initialScreen") { // ダミーのスタートデスティネーション
            composable(
                "shopping_items_route/{listId}",
                arguments = listOf(navArgument("listId") { type = NavType.IntType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getInt("listId")
                // ここで listId をViewModelに渡す必要は、hiltViewModel() が SavedStateHandle を通じて自動で行うため不要
//                ShoppingItemsScreen(navController = navController, listId = listId) // ViewModelを共有
                ShoppingItemsScreen(navController = navController, listId = listId,
                    shoppingItemsViewModel = shoppingItemsViewModel
                )
            }
            composable("shopping_lists_route") {
                ShoppingListsScreen(navController = navController ,
                    shoppingListsViewModel= shoppingListsViewModel
                )
            }
            // 初期画面
            composable("initialScreen") { backStackEntry ->
                // ApplicationContextを取得
                val context = LocalContext.current.applicationContext

                // EntryPointからAppContextProviderを取得
                val appContextProvider = EntryPointAccessors.fromApplication(
                    context,
                    AppContextProviderEntryPoint::class.java
                ).appContextProvider()

                // sharedpreferences に保存した現在表示中のリストのIDを取得
                val listId = appContextProvider.currentListId
                ShoppingItemsScreen(
                    navController = navController,
                    listId = listId,
                    shoppingItemsViewModel = shoppingItemsViewModel
                )
            }


            // ... 他のルート
        }
        Log.d("PerfLog", "AppNavHost NavHost construction End: ${System.currentTimeMillis()}")
//    } else {
//        // ローディング表示など
//        CircularProgressIndicator()
//    }

//    // 初回起動時のナビゲーションロジック (NavHostの外)
//    LaunchedEffect(initialListId) {
//        if (initialListId != null && navController.currentDestination?.route != "shopping_items_route/{listId}") {
//            // 同じルートに何度もnavigateしないようにチェック
//            navController.navigate("shopping_items_route/$initialListId") {
//                // アプリ起動時に常にこの画面になるようにバックスタックをクリア
//                popUpTo(navController.graph.startDestinationId) {
//                    inclusive = true
//                }
//            }
//        }
//    }
//    NavHost(navController = navController, startDestination = "shopping_items_route/{listId}") {
//        // 買い物リスト一覧画面
//        composable("shopping_lists_route") {
//            ShoppingListsScreen(navController = navController)
//        }
//
//        // 個別買い物アイテム詳細画面
//        composable(
//            "shopping_items_route/{listId}", // listIdを引数として受け取る
//            arguments = listOf(navArgument("listId") { type = NavType.IntType })
//        ) { backStackEntry ->
//            val listId = backStackEntry.arguments?.getInt("listId")
//            if (listId != null) {
//                ShoppingItemsScreen(navController = navController, listId = listId)
//            } else {
//                // listIdがnullの場合のエラーハンドリング（例えば、エラー画面に遷移させるなど）
//                // 現状はLogcatで警告を出す程度で良い
//            }
//        }
//        // ★ その他の画面ルートがあればここに追加 ★
//        // 例: composable("settings_route") { SettingsScreen(...) }
//    }
}