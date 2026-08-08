package com.example.data.firebase

import android.util.Log
import com.example.data.entity.FriendEntity
import com.example.data.entity.LoanEntity
import com.example.data.entity.PaymentEntity
import com.google.firebase.firestore.FirebaseFirestore

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
}
