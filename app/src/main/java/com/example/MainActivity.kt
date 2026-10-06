package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.AuraStoreApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuraStoreViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AuraStoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                AuraStoreApp(viewModel = viewModel)
            }
        }
    }
}

