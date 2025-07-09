package com.tsubushiro.kaumemo.common

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.tsubushiro.kaumemo.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// アプリの情報やコンテキストの情報
@Singleton
class AppContextProvider(private val context: Context) {
//class AppContextProvider @Inject constructor(
//    @ApplicationContext private val context: Context
// ){
    private fun getString(resId: Int): String = context.getString(resId)
    fun getAppName():String = getString(R.string.app_name)

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    // 買い物リスト
    var currentListId: Int
        get() {
            Log.d("PerfLog", "AppContextProvider currentListId get: ${System.currentTimeMillis()}")
            return prefs.getInt("current_list_id", 0)
        }
        set(value) {
            Log.d("PerfLog", "AppContextProvider currentListId set: ${System.currentTimeMillis()}")
            prefs.edit() { putInt("current_list_id", value) }
        }
    // 初回起動完了
    var isFirstLaunchCompleted: Boolean
        get() {
            Log.d("PerfLog", "AppContextProvider isFirstLaunchCompleted get: ${System.currentTimeMillis()}")
            return prefs.getBoolean("is_first_launch_completed", false) // デフォルト値はfalse
        }
        set(value) {
            Log.d("PerfLog", "AppContextProvider isFirstLaunchCompleted set: ${System.currentTimeMillis()}")
            prefs.edit() { putBoolean("is_first_launch_completed", value) }
        }
}

// EntryPointインターフェースの作成
// Hiltは@Composable関数で直接@Injectインスタンスを受け取れないため、
// EntryPointAccessorを使ってApplicationContextから取得できます。
// AppNavHostでAppContentProvider使う準備
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppContextProviderEntryPoint {
    fun appContextProvider(): AppContextProvider
}