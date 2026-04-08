package com.example.dobroapp.presentation.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dobroapp.domain.model.CoinTransaction
import com.example.dobroapp.domain.repository.WalletRepository
import com.example.dobroapp.domain.usecase.ResolveRankUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val resolveRankUseCase: ResolveRankUseCase
) : ViewModel() {
    private val _balance = MutableStateFlow(0)
    val balance: StateFlow<Int> = _balance.asStateFlow()

    private val _rank = MutableStateFlow("")
    val rank: StateFlow<String> = _rank.asStateFlow()

    private val _transactions = MutableStateFlow(emptyList<CoinTransaction>())
    val transactions: StateFlow<List<CoinTransaction>> = _transactions.asStateFlow()

    private var userId: String = ""

    fun refresh() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                val currentBalance = walletRepository.getBalance(userId)
                _balance.value = currentBalance
                _rank.value = resolveRankUseCase(currentBalance)
                _transactions.value = walletRepository.getTransactions(userId).reversed()
            }.onFailure {
                _balance.value = 0
                _rank.value = resolveRankUseCase(0)
                _transactions.value = emptyList()
            }
        }
    }

    fun bindUser(userId: String) {
        this.userId = userId
        refresh()
    }
}
