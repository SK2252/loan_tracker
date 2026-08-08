package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LoanWithDetails
import com.example.ui.theme.BorderLight
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoanCard(
    loanWithDetails: LoanWithDetails,
    onMarkPaid: (() -> Unit)? = null,
    onPartialPayment: (() -> Unit)? = null,
    onDeleteLoan: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val loan = loanWithDetails.loan
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val progress = if (loanWithDetails.totalInstallments > 0) {
        (loanWithDetails.paidInstallmentsCount.toFloat() / loanWithDetails.totalInstallments.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val formattedStartDate = dateFormat.format(Date(loan.startDate))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Loan ${CurrencyUtils.formatRupee(loan.loanAmount)}",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${loan.repaymentMonths} Months  •  ${CurrencyUtils.formatRupee(loan.monthlyAmount)}/month",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(status = loanWithDetails.dueStatus, customText = loanWithDetails.nextDueLabel)
                    if (onDeleteLoan != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete Loan",
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar & text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Started $formattedStartDate",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Paid ${loanWithDetails.paidInstallmentsCount} / ${loanWithDetails.totalInstallments}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PrimaryBlue,
                trackColor = Color(0xFFEFF6FF)
            )

            if (!loanWithDetails.isCompleted && (onMarkPaid != null || onPartialPayment != null)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onMarkPaid != null) {
                        Button(
                            onClick = onMarkPaid,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text(text = "Mark Paid (${CurrencyUtils.formatRupee(loan.monthlyAmount)})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (onPartialPayment != null) {
                        OutlinedButton(
                            onClick = onPartialPayment,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderLight)
                        ) {
                            Text(text = "Partial", color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (isExpanded) "Hide Schedule" else "View Schedule", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Toggle Schedule",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    for (i in 1..loanWithDetails.totalInstallments) {
                        val payment = loanWithDetails.payments.find { it.installmentIndex == i }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Month $i", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            if (payment != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val payDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(payment.paymentDate))
                                    Text("Paid on $payDateStr", fontSize = 12.sp, color = Color(0xFF16A34A))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Paid", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Pending", fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Outlined.Schedule, contentDescription = "Pending", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Loan", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this loan of ${CurrencyUtils.formatRupee(loan.loanAmount)}? This will remove all payment history for this loan.", fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteLoan?.invoke()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = Color.White
        )
    }
}
