package com.example.dobroapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dobroapp.presentation.navigation.DobroAppRoot
import com.example.dobroapp.ui.theme.DobroAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DobroAppTheme {
                DobroAppRoot()
            }
        }
    }
}