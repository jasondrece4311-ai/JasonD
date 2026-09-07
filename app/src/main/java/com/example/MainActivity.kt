package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.PriceTagScreen
import com.example.ui.PriceTagViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PriceTagTheme

class MainActivity : ComponentActivity() {
  private val viewModel: PriceTagViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      PriceTagTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = DarkBackground
        ) {
          PriceTagScreen(viewModel = viewModel)
        }
      }
    }
  }
}

