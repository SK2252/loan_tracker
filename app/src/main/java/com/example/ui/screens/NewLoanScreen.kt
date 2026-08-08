package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CurrencyUtils
import com.example.ui.theme.BorderLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LendFlowViewModel

@Composable
fun NewLoanScreen(
    viewModel: LendFlowViewModel,
    initialFriendName: String = "",
    onLoanCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val friendsSummaries by viewModel.friendsSummaries.collectAsState()

    var friendName by remember { mutableStateOf(initialFriendName) }
    var loanAmountText by remember { mutableStateOf("") }
    var repaymentMonths by remember { mutableIntStateOf(6) }
    var monthlyAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isCustomMonthly by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun updateCalculatedMonthly(amountStr: String, months: Int) {
        val amount = amountStr.toDoubleOrNull()
        if (amount != null && amount > 0 && months > 0 && !isCustomMonthly) {
            val calc = Math.round(amount / months).toDouble()
            monthlyAmountText = calc.toInt().toString()
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
                    text = "New Loan",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Create a new loan record for a friend",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Friend Name Field
                Text("Friend Name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = friendName,
                    onValueChange = {
                        friendName = it
                        errorMessage = null
                    },
                    placeholder = { Text("e.g. Sekar, Arun, Karthik", color = TextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_friend_name")
                )

                // Quick suggestions for existing friends
                if (friendsSummaries.isNotEmpty() && friendName.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Or choose existing friend:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        friendsSummaries.take(4).forEach { summary ->
                            Card(
                                modifier = Modifier.clickable { friendName = summary.friend.name },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight),
                                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = summary.friend.name,
                                    fontSize = 12.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Loan Amount Field
                Text("Loan Amount (₹)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = loanAmountText,
                    onValueChange = {
                        loanAmountText = it
                        updateCalculatedMonthly(it, repaymentMonths)
                        errorMessage = null
                    },
                    placeholder = { Text("e.g. 2100", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_loan_amount")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Repayment Months Quick Selector
                Text("Repayment Duration", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(3, 6, 9, 12).forEach { monthOption ->
                        val isSelected = repaymentMonths == monthOption
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                repaymentMonths = monthOption
                                updateCalculatedMonthly(loanAmountText, monthOption)
                            },
                            label = { Text("$monthOption Months", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = RoundedCornerShape(12.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly Amount Field
                Text("Monthly Payment (₹)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = monthlyAmountText,
                    onValueChange = {
                        monthlyAmountText = it
                        isCustomMonthly = true
                        errorMessage = null
                    },
                    placeholder = { Text("Auto calculated e.g. 366", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_monthly_amount")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notes Field
                Text("Notes (Optional)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("e.g. Personal loan via Slice", color = TextSecondary) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_loan_notes")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Generate Loan Button
                Button(
                    onClick = {
                        val amount = loanAmountText.toDoubleOrNull()
                        val monthly = monthlyAmountText.toDoubleOrNull()
                        if (friendName.isBlank()) {
                            errorMessage = "Please enter a friend name"
                            return@Button
                        }
                        if (amount == null || amount <= 0) {
                            errorMessage = "Please enter a valid loan amount"
                            return@Button
                        }
                        if (monthly == null || monthly <= 0) {
                            errorMessage = "Please enter a valid monthly payment"
                            return@Button
                        }

                        viewModel.createLoan(
                            friendName = friendName.trim(),
                            loanAmount = amount,
                            repaymentMonths = repaymentMonths,
                            monthlyAmount = monthly,
                            startDate = System.currentTimeMillis(),
                            notes = notes
                        )
                        onLoanCreated()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_generate_loan"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Generate Loan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
