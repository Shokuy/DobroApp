package com.example.dobroapp.presentation.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dobroapp.domain.model.RewardItem
import com.example.dobroapp.domain.repository.RewardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RewardsViewModel(
    private val repository: RewardsRepository
) : ViewModel() {
    private val _rewards = MutableStateFlow(emptyList<RewardItem>())
    val rewards: StateFlow<List<RewardItem>> = _rewards.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                _rewards.value = repository.getRewards()
            }.onFailure {
                _rewards.value = emptyList()
            }
        }
    }
}
