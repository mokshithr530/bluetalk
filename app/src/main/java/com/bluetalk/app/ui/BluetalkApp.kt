package com.bluetalk.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bluetalk.app.ui.home.HomeRoute
import com.bluetalk.app.ui.home.HomeViewModel
import com.bluetalk.app.ui.navigation.BluetalkDestination

@Composable
fun BluetalkApp(
    homeViewModel: HomeViewModel,
    onRequestBluetoothPermissions: () -> Unit,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BluetalkDestination.Home.route,
    ) {
        composable(BluetalkDestination.Home.route) {
            HomeRoute(
                viewModel = homeViewModel,
                onRequestBluetoothPermissions = onRequestBluetoothPermissions,
            )
        }
    }
}
