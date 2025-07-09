package com.tsubushiro.kaumemo.di

import android.content.Context
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "kaumemo_db"
        )
            .createFromAsset("databases/initial.db") // 初回起動時にのみ使用する
            .build()
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