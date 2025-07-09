package com.tsubushiro.kaumemo.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ShoppingList::class, ShoppingItem::class], // エンティティを登録
    version = 1, // データベースバージョン ToDo: 本番1にもどす
    exportSchema = false // スキーマをファイルにエクスポートしない (今回は不要)
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao

    // WALのチェックポイントを強制的に実行するメソッド
    // companion object の外に出し、インスタンスメソッドにする
    fun forceWalCheckpoint() {
        Log.d("AppDatabase", "forceWalCheckpoint() - Start getting writableDatabase.")
        try {
            // --- ★ここが最も重要です★ ---
            // AppDatabaseクラス（RoomDatabaseのサブクラス）のインスタンスメソッドとして、
            // 継承された getOpenHelper() を直接呼び出します。
            getOpenHelper().writableDatabase.use { db ->
                Log.d("AppDatabase", "forceWalCheckpoint() - Inside use block, executing PRAGMA.")
                db.execSQL("PRAGMA wal_checkpoint(FULL);")
                Log.d("AppDatabase", "WAL checkpoint (FULL) executed. Log from inside use block.")
            }
            Log.d("AppDatabase", "forceWalCheckpoint() - Successfully completed use block.")
        } catch (e: Exception) {
            Log.e("AppDatabase", "forceWalCheckpoint() - Error during database operation: ${e.message}", e)
        }
        Log.d("AppDatabase", "forceWalCheckpoint() - End of function.")
    }

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
                    // Todo: 本番 initial.dbは最新のモデルのものを使う
//                    .createFromAsset("databases/initial.db")
//                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
