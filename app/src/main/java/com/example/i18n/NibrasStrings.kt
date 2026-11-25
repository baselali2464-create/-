package com.example.i18n

import androidx.compose.runtime.Composable
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.LocalAppLanguage

@Composable
fun bi(ar: String, en: String): String {
    return if (LocalAppLanguage.current == AppLanguage.AR) ar else en
}

fun biStatic(language: AppLanguage, ar: String, en: String): String {
    return if (language == AppLanguage.AR) ar else en
}
