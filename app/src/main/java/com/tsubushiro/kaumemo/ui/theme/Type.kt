package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // 例: 16.sp から 14.sp に変更
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // 他のテキストスタイルも同様に調整
    titleMedium = TextStyle( // ShoppingListsScreenのリスト名
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp, // 例: 20.sp から 18.sp に変更
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodySmall = TextStyle( // ShoppingListsScreenのアイテム数
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp, // 例: 12.sp から 10.sp に変更
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)
