package com.tsubushiro.kaumemo.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    // ViewModelはここで一度だけ取得する（Activityスコープ）
    // Hiltが適切にViewModelを管理してくれるはず
    val shoppingItemsViewModel: ShoppingItemsViewModel = hiltViewModel()

    val initialListIdState = shoppingItemsViewModel.currentListId.collectAsStateWithLifecycle()
    val initialListId = initialListIdState.value // Stateオブジェクトから値を取得
//    Log.d("AppNavHost",initialListId.toString())

    // LaunchedEffectを使って、initialListIdが確定したときに一度だけ初期画面に遷移
    LaunchedEffect(initialListId) {
        if (initialListId != null) {
            val route = "shopping_items_route/$initialListId"
            // 現在のルートが既に正しい場合は遷移しないようにする
            // ※これがないと、ナビゲーション引数が変わるたびに再遷移ループになる可能性あり
            if (navController.currentDestination?.route != "shopping_items_route/{listId}") {
                Log.d("PerfLog", "Navigating to initial route: $route at ${System.currentTimeMillis()}")
                navController.navigate(route) {
                    // アプリ起動時に常にこの画面になるようにバックスタックをクリア
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true // 同じルートが複数起動しないようにする
                }
            }
        }
    }

    // initialListId がまだ確定していない場合はローディング表示
    // NavHost の前にローディング画面を出す
    if (initialListId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        // NavHost は初期ルートを静的に定義する
        // 初回起動時の遷移はLaunchedEffectで行うため、startDestinationは「初期画面に到達するためのルート」を指定
        Log.d("PerfLog", "AppNavHost NavHost construction Start: ${System.currentTimeMillis()}")
        NavHost(navController = navController, startDestination = "shopping_lists_route") { // ★ startDestination を固定する ★

            composable("shopping_lists_route") {
                ShoppingListsScreen(navController = navController)
            }

            composable(
                "shopping_items_route/{listId}",
                arguments = listOf(navArgument("listId") { type = NavType.IntType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getInt("listId")
                // ShoppingItemsScreen には listId を渡すだけで、ViewModelはScreen内で取得させる
                // hiltViewModel() は SavedStateHandle を通じて自動で listId を ViewModel に渡すため、
                // ここで listId を直接 ViewModel に渡す必要はない（ViewModelのコンストラクタで @AssistedFactory などを使わない限り）
                ShoppingItemsScreen(navController = navController, listId = listId)
            }
            // 他のルートもここに追加
        }
        Log.d("PerfLog", "AppNavHost NavHost construction End: ${System.currentTimeMillis()}")
    }
}