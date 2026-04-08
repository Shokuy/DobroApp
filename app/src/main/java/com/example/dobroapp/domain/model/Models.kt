package com.example.dobroapp.domain.model

enum class UserRole {
    Pensioner,
    Volunteer
}

enum class HelpType(val title: String, val baseCoins: Int) {
    Groceries("Продукты", 20),
    Pharmacy("Лекарства", 30),
    Housework("Помощь по дому", 40),
    Walk("Прогулка и сопровождение", 25),
    Other("Другое", 15)
}

enum class RequestStatus {
    Open,
    Accepted,
    InProgress,
    Completed
}

data class UserSession(
    val userId: String,
    val role: UserRole,
    val fullName: String,
    val token: String
)

data class GeoPoint(
    val lat: Double,
    val lon: Double
)

data class HelpRequest(
    val id: String,
    val title: String,
    val helpType: HelpType,
    val rewardCoins: Int,
    val district: String,
    val address: String,
    val time: String,
    val comment: String,
    val pensionerName: String,
    val pensionerId: String,
    val status: RequestStatus,
    val volunteerName: String? = null,
    val volunteerId: String? = null,
    val rating: Int? = null,
    val location: GeoPoint? = null
)

data class CoinTransaction(
    val id: String,
    val amount: Int,
    val reason: String,
    val createdAt: String
)

data class RewardItem(
    val id: String,
    val title: String,
    val category: String,
    val cost: Int
)

data class Badge(
    val id: String,
    val title: String
)

data class LeaderboardEntry(
    val volunteerName: String,
    val district: String,
    val coins: Int,
    val rankTitle: String,
    val badges: List<Badge>
)

data class ProfileSummary(
    val userId: String,
    val fullName: String,
    val role: UserRole,
    val activeRequests: Int,
    val completedRequests: Int
)
