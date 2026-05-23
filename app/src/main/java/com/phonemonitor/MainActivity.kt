package com.phonemonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.phonemonitor.ui.DrawerScaffold
import com.phonemonitor.ui.MainScreen
import com.phonemonitor.ui.theme.NotiMonTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      NotiMonTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          var showAbout by remember { mutableStateOf(false) }
          DrawerScaffold(
            onMenuToggle = { },
            showAbout = showAbout,
            onShowAboutChange = { showAbout = it }
          ) {
            MainScreen(vm = viewModel())
          }
        }
      }
    }
  }
}
