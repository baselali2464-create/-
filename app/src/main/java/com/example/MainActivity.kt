package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.core.configureAppCheck
import com.example.ui.NibrasApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mandatory: configure Firebase App Check BEFORE setContent or any Firebase SDK calls
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            NibrasApp()
        }
    }
}
