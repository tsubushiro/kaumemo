package com.tsubushiro.kaumemo.di

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.room.Room
import com.tsubushiro.kaumemo.common.AppContextProvider
import com.tsubushiro.kaumemo.data.AppDatabase
import com.tsubushiro.kaumemo.data.ShoppingItemDao
import com.tsubushiro.kaumemo.data.ShoppingListDao
import com.tsubushiro.kaumemo.data.ShoppingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // アプリケーションスコープにインストール
object AppModule {

    @Singleton // アプリケーション全体で単一のインスタンスを保証
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        appContextProvider: AppContextProvider
    ): AppDatabase {
        var db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "kaumemo_db"
        )
            .createFromAsset("databases/initial.db") // 初回起動時にのみ使用する
            .build()

        // 初回起動時にのみToastを表示
        var isFirstLaunch = !appContextProvider.isFirstLaunchCompleted

        if(isFirstLaunch){
            // UIスレッドでトーストを表示するためにHandlerを使用
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(
                    context.applicationContext, // ApplicationContextを使用
                    "アプリの準備ができました！", // トーストのテキスト候補
                    Toast.LENGTH_LONG
                ).show()
            }
            appContextProvider.isFirstLaunchCompleted = true
        }

        return db
    }

    @Provides
    fun provideShoppingListDao(database: AppDatabase): ShoppingListDao {
        return database.shoppingListDao()
    }

    @Provides
    fun provideShoppingItemDao(database: AppDatabase): ShoppingItemDao {
        return database.shoppingItemDao()
    }

    @Singleton // リポジトリも単一のインスタンスを保証
    @Provides
    fun provideShoppingRepository(
        shoppingListDao: ShoppingListDao,
        shoppingItemDao: ShoppingItemDao
    ): ShoppingRepository {
        return ShoppingRepository(shoppingListDao, shoppingItemDao)
    }

    @Provides
    @Singleton
    fun provideAppContextProvider(
        @ApplicationContext context: Context
    ): AppContextProvider = AppContextProvider(context)
}