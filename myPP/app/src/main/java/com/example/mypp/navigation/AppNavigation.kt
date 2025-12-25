package com.example.mypp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mypp.screens.*
import com.example.mypp.viewmodels.UserAuthViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object ChooseRole : Screen("choose_role_screen")
    object Login : Screen("login_screen")
    object SignUp : Screen("sign_up_screen")
    object Home : Screen("home_screen")
    object RouteMap : Screen("route_map_screen/{routeId}") {
        fun createRoute(routeId: String) = "route_map_screen/$routeId"
    }
    object RouteDetails : Screen("route_details_screen/{routeId}") {
        fun createRoute(routeId: String) = "route_details_screen/$routeId"
    }
    object Payment : Screen("payment_screen/{routeId}/{amount}") {
        fun createRoute(routeId: String, amount: String) = "payment_screen/$routeId/$amount"
    }
    object ChatBot : Screen("chatbot_screen")
    object ConductorJourney : Screen("conductor_journey_screen")
}

/**
 * Helper function to share ViewModels between screens
 */
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return viewModel(viewModelStoreOwner = parentEntry)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val userAuthViewModel: UserAuthViewModel = viewModel()
    
    // Collect auth state to determine if user is logged in
    val isLoggedIn by userAuthViewModel.isLoggedIn.collectAsState()
    val currentUser by userAuthViewModel.currentUser.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onGetStartedClicked = {
                    navController.navigate(Screen.ChooseRole.route)
                }
            )
        }

        composable(Screen.ChooseRole.route) {
            ChooseRoleScreen(
                onContinueClicked = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate based on the selected role
                    val selectedRole = userAuthViewModel.selectedRole.value
                    if (selectedRole == "Conductor") {
                        navController.navigate(Screen.ConductorJourney.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                },
                onSignUpClicked = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    // Navigate based on the selected role
                    val selectedRole = userAuthViewModel.selectedRole.value
                    if (selectedRole == "Conductor") {
                        navController.navigate(Screen.ConductorJourney.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                },
                onLoginClicked = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Home.route) {
            // Get the current user's name from auth view model
            val userName = userAuthViewModel.currentUser.collectAsState().value?.name ?: "User"
            
            HomeScreen(
                name = userName, 
                onFindBusClicked = { source, destination ->
                    // In a real app, you would lookup the route ID based on source and destination
                    // For now, we'll use a hardcoded route ID for demonstration
                    val routeId = "route1"
                    navController.navigate(Screen.RouteMap.createRoute(routeId))
                },
                onChatbotClicked = {
                    navController.navigate(Screen.ChatBot.route)
                }
            )
        }
        
        composable(
            route = Screen.RouteMap.route
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")
            RouteMapScreen(
                routeId = routeId,
                onBackClicked = {
                    navController.navigateUp()
                },
                onBookTicketClicked = { routeId, amount ->
                    navController.navigate(Screen.Payment.createRoute(routeId, amount))
                }
            )
        }
        
        composable(
            route = Screen.RouteDetails.route
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")
            // Implementation for route details screen will be added later
            // For now, we can reuse the map screen
            if (routeId != null) {
                RouteMapScreen(
                    routeId = routeId,
                    onBackClicked = {
                        navController.navigateUp()
                    }
                )
            }
        }
        
        composable(
            route = Screen.Payment.route
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId")
            val amount = backStackEntry.arguments?.getString("amount")
            
            PaymentScreen(
                routeId = routeId,
                amount = amount,
                onPaymentComplete = {
                    // After payment, navigate to home screen
                    navController.navigate(Screen.Home.route) {
                        // Clear back stack up to Home to prevent going back to payment screen
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBackPressed = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(Screen.ChatBot.route) {
            ChatBotScreen(
                onBackPressed = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(Screen.ConductorJourney.route) {
            ConductorJourneyScreen(
                onLogoutClicked = {
                    userAuthViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}