package com.tsubushiro.kaumemo.common

import android.content.Context
import android.content.SharedPreferences
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
            return prefs.getInt("current_list_id", 0)
        }
        set(value) {
            prefs.edit() { putInt("current_list_id", value) }
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