package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

enum class NavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    FRIENDS("Friends", Icons.Filled.People, Icons.Outlined.People),
    NEW_LOAN("New Loan", Icons.Filled.AddCircle, Icons.Outlined.AddCircleOutline),
    COLLECTION("Calendar", Icons.Filled.DateRange, Icons.Outlined.DateRange),
    REPORTS("Reports", Icons.Filled.Analytics, Icons.Outlined.Analytics)
}

@Composable
fun LendFlowBottomNavigation(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        border = BorderStroke(1.dp, BorderLight),
        shadowElevation = 0.dp
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            NavDestination.entries.forEach { destination ->
                val isSelected = currentDestination == destination
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(destination) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.title
                        )
                    },
                    label = {
                        Text(
                            text = destination.title.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlueLight,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}
