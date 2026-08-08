package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.LendFlowDatabase
import com.example.data.repository.LendFlowRepository
import com.example.domain.model.DashboardStats
import com.example.domain.model.FriendSummary
import com.example.domain.model.LoanWithDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LendFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LendFlowRepository
    val friendsSummaries: StateFlow<List<FriendSummary>>
    val loansWithDetails: StateFlow<List<LoanWithDetails>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFriendId = MutableStateFlow<Long?>(null)
    val selectedFriendId: StateFlow<Long?> = _selectedFriendId

    init {
        val database = LendFlowDatabase.getDatabase(application)
        repository = LendFlowRepository(
            friendDao = database.friendDao(),
            loanDao = database.loanDao(),
            paymentDao = database.paymentDao()
        )

        viewModelScope.launch {
            repository.seedInitialMockDataIfNeeded()
        }

        loansWithDetails = repository.loansWithDetails.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        friendsSummaries = repository.friendsSummaries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    val filteredFriends: StateFlow<List<FriendSummary>> = combine(friendsSummaries, searchQuery) { friends, query ->
        if (query.isBlank()) {
            friends
        } else {
            friends.filter { it.friend.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedFriendSummary: StateFlow<FriendSummary?> = combine(friendsSummaries, _selectedFriendId) { friends, id ->
        if (id == null) null else friends.find { it.friend.id == id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val dashboardStats: StateFlow<DashboardStats> = loansWithDetails.map { loans ->
        val activeLoans = loans.filter { !it.isCompleted }
        val totalActiveLoan = activeLoans.sumOf { it.pendingAmount }
        
        val currentMonthPayments = loans.flatMap { it.payments }
        val thisMonthCollected = currentMonthPayments.sumOf { it.amount }
        val pendingThisMonth = activeLoans.sumOf { it.loan.monthlyAmount }
        val activeFriendsCount = activeLoans.map { it.loan.friendId }.distinct().size

        DashboardStats(
            totalActiveLoanAmount = totalActiveLoan,
            thisMonthCollection = thisMonthCollected,
            pendingCollectionThisMonth = pendingThisMonth,
            activeFriendsCount = activeFriendsCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardStats()
    )

    val upcomingCollections: StateFlow<List<LoanWithDetails>> = loansWithDetails.map { loans ->
        loans.filter { !it.isCompleted }
            .sortedBy { it.customNextDueDays ?: 99 }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectFriend(friendId: Long?) {
        _selectedFriendId.value = friendId
    }

    fun recordPayment(loanId: Long, amount: Double, notes: String = "") {
        viewModelScope.launch {
            repository.recordInstallment(loanId, amount, notes)
        }
    }

    fun deleteLoan(loanId: Long) {
        viewModelScope.launch {
            repository.deleteLoan(loanId)
        }
    }

    fun deleteFriend(friendId: Long) {
        viewModelScope.launch {
            repository.deleteFriend(friendId)
            if (_selectedFriendId.value == friendId) {
                _selectedFriendId.value = null
            }
        }
    }

    fun createLoan(
        friendName: String,
        loanAmount: Double,
        repaymentMonths: Int,
        monthlyAmount: Double,
        startDate: Long,
        notes: String
    ) {
        viewModelScope.launch {
            repository.createLoanWithFriend(
                friendName = friendName,
                loanAmount = loanAmount,
                repaymentMonths = repaymentMonths,
                monthlyAmount = monthlyAmount,
                startDate = startDate,
                notes = notes
            )
        }
    }
}
