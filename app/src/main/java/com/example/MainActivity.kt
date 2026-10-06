package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavGraph
import com.example.ui.theme.DarkBg
import com.example.ui.theme.Explainer3DTheme
import com.example.ui.viewmodel.ExplainerViewModel

class MainActivity : ComponentActivity() {

    private val explainerViewModel: ExplainerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by explainerViewModel.userSettings.collectAsState()

            Explainer3DTheme(darkTheme = userSettings.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    AppNavGraph(viewModel = explainerViewModel)
                }
            }
        }
    }
}
