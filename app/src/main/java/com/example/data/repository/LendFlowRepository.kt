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
import java.text.SimpleDateFormat
import java.util.Locale

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
            LoanWithDetails(
                loan = loan,
                payments = loanPayments,
                customNextDueDays = 0
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

    // ─── Step 1: On fresh install, try to restore from Firestore ────────────
    suspend fun syncFromFirestoreIfNeeded() {
        val existingFriends = friendDao.getAllFriends().first()
        if (existingFriends.isNotEmpty()) return // Room already has data, skip

        val remoteFriends = FirestoreSyncHelper.fetchAllFriends()
        val remoteLoans = FirestoreSyncHelper.fetchAllLoans()
        val remotePayments = FirestoreSyncHelper.fetchAllPayments()

        if (remoteFriends.isEmpty()) return // Firestore also empty — seed will run next

        for (friend in remoteFriends) {
            friendDao.insertFriend(friend)
        }
        for (loan in remoteLoans) {
            loanDao.insertLoan(loan)
        }
        for (payment in remotePayments) {
            paymentDao.insertPayment(payment)
        }
    }

    // ─── Step 2: If Firestore was empty too, seed real data ─────────────────
    suspend fun seedRealDataIfNeeded() {
        val friends = friendDao.getAllFriends().first()
        if (friends.isNotEmpty()) return // Already populated

        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun dateMs(s: String) = fmt.parse(s)?.time ?: System.currentTimeMillis()

        // ── FRIEND 1: Sekar ──────────────────────────────────────────────────
        val sekar = FriendEntity(name = "Sekar", avatarColorHex = "#2563EB")
        val sekarId = friendDao.insertFriend(sekar)
        FirestoreSyncHelper.syncFriend(sekar.copy(id = sekarId))

        data class InstallmentSpec(val index: Int, val dueDate: String, val amount: Double, val paid: Boolean)
        data class LoanSpec(val loanAmount: Double, val startDate: String, val months: Int, val monthlyAmount: Double, val status: String, val installments: List<InstallmentSpec>)

        val sekarLoans = listOf(
            LoanSpec(1659.0, "2025-12-02", 9, 206.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-01-02", 206.0, true), InstallmentSpec(2, "2026-02-02", 206.0, true),
                InstallmentSpec(3, "2026-03-02", 206.0, true), InstallmentSpec(4, "2026-04-02", 206.0, true),
                InstallmentSpec(5, "2026-05-02", 206.0, true), InstallmentSpec(6, "2026-06-02", 206.0, true),
                InstallmentSpec(7, "2026-07-02", 206.0, true), InstallmentSpec(8, "2026-08-02", 206.0, true),
                InstallmentSpec(9, "2026-09-02", 206.0, false)
            )),
            LoanSpec(544.0, "2025-12-02", 9, 69.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-01-02", 69.0, true), InstallmentSpec(2, "2026-02-02", 69.0, true),
                InstallmentSpec(3, "2026-03-02", 69.0, true), InstallmentSpec(4, "2026-04-02", 69.0, true),
                InstallmentSpec(5, "2026-05-02", 69.0, true), InstallmentSpec(6, "2026-06-02", 69.0, true),
                InstallmentSpec(7, "2026-07-02", 69.0, true), InstallmentSpec(8, "2026-08-02", 69.0, true),
                InstallmentSpec(9, "2026-09-02", 69.0, false)
            )),
            LoanSpec(1229.0, "2026-01-04", 9, 152.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-02-04", 152.0, true), InstallmentSpec(2, "2026-03-04", 152.0, true),
                InstallmentSpec(3, "2026-04-04", 152.0, true), InstallmentSpec(4, "2026-05-04", 152.0, true),
                InstallmentSpec(5, "2026-06-04", 152.0, true), InstallmentSpec(6, "2026-07-04", 152.0, true),
                InstallmentSpec(7, "2026-08-04", 152.0, true), InstallmentSpec(8, "2026-09-04", 152.0, false),
                InstallmentSpec(9, "2026-10-04", 152.0, false)
            )),
            LoanSpec(1309.0, "2026-02-05", 12, 126.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-03-05", 126.0, true), InstallmentSpec(2, "2026-04-05", 126.0, true),
                InstallmentSpec(3, "2026-05-05", 126.0, true), InstallmentSpec(4, "2026-06-05", 126.0, true),
                InstallmentSpec(5, "2026-07-05", 126.0, true), InstallmentSpec(6, "2026-08-05", 126.0, true),
                InstallmentSpec(7, "2026-09-05", 126.0, false), InstallmentSpec(8, "2026-10-05", 126.0, false),
                InstallmentSpec(9, "2026-11-05", 126.0, false), InstallmentSpec(10, "2026-12-05", 126.0, false),
                InstallmentSpec(11, "2027-01-05", 126.0, false), InstallmentSpec(12, "2027-02-05", 126.0, false)
            )),
            LoanSpec(376.0, "2026-03-05", 6, 68.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-04-05", 68.0, true), InstallmentSpec(2, "2026-05-05", 68.0, true),
                InstallmentSpec(3, "2026-06-05", 68.0, true), InstallmentSpec(4, "2026-07-05", 68.0, true),
                InstallmentSpec(5, "2026-08-05", 68.0, true), InstallmentSpec(6, "2026-09-05", 68.0, false)
            )),
            LoanSpec(1493.0, "2026-04-02", 9, 185.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-05-02", 185.0, true), InstallmentSpec(2, "2026-06-02", 185.0, true),
                InstallmentSpec(3, "2026-07-02", 185.0, true), InstallmentSpec(4, "2026-08-02", 185.0, true),
                InstallmentSpec(5, "2026-09-02", 185.0, false), InstallmentSpec(6, "2026-10-02", 185.0, false),
                InstallmentSpec(7, "2026-11-02", 185.0, false), InstallmentSpec(8, "2026-12-02", 185.0, false),
                InstallmentSpec(9, "2027-01-02", 185.0, false)
            )),
            LoanSpec(1047.0, "2026-04-02", 6, 189.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-05-02", 189.0, true), InstallmentSpec(2, "2026-06-02", 189.0, true),
                InstallmentSpec(3, "2026-07-02", 189.0, true), InstallmentSpec(4, "2026-08-02", 189.0, true),
                InstallmentSpec(5, "2026-09-02", 189.0, false), InstallmentSpec(6, "2026-10-02", 189.0, false)
            )),
            LoanSpec(1757.0, "2026-05-05", 9, 217.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-06-05", 217.0, true), InstallmentSpec(2, "2026-07-05", 217.0, true),
                InstallmentSpec(3, "2026-08-05", 217.0, true), InstallmentSpec(4, "2026-09-05", 217.0, false),
                InstallmentSpec(5, "2026-10-05", 217.0, false), InstallmentSpec(6, "2026-11-05", 217.0, false),
                InstallmentSpec(7, "2026-12-05", 217.0, false), InstallmentSpec(8, "2027-01-05", 217.0, false),
                InstallmentSpec(9, "2027-02-05", 217.0, false)
            )),
            LoanSpec(2094.0, "2026-05-06", 12, 200.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-06-06", 200.0, true), InstallmentSpec(2, "2026-07-06", 200.0, true),
                InstallmentSpec(3, "2026-08-06", 200.0, true), InstallmentSpec(4, "2026-09-06", 200.0, false),
                InstallmentSpec(5, "2026-10-06", 200.0, false), InstallmentSpec(6, "2026-11-06", 200.0, false),
                InstallmentSpec(7, "2026-12-06", 200.0, false), InstallmentSpec(8, "2027-01-06", 200.0, false),
                InstallmentSpec(9, "2027-02-06", 200.0, false), InstallmentSpec(10, "2027-03-06", 200.0, false),
                InstallmentSpec(11, "2027-04-06", 200.0, false), InstallmentSpec(12, "2027-05-06", 200.0, false)
            )),
            LoanSpec(2070.0, "2026-07-20", 6, 367.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-08-20", 367.0, false), InstallmentSpec(2, "2026-09-20", 367.0, false),
                InstallmentSpec(3, "2026-10-20", 367.0, false), InstallmentSpec(4, "2026-11-20", 367.0, false),
                InstallmentSpec(5, "2026-12-20", 367.0, false), InstallmentSpec(6, "2027-01-20", 367.0, false)
            )),
            LoanSpec(2045.0, "2026-08-05", 9, 252.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-09-05", 252.0, false), InstallmentSpec(2, "2026-10-05", 252.0, false),
                InstallmentSpec(3, "2026-11-05", 252.0, false), InstallmentSpec(4, "2026-12-05", 252.0, false),
                InstallmentSpec(5, "2027-01-05", 252.0, false), InstallmentSpec(6, "2027-02-05", 252.0, false),
                InstallmentSpec(7, "2027-03-05", 252.0, false), InstallmentSpec(8, "2027-04-05", 252.0, false),
                InstallmentSpec(9, "2027-05-05", 252.0, false)
            ))
        )

        for (spec in sekarLoans) {
            val loanEntity = LoanEntity(
                friendId = sekarId, friendName = "Sekar",
                loanAmount = spec.loanAmount, startDate = dateMs(spec.startDate),
                repaymentMonths = spec.months, monthlyAmount = spec.monthlyAmount,
                totalRepayment = spec.monthlyAmount * spec.months, status = spec.status
            )
            val loanId = loanDao.insertLoan(loanEntity)
            FirestoreSyncHelper.syncLoan(loanEntity.copy(id = loanId))
            for (inst in spec.installments.filter { it.paid }) {
                val p = PaymentEntity(loanId = loanId, amount = inst.amount, installmentIndex = inst.index, paymentDate = dateMs(inst.dueDate))
                val pId = paymentDao.insertPayment(p)
                FirestoreSyncHelper.syncPayment(p.copy(id = pId))
            }
        }

        // ── FRIEND 2: Sekar(Pipe) ────────────────────────────────────────────
        val sekarPipe = FriendEntity(name = "Sekar(Pipe)", avatarColorHex = "#0284C7")
        val sekarPipeId = friendDao.insertFriend(sekarPipe)
        FirestoreSyncHelper.syncFriend(sekarPipe.copy(id = sekarPipeId))

        val sekarPipeLoan = LoanEntity(
            friendId = sekarPipeId, friendName = "Sekar(Pipe)",
            loanAmount = 2070.0, startDate = dateMs("2026-01-05"),
            repaymentMonths = 12, monthlyAmount = 198.0, totalRepayment = 198.0 * 12, status = "ACTIVE"
        )
        val sekarPipeLoanId = loanDao.insertLoan(sekarPipeLoan)
        FirestoreSyncHelper.syncLoan(sekarPipeLoan.copy(id = sekarPipeLoanId))
        listOf(
            InstallmentSpec(1, "2026-02-05", 198.0, true), InstallmentSpec(2, "2026-03-05", 198.0, true),
            InstallmentSpec(3, "2026-04-05", 198.0, true), InstallmentSpec(4, "2026-05-05", 198.0, true),
            InstallmentSpec(5, "2026-06-05", 198.0, true), InstallmentSpec(6, "2026-07-05", 198.0, true),
            InstallmentSpec(7, "2026-08-05", 198.0, true)
        ).forEach { inst ->
            val p = PaymentEntity(loanId = sekarPipeLoanId, amount = inst.amount, installmentIndex = inst.index, paymentDate = dateMs(inst.dueDate))
            val pId = paymentDao.insertPayment(p)
            FirestoreSyncHelper.syncPayment(p.copy(id = pId))
        }

        // ── FRIEND 3: Karthi ─────────────────────────────────────────────────
        val karthi = FriendEntity(name = "Karthi", avatarColorHex = "#059669")
        val karthiId = friendDao.insertFriend(karthi)
        FirestoreSyncHelper.syncFriend(karthi.copy(id = karthiId))

        val karthiLoans = listOf(
            LoanSpec(1047.0, "2026-02-11", 6, 187.0, "COMPLETED", listOf(
                InstallmentSpec(1, "2026-03-11", 187.0, true), InstallmentSpec(2, "2026-04-11", 187.0, true),
                InstallmentSpec(3, "2026-05-11", 187.0, true), InstallmentSpec(4, "2026-06-11", 187.0, true),
                InstallmentSpec(5, "2026-07-11", 187.0, true), InstallmentSpec(6, "2026-08-11", 187.0, false)
            )),
            LoanSpec(2070.0, "2026-04-23", 6, 253.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-05-23", 253.0, true), InstallmentSpec(2, "2026-06-23", 253.0, true),
                InstallmentSpec(3, "2026-07-23", 253.0, true), InstallmentSpec(4, "2026-08-23", 253.0, false),
                InstallmentSpec(5, "2026-09-23", 253.0, false), InstallmentSpec(6, "2026-10-23", 253.0, false)
            )),
            LoanSpec(1059.0, "2026-04-24", 6, 100.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-05-24", 100.0, true), InstallmentSpec(2, "2026-06-24", 100.0, true),
                InstallmentSpec(3, "2026-07-24", 100.0, true), InstallmentSpec(4, "2026-08-24", 100.0, false),
                InstallmentSpec(5, "2026-09-24", 100.0, false), InstallmentSpec(6, "2026-10-24", 100.0, false)
            ))
        )
        for (spec in karthiLoans) {
            val loanEntity = LoanEntity(
                friendId = karthiId, friendName = "Karthi",
                loanAmount = spec.loanAmount, startDate = dateMs(spec.startDate),
                repaymentMonths = spec.months, monthlyAmount = spec.monthlyAmount,
                totalRepayment = spec.monthlyAmount * spec.months, status = spec.status
            )
            val loanId = loanDao.insertLoan(loanEntity)
            FirestoreSyncHelper.syncLoan(loanEntity.copy(id = loanId))
            for (inst in spec.installments.filter { it.paid }) {
                val p = PaymentEntity(loanId = loanId, amount = inst.amount, installmentIndex = inst.index, paymentDate = dateMs(inst.dueDate))
                val pId = paymentDao.insertPayment(p)
                FirestoreSyncHelper.syncPayment(p.copy(id = pId))
            }
        }

        // ── FRIEND 4: Shyam ──────────────────────────────────────────────────
        val shyam = FriendEntity(name = "Shyam", avatarColorHex = "#7C3AED")
        val shyamId = friendDao.insertFriend(shyam)
        FirestoreSyncHelper.syncFriend(shyam.copy(id = shyamId))

        val shyamLoans = listOf(
            LoanSpec(6212.0, "2026-07-08", 12, 589.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-08-08", 589.0, true),
                InstallmentSpec(2, "2026-09-08", 589.0, false), InstallmentSpec(3, "2026-10-08", 589.0, false),
                InstallmentSpec(4, "2026-11-08", 589.0, false), InstallmentSpec(5, "2026-12-08", 589.0, false),
                InstallmentSpec(6, "2027-01-08", 589.0, false), InstallmentSpec(7, "2027-02-08", 589.0, false),
                InstallmentSpec(8, "2027-03-08", 589.0, false), InstallmentSpec(9, "2027-04-08", 589.0, false),
                InstallmentSpec(10, "2027-05-08", 589.0, false), InstallmentSpec(11, "2027-06-08", 589.0, false),
                InstallmentSpec(12, "2027-07-08", 589.0, false)
            )),
            LoanSpec(1059.0, "2026-07-09", 12, 101.0, "ACTIVE", listOf(
                InstallmentSpec(1, "2026-08-09", 101.0, true),
                InstallmentSpec(2, "2026-09-09", 101.0, false), InstallmentSpec(3, "2026-10-09", 101.0, false),
                InstallmentSpec(4, "2026-11-09", 101.0, false), InstallmentSpec(5, "2026-12-09", 101.0, false),
                InstallmentSpec(6, "2027-01-09", 101.0, false), InstallmentSpec(7, "2027-02-09", 101.0, false),
                InstallmentSpec(8, "2027-03-09", 101.0, false), InstallmentSpec(9, "2027-04-09", 101.0, false),
                InstallmentSpec(10, "2027-05-09", 101.0, false), InstallmentSpec(11, "2027-06-09", 101.0, false),
                InstallmentSpec(12, "2027-07-09", 101.0, false)
            ))
        )
        for (spec in shyamLoans) {
            val loanEntity = LoanEntity(
                friendId = shyamId, friendName = "Shyam",
                loanAmount = spec.loanAmount, startDate = dateMs(spec.startDate),
                repaymentMonths = spec.months, monthlyAmount = spec.monthlyAmount,
                totalRepayment = spec.monthlyAmount * spec.months, status = spec.status
            )
            val loanId = loanDao.insertLoan(loanEntity)
            FirestoreSyncHelper.syncLoan(loanEntity.copy(id = loanId))
            for (inst in spec.installments.filter { it.paid }) {
                val p = PaymentEntity(loanId = loanId, amount = inst.amount, installmentIndex = inst.index, paymentDate = dateMs(inst.dueDate))
                val pId = paymentDao.insertPayment(p)
                FirestoreSyncHelper.syncPayment(p.copy(id = pId))
            }
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

    private fun getAvatarColorForName(name: String): String {
        val colors = listOf("#2563EB", "#0284C7", "#059669", "#7C3AED", "#D97706", "#4F46E5", "#0D9488")
        return colors[name.hashCode().coerceAtLeast(0) % colors.size]
    }
}
