package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.screens.TrashAppUi
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TrashViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TrashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TrashAppUi(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
