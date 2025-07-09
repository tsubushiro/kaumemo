package com.tsubushiro.kaumemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.compose.KaumemoTheme
import com.google.android.gms.ads.MobileAds
import com.tsubushiro.kaumemo.common.AppContextProvider
import com.tsubushiro.kaumemo.ui.AppNavHost
import com.tsubushiro.kaumemo.ui.ShoppingViewModel
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint // ここにHiltのエントリーポイントアノテーションを追加
class MainActivity : ComponentActivity() {
    // ★ ここにViewModelをActivityスコープで取得する行を追加 ★
//    private val shoppingItemsViewModel: ShoppingItemsViewModel by viewModels()
//    private val shoppingListsViewModel: ShoppingListsViewModel by viewModels()
    private val shoppingViewModel: ShoppingViewModel by viewModels() // 両方のViewModelを合成したViewModel

    @Inject // ★AppContextProviderを注入★
    lateinit var appContextProvider: AppContextProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("PerfLog", "MainActivity Splash Screen Start: ${System.currentTimeMillis()}")
        // スプラッシュスクリーンをインストール
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // ★★★ ここに「アプリの準備を開始します！」トーストのロジックを追加 ★★★
        if (!appContextProvider.isFirstLaunchCompleted) {
            Toast.makeText(
                applicationContext, // ContextはapplicationContextを使用
                "アプリの準備を開始します！", // トーストのテキスト
                Toast.LENGTH_SHORT // 短い表示時間でOK
            ).show()
        }

        // ViewModelのisLoadingがtrueの間、スプラッシュスクリーンを表示し続ける
        splashScreen.setKeepOnScreenCondition {
            shoppingViewModel.isLoading.value
        }

        Log.d("PerfLog", "MainActivity Splash Screen End: ${System.currentTimeMillis()}")
        Log.d("PerfLog", "MainActivity onCreate Start: ${System.currentTimeMillis()}")
        setContent {
            KaumemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ★ ここでMobile Ads SDKを初期化 ★
                    MobileAds.initialize(this) {}
                    AppNavHost(
//                        shoppingItemsViewModel = shoppingItemsViewModel,
//                        shoppingListsViewModel = shoppingListsViewModel
                        shoppingItemsViewModel = shoppingViewModel,
                        shoppingListsViewModel = shoppingViewModel
                    )
                }
            }
        }
        Log.d("PerfLog", "MainActivity onCreate End: ${System.currentTimeMillis()}")
    }
}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hi $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    KaumemoTheme {
//        Greeting("Android")
//    }
//}