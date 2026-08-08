package com.example.data.firebase

import android.util.Log
import com.example.data.entity.FriendEntity
import com.example.data.entity.LoanEntity
import com.example.data.entity.PaymentEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirestoreSyncHelper {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("LendFlow", "Firebase Firestore unavailable: ${e.message}")
            null
        }
    }

    fun syncFriend(friend: FriendEntity) {
        firestore?.collection("friends")
            ?.document(friend.id.toString())
            ?.set(
                mapOf(
                    "id" to friend.id,
                    "name" to friend.name,
                    "avatarColorHex" to friend.avatarColorHex,
                    "createdAt" to friend.createdAt
                )
            )
            ?.addOnFailureListener { Log.e("LendFlow", "Failed to sync friend: ${it.message}") }
    }

    fun deleteFriend(friendId: Long) {
        firestore?.collection("friends")
            ?.document(friendId.toString())
            ?.delete()
            ?.addOnFailureListener { Log.e("LendFlow", "Failed to delete friend: ${it.message}") }
    }

    fun syncLoan(loan: LoanEntity) {
        firestore?.collection("loans")
            ?.document(loan.id.toString())
            ?.set(
                mapOf(
                    "id" to loan.id,
                    "friendId" to loan.friendId,
                    "friendName" to loan.friendName,
                    "loanAmount" to loan.loanAmount,
                    "startDate" to loan.startDate,
                    "repaymentMonths" to loan.repaymentMonths,
                    "monthlyAmount" to loan.monthlyAmount,
                    "totalRepayment" to loan.totalRepayment,
                    "notes" to loan.notes,
                    "status" to loan.status,
                    "createdAt" to loan.createdAt
                )
            )
            ?.addOnFailureListener { Log.e("LendFlow", "Failed to sync loan: ${it.message}") }
    }

    fun deleteLoan(loanId: Long) {
        firestore?.collection("loans")
            ?.document(loanId.toString())
            ?.delete()
            ?.addOnFailureListener { Log.e("LendFlow", "Failed to delete loan: ${it.message}") }
    }

    fun syncPayment(payment: PaymentEntity) {
        firestore?.collection("payments")
            ?.document(payment.id.toString())
            ?.set(
                mapOf(
                    "id" to payment.id,
                    "loanId" to payment.loanId,
                    "amount" to payment.amount,
                    "installmentIndex" to payment.installmentIndex,
                    "paymentDate" to payment.paymentDate,
                    "notes" to payment.notes
                )
            )
            ?.addOnFailureListener { Log.e("LendFlow", "Failed to sync payment: ${it.message}") }
    }

    fun deletePaymentsForLoan(loanId: Long) {
        firestore?.collection("payments")
            ?.whereEqualTo("loanId", loanId)
            ?.get()
            ?.addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.delete()
                }
            }
    }

    // ─── Fetch all friends from Firestore ───────────────────────────────────
    suspend fun fetchAllFriends(): List<FriendEntity> {
        return try {
            val snapshot = firestore?.collection("friends")?.get()?.await() ?: return emptyList()
            snapshot.documents.mapNotNull { doc ->
                try {
                    FriendEntity(
                        id = (doc.getLong("id") ?: 0L),
                        name = doc.getString("name") ?: "",
                        avatarColorHex = doc.getString("avatarColorHex") ?: "#2563EB",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e("LendFlow", "Error parsing friend doc: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LendFlow", "Failed to fetch friends from Firestore: ${e.message}")
            emptyList()
        }
    }

    // ─── Fetch all loans from Firestore ─────────────────────────────────────
    suspend fun fetchAllLoans(): List<LoanEntity> {
        return try {
            val snapshot = firestore?.collection("loans")?.get()?.await() ?: return emptyList()
            snapshot.documents.mapNotNull { doc ->
                try {
                    LoanEntity(
                        id = doc.getLong("id") ?: 0L,
                        friendId = doc.getLong("friendId") ?: 0L,
                        friendName = doc.getString("friendName") ?: "",
                        loanAmount = doc.getDouble("loanAmount") ?: 0.0,
                        startDate = doc.getLong("startDate") ?: 0L,
                        repaymentMonths = (doc.getLong("repaymentMonths") ?: 0L).toInt(),
                        monthlyAmount = doc.getDouble("monthlyAmount") ?: 0.0,
                        totalRepayment = doc.getDouble("totalRepayment") ?: 0.0,
                        notes = doc.getString("notes") ?: "",
                        status = doc.getString("status") ?: "ACTIVE",
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e("LendFlow", "Error parsing loan doc: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LendFlow", "Failed to fetch loans from Firestore: ${e.message}")
            emptyList()
        }
    }

    // ─── Fetch all payments from Firestore ───────────────────────────────────
    suspend fun fetchAllPayments(): List<PaymentEntity> {
        return try {
            val snapshot = firestore?.collection("payments")?.get()?.await() ?: return emptyList()
            snapshot.documents.mapNotNull { doc ->
                try {
                    PaymentEntity(
                        id = doc.getLong("id") ?: 0L,
                        loanId = doc.getLong("loanId") ?: 0L,
                        amount = doc.getDouble("amount") ?: 0.0,
                        paymentDate = doc.getLong("paymentDate") ?: System.currentTimeMillis(),
                        installmentIndex = (doc.getLong("installmentIndex") ?: 0L).toInt(),
                        notes = doc.getString("notes") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("LendFlow", "Error parsing payment doc: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("LendFlow", "Failed to fetch payments from Firestore: ${e.message}")
            emptyList()
        }
    }
}
