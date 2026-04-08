package com.example.dobroapp.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dobroapp.domain.model.LeaderboardEntry
import com.example.dobroapp.domain.repository.LeaderboardRepository
import com.example.dobroapp.domain.usecase.ResolveRankUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel(
    private val repository: LeaderboardRepository,
    private val resolveRankUseCase: ResolveRankUseCase
) : ViewModel() {
    private val _items = MutableStateFlow(emptyList<LeaderboardEntry>())
    val items: StateFlow<List<LeaderboardEntry>> = _items.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                _items.value = repository.getLeaderboard().sortedByDescending { it.coins }.map {
                    it.copy(rankTitle = resolveRankUseCase(it.coins))
                }
            }.onFailure {
                _items.value = emptyList()
            }
        }
    }
}
