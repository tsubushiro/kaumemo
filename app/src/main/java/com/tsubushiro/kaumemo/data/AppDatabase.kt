package com.tsubushiro.kaumemo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ShoppingList::class, ShoppingItem::class], // エンティティを登録
    version = 2, // データベースバージョン ToDo: 本番1にもどす
    exportSchema = false // スキーマをファイルにエクスポートしない (今回は不要)
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    companion object {
        @Volatile // メインメモリから読み書きを保証
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaumemo_db" // データベースファイル名
                )
                    // 開発中にエンティティを変更した際にDBを再構築する（本番アプリでは非推奨）
                    // ToDo: 本番 fallbackToDestructiveMigration()は消す
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
