plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp) // 追加
    id("com.google.dagger.hilt.android") // Hilt Gradle plugin
}

android {
    namespace = "com.tsubushiro.kaumemo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tsubushiro.kaumemo" // Todo:リリース時
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true // 参考書記載の高速化
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // 追加したパッケージ
    // 「Project Structure」→[Dependencies」「app」で[Declared Dependencies」のプラスボタンより「Library Dependency」を選択
    //  直接書くよりもメニューから選択したほうが正式
    // https://codeforfun.jp/android-room-and-ksp-settings/
    implementation(libs.androidx.room.runtime)
    implementation(libs.play.services.ads)
    ksp(libs.androidx.room.compiler) //  ここだけimplementation をkspに変更
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.navigation.compose)
    //
    implementation("com.google.dagger:hilt-android:2.56.2") // 最新のHiltバージョンに合わせる
    ksp("com.google.dagger:hilt-compiler:2.56.2") // kapt または ksp を使用
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6") // ドラッグアンドドロップ
    implementation(libs.androidx.core.splashscreen) // スプラッシュスクリーン
    //
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}