package com.max04.carburancenis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.max04.carburancenis.data.AppContainer
import com.max04.carburancenis.ui.HomePage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppContainer.init(applicationContext)

        setContent {
            App()
        }
    }
}

@Composable
private fun App() {
    MaterialTheme {
        Surface {
            HomePage()
        }
    }
}