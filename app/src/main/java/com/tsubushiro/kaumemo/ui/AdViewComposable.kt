package com.tsubushiro.kaumemo.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdViewComposable(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(), // 横幅いっぱいにする
        factory = { context ->
            AdView(context).apply {
                // テスト用の広告ユニットID
                // 本番環境ではご自身の広告ユニットIDに置き換えてください
                setAdUnitId("ca-app-pub-3940256099942544/6300978111")
                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                setAdSize(AdSize.BANNER) // BANNERサイズを指定

                // AdListenerを設定し、広告のロード状態をログに出力
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d("AdViewComposable", "広告が正常にロードされました。")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e("AdViewComposable", "広告のロードに失敗しました: ${adError.message}")
                    }

                    override fun onAdOpened() {
                        Log.d("AdViewComposable", "広告が開かれました。")
                    }

                    override fun onAdClicked() {
                        Log.d("AdViewComposable", "広告がクリックされました。")
                    }

                    override fun onAdClosed() {
                        Log.d("AdViewComposable", "広告が閉じられました。")
                    }
                }

                // 広告をロード
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}