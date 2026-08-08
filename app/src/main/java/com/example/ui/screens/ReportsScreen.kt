package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.LoanCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LendFlowViewModel

@Composable
fun ReportsScreen(
    viewModel: LendFlowViewModel,
    modifier: Modifier = Modifier
) {
    val loans by viewModel.loansWithDetails.collectAsState()
    val stats by viewModel.dashboardStats.collectAsState()

    val totalDisbursed = loans.sumOf { it.loan.loanAmount }
    val totalCollected = loans.sumOf { it.totalPaid }
    val totalPending = (totalDisbursed - totalCollected).coerceAtLeast(0.0)
    val overallRecoveryRate = if (totalDisbursed > 0) ((totalCollected / totalDisbursed) * 100).toInt() else 0

    val completedLoans = loans.filter { it.isCompleted }
    val activeLoans = loans.filter { !it.isCompleted }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White
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
                Text(
                    text = "Reports",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Overall loan performance & collection metrics",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Overall Recovery Rate Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight),
                    border = BorderStroke(1.dp, Color(0xFFDBEAFE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RECOVERY RATE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$overallRecoveryRate%",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Collected", fontSize = 12.sp, color = TextSecondary)
                                Text(CurrencyUtils.formatRupee(totalCollected), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Pending", fontSize = 12.sp, color = TextSecondary)
                                Text(CurrencyUtils.formatRupee(totalPending), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { (overallRecoveryRate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly Collection Breakdown
                Text(
                    text = "Collection Metrics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("COLLECTED THIS MONTH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.8.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(CurrencyUtils.formatRupee(stats.thisMonthCollection), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("MONTHLY DUE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.8.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(CurrencyUtils.formatRupee(stats.pendingCollectionThisMonth), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual bar comparison
                        val totalMonthTarget = stats.thisMonthCollection + stats.pendingCollectionThisMonth
                        val collectedRatio = if (totalMonthTarget > 0) (stats.thisMonthCollection / totalMonthTarget).toFloat() else 0f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF1F5F9))
                        ) {
                            if (collectedRatio > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight(collectedRatio)
                                        .fillMaxSize()
                                        .background(Color(0xFF16A34A))
                                )
                            }
                            if ((1f - collectedRatio) > 0f) {
                                Box(
                                    modifier = Modifier
                                        .weight((1f - collectedRatio).coerceAtLeast(0.01f))
                                        .fillMaxSize()
                                        .background(PrimaryBlue.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Loan Status Breakdown
                Text(
                    text = "Loan Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Summary cards row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ACTIVE LOANS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${activeLoans.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("COMPLETED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.8.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${completedLoans.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }
                    }
                }
            }

            if (completedLoans.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Completed Loans", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                items(
                    items = completedLoans,
                    key = { "comp_${it.loan.id}" }
                ) { loanWithDetails ->
                    Column {
                        Text(
                            text = "${loanWithDetails.loan.friendName} • ${CurrencyUtils.formatRupee(loanWithDetails.loan.loanAmount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        LoanCard(loanWithDetails = loanWithDetails)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
