package com.bluetalk.app.ui.navigation

sealed class BluetalkDestination(val route: String) {
    data object Home : BluetalkDestination("home")
    data object Discovery : BluetalkDestination("discovery")
    data object Session : BluetalkDestination("session")
    data object Chat : BluetalkDestination("chat")
}
