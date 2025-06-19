package com.tsubushiro.kaumemo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KaumemoApplication : Application() {
    // アプリケーションレベルで初期化が必要なものがあればここに書く
    // 例: ThreeTenABP.init(this) など
}