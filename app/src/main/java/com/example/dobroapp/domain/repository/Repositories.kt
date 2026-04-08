package com.example.dobroapp.domain.repository

import com.example.dobroapp.domain.model.CoinTransaction
import com.example.dobroapp.domain.model.HelpRequest
import com.example.dobroapp.domain.model.LeaderboardEntry
import com.example.dobroapp.domain.model.ProfileSummary
import com.example.dobroapp.domain.model.RewardItem
import com.example.dobroapp.domain.model.UserRole
import com.example.dobroapp.domain.model.UserSession

interface RequestRepository {
    suspend fun getMyRequests(role: UserRole, userId: String): List<HelpRequest>
    suspend fun getOpenRequests(district: String?): List<HelpRequest>
    suspend fun createRequest(request: HelpRequest)
    suspend fun acceptRequest(requestId: String, volunteerId: String)
    suspend fun startRequest(requestId: String, volunteerId: String)
    suspend fun completeRequest(requestId: String, rating: Int)
}

interface WalletRepository {
    suspend fun getBalance(userId: String): Int
    suspend fun getTransactions(userId: String): List<CoinTransaction>
}

interface RewardsRepository {
    suspend fun getRewards(): List<RewardItem>
}

interface LeaderboardRepository {
    suspend fun getLeaderboard(): List<LeaderboardEntry>
}

interface AuthRepository {
    suspend fun signIn(role: UserRole, fullName: String): UserSession
}

interface ProfileRepository {
    suspend fun getProfile(role: UserRole, userId: String): ProfileSummary
}
