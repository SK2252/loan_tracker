package com.example.data.repository

import com.example.data.dao.FriendDao
import com.example.data.dao.LoanDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.FriendEntity
import com.example.data.entity.LoanEntity
import com.example.data.entity.PaymentEntity
import com.example.data.firebase.FirestoreSyncHelper
import com.example.domain.model.FriendSummary
import com.example.domain.model.LoanWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class LendFlowRepository(
    private val friendDao: FriendDao,
    private val loanDao: LoanDao,
    private val paymentDao: PaymentDao
) {
    val allFriends: Flow<List<FriendEntity>> = friendDao.getAllFriends()
    val allLoans: Flow<List<LoanEntity>> = loanDao.getAllLoans()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    val loansWithDetails: Flow<List<LoanWithDetails>> = combine(allLoans, allPayments) { loans, payments ->
        loans.map { loan ->
            val loanPayments = payments.filter { it.loanId == loan.id }
            val customDays = when (loan.friendName.trim()) {
                "Sekar" -> 0
                "Arun" -> 1
                "Karthik" -> 2
                "Mani" -> 5
                "Ramesh" -> 12
                "Siva" -> 18
                else -> 15
            }
            LoanWithDetails(
                loan = loan,
                payments = loanPayments,
                customNextDueDays = customDays
            )
        }
    }

    val friendsSummaries: Flow<List<FriendSummary>> = combine(allFriends, loansWithDetails) { friends, loans ->
        friends.map { friend ->
            val friendLoans = loans.filter { it.loan.friendId == friend.id }
            FriendSummary(
                friend = friend,
                loans = friendLoans
            )
        }
    }

    suspend fun recordInstallment(loanId: Long, amount: Double, notes: String = "") {
        val loan = loanDao.getLoanById(loanId) ?: return
        val currentPayments = paymentDao.getPaymentsForLoan(loanId).first()
        val nextIndex = currentPayments.size + 1
        val payment = PaymentEntity(
            loanId = loanId,
            amount = amount,
            installmentIndex = nextIndex,
            notes = notes
        )
        val paymentId = paymentDao.insertPayment(payment)
        val insertedPayment = payment.copy(id = paymentId)
        FirestoreSyncHelper.syncPayment(insertedPayment)

        val newTotalPaid = currentPayments.sumOf { it.amount } + amount
        if (newTotalPaid >= loan.loanAmount) {
            val completedLoan = loan.copy(status = "COMPLETED")
            loanDao.updateLoan(completedLoan)
            FirestoreSyncHelper.syncLoan(completedLoan)
        }
    }

    suspend fun createLoanWithFriend(
        friendName: String,
        loanAmount: Double,
        repaymentMonths: Int,
        monthlyAmount: Double,
        startDate: Long,
        notes: String
    ): Long {
        val trimmedName = friendName.trim()
        val existingFriend = friendDao.getFriendByName(trimmedName)
        val friendObj = if (existingFriend == null) {
            val newFriend = FriendEntity(
                name = trimmedName,
                avatarColorHex = getAvatarColorForName(trimmedName)
            )
            val generatedId = friendDao.insertFriend(newFriend)
            newFriend.copy(id = generatedId)
        } else {
            existingFriend
        }
        FirestoreSyncHelper.syncFriend(friendObj)

        val totalRepayment = monthlyAmount * repaymentMonths
        val loan = LoanEntity(
            friendId = friendObj.id,
            friendName = trimmedName,
            loanAmount = loanAmount,
            startDate = startDate,
            repaymentMonths = repaymentMonths,
            monthlyAmount = monthlyAmount,
            totalRepayment = totalRepayment,
            notes = notes,
            status = "ACTIVE"
        )
        val loanId = loanDao.insertLoan(loan)
        val insertedLoan = loan.copy(id = loanId)
        FirestoreSyncHelper.syncLoan(insertedLoan)

        return loanId
    }

    suspend fun deleteLoan(loanId: Long) {
        paymentDao.deletePaymentsForLoan(loanId)
        loanDao.deleteLoanById(loanId)
        FirestoreSyncHelper.deleteLoan(loanId)
        FirestoreSyncHelper.deletePaymentsForLoan(loanId)
    }

    suspend fun deleteFriend(friendId: Long) {
        paymentDao.deletePaymentsForFriend(friendId)
        loanDao.deleteLoansForFriend(friendId)
        friendDao.deleteFriendById(friendId)
        FirestoreSyncHelper.deleteFriend(friendId)
    }

    suspend fun seedInitialMockDataIfNeeded() {
        val friends = friendDao.getAllFriends().first()
        if (friends.isNotEmpty()) return

        val mockEntries = listOf(
            MockDataSpec("Sekar", 2100.0, 9, 366.0, 2, "#2563EB"),
            MockDataSpec("Arun", 3250.0, 6, 557.0, 1, "#0284C7"),
            MockDataSpec("Karthik", 1780.0, 6, 297.0, 0, "#059669"),
            MockDataSpec("Ramesh", 4140.0, 6, 690.0, 3, "#7C3AED"),
            MockDataSpec("Siva", 2118.0, 6, 353.0, 4, "#D97706"),
            MockDataSpec("Mani", 13374.0, 6, 2229.0, 1, "#4F46E5")
        )

        for (spec in mockEntries) {
            val friendEntity = FriendEntity(name = spec.name, avatarColorHex = spec.color)
            val friendId = friendDao.insertFriend(friendEntity)
            FirestoreSyncHelper.syncFriend(friendEntity.copy(id = friendId))

            val loanEntity = LoanEntity(
                friendId = friendId,
                friendName = spec.name,
                loanAmount = spec.loanAmount,
                startDate = System.currentTimeMillis() - (spec.paidInstallments * 30L * 24 * 3600 * 1000),
                repaymentMonths = spec.months,
                monthlyAmount = spec.monthlyAmount,
                totalRepayment = spec.monthlyAmount * spec.months,
                notes = "Personal loan via Slice",
                status = "ACTIVE"
            )
            val loanId = loanDao.insertLoan(loanEntity)
            FirestoreSyncHelper.syncLoan(loanEntity.copy(id = loanId))

            for (i in 1..spec.paidInstallments) {
                val paymentEntity = PaymentEntity(
                    loanId = loanId,
                    amount = spec.monthlyAmount,
                    installmentIndex = i,
                    paymentDate = System.currentTimeMillis() - ((spec.paidInstallments - i + 1) * 30L * 24 * 3600 * 1000),
                    notes = "Installment #$i"
                )
                val paymentId = paymentDao.insertPayment(paymentEntity)
                FirestoreSyncHelper.syncPayment(paymentEntity.copy(id = paymentId))
            }
        }
    }

    private fun getAvatarColorForName(name: String): String {
        val colors = listOf("#2563EB", "#0284C7", "#059669", "#7C3AED", "#D97706", "#4F46E5", "#0D9488")
        return colors[name.hashCode().coerceAtLeast(0) % colors.size]
    }
}

private data class MockDataSpec(
    val name: String,
    val loanAmount: Double,
    val months: Int,
    val monthlyAmount: Double,
    val paidInstallments: Int,
    val color: String
)
