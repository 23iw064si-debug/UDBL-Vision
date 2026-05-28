package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModel
import com.example.ui.AuthScreen
import com.example.ui.MainContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()
            val selectedTheme by viewModel.selectedTheme.collectAsState()

            MyApplicationTheme(selectedTheme = selectedTheme) {
                var showLogoSplash by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

                if (showLogoSplash) {
                    com.example.ui.LogoSplashScreen(onFinished = { showLogoSplash = false })
                } else {
                    if (isLoggedIn) {
                        MainContainer(
                            viewModel = viewModel,
                            onLogout = {
                                // Can do extra clear session routines if needed
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AuthScreen(
                            viewModel = viewModel,
                            onAuthSuccess = {
                                // Profile loads, navigation switches automatically
                            }
                        )
                    }
                }
            }
        }
    }
}

