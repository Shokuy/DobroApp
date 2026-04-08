package com.example.dobroapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestDto(
    val role: String,
    val full_name: String
)

@Serializable
data class AuthResponseDto(
    val user_id: String,
    val role: String,
    val full_name: String,
    val token: String
)

@Serializable
data class RequestDto(
    val id: String,
    val title: String,
    val help_type: String,
    val reward_coins: Int,
    val district: String,
    val address: String,
    val time: String,
    val comment: String,
    val pensioner_name: String,
    val pensioner_id: String,
    val status: String,
    val volunteer_name: String? = null,
    val volunteer_id: String? = null,
    val rating: Int? = null,
    val lat: Double? = null,
    val lon: Double? = null
)

@Serializable
data class CreateRequestDto(
    val title: String,
    val help_type: String,
    val reward_coins: Int,
    val district: String,
    val address: String,
    val time: String,
    val comment: String,
    val pensioner_id: String,
    val pensioner_name: String
)

@Serializable
data class AcceptRequestDto(
    val volunteer_id: String
)

@Serializable
data class CompleteRequestDto(
    val rating: Int
)

@Serializable
data class TransactionDto(
    val id: String,
    val amount: Int,
    val reason: String,
    val created_at: String
)

@Serializable
data class WalletDto(
    val balance: Int,
    val transactions: List<TransactionDto>
)

@Serializable
data class RewardDto(
    val id: String,
    val title: String,
    val category: String,
    val cost: Int
)

@Serializable
data class BadgeDto(
    val id: String,
    val title: String
)

@Serializable
data class LeaderboardDto(
    val volunteer_name: String,
    val district: String,
    val coins: Int,
    val rank_title: String,
    val badges: List<BadgeDto>
)

@Serializable
data class ProfileDto(
    val user_id: String,
    val full_name: String,
    val role: String,
    val active_requests: Int,
    val completed_requests: Int
)
