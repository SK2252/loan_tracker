package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.domain.model.LoanWithDetails
import com.example.ui.components.LendFlowBottomNavigation
import com.example.ui.components.NavDestination
import com.example.ui.screens.CollectionScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FriendDetailsScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.NewLoanScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.viewmodel.LendFlowViewModel

sealed class Screen {
    data object Dashboard : Screen()
    data object Friends : Screen()
    data class FriendDetails(val friendId: Long) : Screen()
    data class NewLoan(val prefilledFriendName: String = "") : Screen()
    data object Collection : Screen()
    data object Reports : Screen()
}

@Composable
fun MainAppNavigation(
    viewModel: LendFlowViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    val currentTab = when (currentScreen) {
        is Screen.Dashboard -> NavDestination.HOME
        is Screen.Friends, is Screen.FriendDetails -> NavDestination.FRIENDS
        is Screen.NewLoan -> NavDestination.NEW_LOAN
        is Screen.Collection -> NavDestination.COLLECTION
        is Screen.Reports -> NavDestination.REPORTS
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            LendFlowBottomNavigation(
                currentDestination = currentTab,
                onNavigate = { destination ->
                    currentScreen = when (destination) {
                        NavDestination.HOME -> Screen.Dashboard
                        NavDestination.FRIENDS -> Screen.Friends
                        NavDestination.NEW_LOAN -> Screen.NewLoan()
                        NavDestination.COLLECTION -> Screen.Collection
                        NavDestination.REPORTS -> Screen.Reports
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    is Screen.Dashboard -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToNewLoan = { currentScreen = Screen.NewLoan() },
                            onNavigateToCalendar = { currentScreen = Screen.Collection },
                            onRecordPayment = { /* Partial modal handled inside CollectionScreen / Dashboard */ }
                        )
                    }

                    is Screen.Friends -> {
                        FriendsScreen(
                            viewModel = viewModel,
                            onSelectFriend = { friendId ->
                                viewModel.selectFriend(friendId)
                                currentScreen = Screen.FriendDetails(friendId)
                            },
                            onNavigateToNewLoan = { currentScreen = Screen.NewLoan() }
                        )
                    }

                    is Screen.FriendDetails -> {
                        FriendDetailsScreen(
                            viewModel = viewModel,
                            onBack = { currentScreen = Screen.Friends },
                            onAddLoanForFriend = { friendName ->
                                currentScreen = Screen.NewLoan(friendName)
                            },
                            onRecordPayment = { /* Recorded via screen */ }
                        )
                    }

                    is Screen.NewLoan -> {
                        NewLoanScreen(
                            viewModel = viewModel,
                            initialFriendName = targetScreen.prefilledFriendName,
                            onLoanCreated = { currentScreen = Screen.Dashboard }
                        )
                    }

                    is Screen.Collection -> {
                        CollectionScreen(viewModel = viewModel)
                    }

                    is Screen.Reports -> {
                        ReportsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
