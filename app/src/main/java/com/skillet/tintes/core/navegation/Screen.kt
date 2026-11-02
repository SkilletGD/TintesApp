package com.skillet.tintes.core.navegation

sealed class Screen(val route: String) {
    data object Scanner : Screen("scanner_screen")
    data object Resurtido : Screen("resurtido_screen")
}