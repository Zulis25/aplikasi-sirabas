package com.stb.sirabas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stb.sirabas.ui.SirabasApp
import com.stb.sirabas.ui.theme.SIRABASTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SIRABASTheme {
                SirabasApp()
            }
        }
    }
}