package com.example.dobroapp.data.repository

import com.example.dobroapp.data.api.SessionStore
import com.example.dobroapp.data.dto.AcceptRequestDto
import com.example.dobroapp.data.dto.AuthRequestDto
import com.example.dobroapp.data.dto.AuthResponseDto
import com.example.dobroapp.data.dto.CompleteRequestDto
import com.example.dobroapp.data.dto.CreateRequestDto
import com.example.dobroapp.data.dto.LeaderboardDto
import com.example.dobroapp.data.dto.ProfileDto
import com.example.dobroapp.data.dto.RequestDto
import com.example.dobroapp.data.dto.RewardDto
import com.example.dobroapp.data.dto.WalletDto
import com.example.dobroapp.domain.model.Badge
import com.example.dobroapp.domain.model.CoinTransaction
import com.example.dobroapp.domain.model.GeoPoint
import com.example.dobroapp.domain.model.HelpRequest
import com.example.dobroapp.domain.model.HelpType
import com.example.dobroapp.domain.model.LeaderboardEntry
import com.example.dobroapp.domain.model.ProfileSummary
import com.example.dobroapp.domain.model.RequestStatus
import com.example.dobroapp.domain.model.RewardItem
import com.example.dobroapp.domain.model.UserRole
import com.example.dobroapp.domain.model.UserSession
import com.example.dobroapp.domain.repository.AuthRepository
import com.example.dobroapp.domain.repository.LeaderboardRepository
import com.example.dobroapp.domain.repository.ProfileRepository
import com.example.dobroapp.domain.repository.RequestRepository
import com.example.dobroapp.domain.repository.RewardsRepository
import com.example.dobroapp.domain.repository.WalletRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class ApiAuthRepository(
    private val httpClient: HttpClient,
    private val sessionStore: SessionStore
) : AuthRepository {
    override suspend fun signIn(role: UserRole, fullName: String): UserSession {
        val response = httpClient.post("auth/role") {
            setBody(
                AuthRequestDto(
                    role = role.name.lowercase(),
                    full_name = fullName
                )
            )
        }.body<AuthResponseDto>()
        val session = response.toDomain()
        sessionStore.token = session.token
        sessionStore.userId = session.userId
        return session
    }
}

class ApiRequestRepository(
    private val httpClient: HttpClient
) : RequestRepository {
    override suspend fun getMyRequests(role: UserRole, userId: String): List<HelpRequest> {
        return httpClient.get("requests/me") {
            parameter("role", role.name.lowercase())
            parameter("user_id", userId)
        }.body<List<RequestDto>>().map { it.toDomain() }
    }

    override suspend fun getOpenRequests(district: String?): List<HelpRequest> {
        return httpClient.get("requests/open") {
            if (!district.isNullOrBlank()) parameter("district", district)
        }.body<List<RequestDto>>().map { it.toDomain() }
    }

    override suspend fun createRequest(request: HelpRequest) {
        httpClient.post("requests") {
            setBody(
                CreateRequestDto(
                    title = request.title,
                    help_type = request.helpType.name.lowercase(),
                    reward_coins = request.rewardCoins,
                    district = request.district,
                    address = request.address,
                    time = request.time,
                    comment = request.comment,
                    pensioner_id = request.pensionerId,
                    pensioner_name = request.pensionerName
                )
            )
        }
    }

    override suspend fun acceptRequest(requestId: String, volunteerId: String) {
        httpClient.post("requests/$requestId/accept") {
            setBody(AcceptRequestDto(volunteer_id = volunteerId))
        }
    }

    override suspend fun startRequest(requestId: String, volunteerId: String) {
        httpClient.post("requests/$requestId/start") {
            setBody(AcceptRequestDto(volunteer_id = volunteerId))
        }
    }

    override suspend fun completeRequest(requestId: String, rating: Int) {
        httpClient.post("requests/$requestId/complete") {
            setBody(CompleteRequestDto(rating = rating))
        }
    }
}

class ApiWalletRepository(
    private val httpClient: HttpClient
) : WalletRepository {
    override suspend fun getBalance(userId: String): Int {
        return httpClient.get("wallet/$userId").body<WalletDto>().balance
    }

    override suspend fun getTransactions(userId: String): List<CoinTransaction> {
        return httpClient.get("wallet/$userId").body<WalletDto>().transactions.map {
            CoinTransaction(it.id, it.amount, it.reason, it.created_at)
        }
    }
}

class ApiRewardsRepository(
    private val httpClient: HttpClient
) : RewardsRepository {
    override suspend fun getRewards(): List<RewardItem> {
        return httpClient.get("rewards").body<List<RewardDto>>().map {
            RewardItem(it.id, it.title, it.category, it.cost)
        }
    }
}

class ApiLeaderboardRepository(
    private val httpClient: HttpClient
) : LeaderboardRepository {
    override suspend fun getLeaderboard(): List<LeaderboardEntry> {
        return httpClient.get("leaderboard").body<List<LeaderboardDto>>().map {
            LeaderboardEntry(
                volunteerName = it.volunteer_name,
                district = it.district,
                coins = it.coins,
                rankTitle = it.rank_title,
                badges = it.badges.map { badge -> Badge(badge.id, badge.title) }
            )
        }
    }
}

class ApiProfileRepository(
    private val httpClient: HttpClient
) : ProfileRepository {
    override suspend fun getProfile(role: UserRole, userId: String): ProfileSummary {
        val endpoint = if (role == UserRole.Pensioner) "profiles/pensioner/$userId" else "profiles/volunteer/$userId"
        val dto = httpClient.get(endpoint).body<ProfileDto>()
        return ProfileSummary(
            userId = dto.user_id,
            fullName = dto.full_name,
            role = dto.role.toRole(),
            activeRequests = dto.active_requests,
            completedRequests = dto.completed_requests
        )
    }
}

private fun AuthResponseDto.toDomain(): UserSession {
    return UserSession(
        userId = user_id,
        role = role.toRole(),
        fullName = full_name,
        token = token
    )
}

private fun RequestDto.toDomain(): HelpRequest {
    return HelpRequest(
        id = id,
        title = title,
        helpType = help_type.toHelpType(),
        rewardCoins = reward_coins,
        district = district,
        address = address,
        time = time,
        comment = comment,
        pensionerName = pensioner_name,
        pensionerId = pensioner_id,
        status = status.toStatus(),
        volunteerName = volunteer_name,
        volunteerId = volunteer_id,
        rating = rating,
        location = if (lat != null && lon != null) GeoPoint(lat, lon) else null
    )
}

private fun String.toHelpType(): HelpType = when (lowercase()) {
    "groceries" -> HelpType.Groceries
    "pharmacy" -> HelpType.Pharmacy
    "housework" -> HelpType.Housework
    "walk" -> HelpType.Walk
    else -> HelpType.Other
}

private fun String.toStatus(): RequestStatus = when (lowercase()) {
    "accepted" -> RequestStatus.Accepted
    "inprogress" -> RequestStatus.InProgress
    "completed" -> RequestStatus.Completed
    else -> RequestStatus.Open
}

private fun String.toRole(): UserRole = if (lowercase() == "volunteer") UserRole.Volunteer else UserRole.Pensioner
