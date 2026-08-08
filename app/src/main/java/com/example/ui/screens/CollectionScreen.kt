package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DueStatus
import com.example.domain.model.LoanWithDetails
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.LoanCard
import com.example.ui.theme.BorderLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LendFlowViewModel

@Composable
fun CollectionScreen(
    viewModel: LendFlowViewModel,
    modifier: Modifier = Modifier
) {
    val loans by viewModel.loansWithDetails.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    var partialPaymentLoan by remember { mutableStateOf<LoanWithDetails?>(null) }
    var partialAmountText by remember { mutableStateOf("") }

    val filteredLoans = remember(loans, selectedFilter) {
        when (selectedFilter) {
            "Due Today" -> loans.filter { it.dueStatus == DueStatus.DUE_TODAY }
            "Due Soon" -> loans.filter { it.dueStatus == DueStatus.DUE_SOON || it.dueStatus == DueStatus.TOMORROW }
            "Overdue" -> loans.filter { it.dueStatus == DueStatus.OVERDUE }
            "Completed" -> loans.filter { it.isCompleted }
            else -> loans.filter { !it.isCompleted }
        }
    }

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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Collections",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Track upcoming and past installment collections",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterOptions = listOf("Active", "Due Today", "Due Soon", "Overdue", "Completed")
                    items(filterOptions) { filter ->
                        val isSelected = (selectedFilter == filter) || (selectedFilter == "All" && filter == "Active")
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = TextPrimary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = BorderLight,
                                selectedBorderColor = PrimaryBlue
                            )
                        )
                    }
                }
            }

            if (filteredLoans.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No loans found in '$selectedFilter'",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(
                    items = filteredLoans,
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
                                text = "•  Due ${CurrencyUtils.formatRupee(loanWithDetails.loan.monthlyAmount)}",
                                fontSize = 13.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        LoanCard(
                            loanWithDetails = loanWithDetails,
                            onMarkPaid = {
                                viewModel.recordPayment(loanWithDetails.loan.id, loanWithDetails.loan.monthlyAmount)
                            },
                            onPartialPayment = {
                                partialPaymentLoan = loanWithDetails
                                partialAmountText = ""
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

    // Partial Payment Dialog
    if (partialPaymentLoan != null) {
        val targetLoan = partialPaymentLoan!!
        AlertDialog(
            onDismissRequest = { partialPaymentLoan = null },
            title = { Text("Partial Payment for ${targetLoan.loan.friendName}", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Monthly installment is ${CurrencyUtils.formatRupee(targetLoan.loan.monthlyAmount)}. Enter custom amount collected:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = partialAmountText,
                        onValueChange = { partialAmountText = it },
                        placeholder = { Text("Amount in ₹", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = partialAmountText.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            viewModel.recordPayment(targetLoan.loan.id, amt, "Partial payment")
                            partialPaymentLoan = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { partialPaymentLoan = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }
}
