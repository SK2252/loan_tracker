package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LoanWithDetails
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.LoanCard
import com.example.ui.components.SummaryCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LendFlowViewModel

@Composable
fun DashboardScreen(
    viewModel: LendFlowViewModel,
    onNavigateToNewLoan: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onRecordPayment: (LoanWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val upcomingCollections by viewModel.upcomingCollections.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewLoan,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_loan")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "New Loan",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Clean Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LendFlow",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Personal Loan Tracker",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Summary Grid (Total Active, Collection in Blue-50, Pending in Rose, Active Friends)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            title = "Total Active",
                            value = CurrencyUtils.formatRupee(stats.totalActiveLoanAmount),
                            modifier = Modifier.weight(1f),
                            highlightColor = TextPrimary,
                            containerColor = Color.White,
                            borderColor = BorderLight,
                            labelColor = TextMuted
                        )
                        SummaryCard(
                            title = "Collection",
                            value = CurrencyUtils.formatRupee(stats.thisMonthCollection),
                            modifier = Modifier.weight(1f),
                            highlightColor = Color(0xFF1D4ED8),
                            containerColor = PrimaryBlueLight,
                            borderColor = Color(0xFFDBEAFE),
                            labelColor = PrimaryBlue
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            title = "Pending",
                            value = CurrencyUtils.formatRupee(stats.pendingCollectionThisMonth),
                            modifier = Modifier.weight(1f),
                            highlightColor = Color(0xFFE11D48),
                            containerColor = Color.White,
                            borderColor = BorderLight,
                            labelColor = TextMuted
                        )
                        SummaryCard(
                            title = "Friends",
                            value = String.format("%02d", stats.activeFriendsCount),
                            modifier = Modifier.weight(1f),
                            highlightColor = TextPrimary,
                            containerColor = Color.White,
                            borderColor = BorderLight,
                            labelColor = TextMuted
                        )
                    }
                }
            }

            // Section: Upcoming Collections
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "View Calendar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigateToCalendar() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (upcomingCollections.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming collections due",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(
                    items = upcomingCollections,
                    key = { it.loan.id }
                ) { loanWithDetails ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = loanWithDetails.loan.friendName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "•  ${loanWithDetails.nextDueLabel}",
                                fontSize = 13.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        LoanCard(
                            loanWithDetails = loanWithDetails,
                            onMarkPaid = {
                                viewModel.recordPayment(loanWithDetails.loan.id, loanWithDetails.loan.monthlyAmount)
                            },
                            onPartialPayment = {
                                onRecordPayment(loanWithDetails)
                            },
                            onDeleteLoan = {
                                viewModel.deleteLoan(loanWithDetails.loan.id)
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
