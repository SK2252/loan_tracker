package com.example.domain.model

import com.example.data.entity.FriendEntity
import com.example.data.entity.LoanEntity
import com.example.data.entity.PaymentEntity

enum class DueStatus {
    DUE_TODAY,
    TOMORROW,
    DUE_SOON,
    UPCOMING,
    OVERDUE,
    COMPLETED
}

data class LoanWithDetails(
    val loan: LoanEntity,
    val payments: List<PaymentEntity> = emptyList(),
    val customNextDueDays: Int? = null // For mock data alignment
) {
    val totalPaid: Double
        get() = payments.sumOf { it.amount }

    val pendingAmount: Double
        get() = (loan.loanAmount - totalPaid).coerceAtLeast(0.0)

    val paidInstallmentsCount: Int
        get() = payments.size

    val totalInstallments: Int
        get() = loan.repaymentMonths

    val isCompleted: Boolean
        get() = pendingAmount <= 0.0 || loan.status == "COMPLETED"

    val nextDueLabel: String
        get() {
            if (isCompleted) return "Completed"
            return when (customNextDueDays) {
                0 -> "Due Today"
                1 -> "Tomorrow"
                2 -> "2 days left"
                in 3..30 -> "$customNextDueDays days left"
                else -> "Due in 15 days"
            }
        }

    val dueStatus: DueStatus
        get() {
            if (isCompleted) return DueStatus.COMPLETED
            return when (customNextDueDays) {
                0 -> DueStatus.DUE_TODAY
                1 -> DueStatus.TOMORROW
                in 2..5 -> DueStatus.DUE_SOON
                in 6..30 -> DueStatus.UPCOMING
                else -> DueStatus.UPCOMING
            }
        }
}

data class FriendSummary(
    val friend: FriendEntity,
    val loans: List<LoanWithDetails> = emptyList()
) {
    val activeLoansCount: Int
        get() = loans.count { !it.isCompleted }

    val totalPendingAmount: Double
        get() = loans.sumOf { it.pendingAmount }

    val totalLoanAmount: Double
        get() = loans.sumOf { it.loan.loanAmount }

    val totalCollectedAmount: Double
        get() = loans.sumOf { it.totalPaid }

    val nextDueLoan: LoanWithDetails?
        get() = loans.filter { !it.isCompleted }
            .minByOrNull { it.customNextDueDays ?: 99 }

    val nextDueText: String
        get() {
            val loan = nextDueLoan ?: return "No dues"
            return loan.nextDueLabel
        }

    val nextDueAmount: Double
        get() = nextDueLoan?.loan?.monthlyAmount ?: 0.0
}

data class DashboardStats(
    val totalActiveLoanAmount: Double = 0.0,
    val thisMonthCollection: Double = 0.0,
    val pendingCollectionThisMonth: Double = 0.0,
    val activeFriendsCount: Int = 0
)
