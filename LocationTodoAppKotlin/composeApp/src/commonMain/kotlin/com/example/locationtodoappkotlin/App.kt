package com.example.locationtodoappkotlin

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.locationtodoappkotlin.navigation.AppNavigation

@Composable
fun App() {
    MaterialTheme {
        AppNavigation()
    }
}
