package com.example.dobroapp.di

import com.example.dobroapp.data.api.SessionStore
import com.example.dobroapp.data.api.provideHttpClient
import com.example.dobroapp.data.repository.ApiAuthRepository
import com.example.dobroapp.data.repository.ApiLeaderboardRepository
import com.example.dobroapp.data.repository.ApiProfileRepository
import com.example.dobroapp.data.repository.ApiRequestRepository
import com.example.dobroapp.data.repository.ApiRewardsRepository
import com.example.dobroapp.data.repository.ApiWalletRepository
import com.example.dobroapp.domain.repository.AuthRepository
import com.example.dobroapp.domain.repository.LeaderboardRepository
import com.example.dobroapp.domain.repository.ProfileRepository
import com.example.dobroapp.domain.repository.RequestRepository
import com.example.dobroapp.domain.repository.RewardsRepository
import com.example.dobroapp.domain.repository.WalletRepository
import com.example.dobroapp.domain.usecase.CalculateCoinsUseCase
import com.example.dobroapp.domain.usecase.ResolveRankUseCase
import com.example.dobroapp.presentation.auth.AuthViewModel
import com.example.dobroapp.presentation.leaderboard.LeaderboardViewModel
import com.example.dobroapp.presentation.profile.ProfileViewModel
import com.example.dobroapp.presentation.requests.RequestsViewModel
import com.example.dobroapp.presentation.rewards.RewardsViewModel
import com.example.dobroapp.presentation.wallet.WalletViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CalculateCoinsUseCase() }
    single { ResolveRankUseCase() }

    single { SessionStore() }
    single { provideHttpClient(get()) }
    single<AuthRepository> { ApiAuthRepository(get(), get()) }
    single<WalletRepository> { ApiWalletRepository(get()) }
    single<RequestRepository> { ApiRequestRepository(get()) }
    single<RewardsRepository> { ApiRewardsRepository(get()) }
    single<LeaderboardRepository> { ApiLeaderboardRepository(get()) }
    single<ProfileRepository> { ApiProfileRepository(get()) }

    viewModel { AuthViewModel(get()) }
    viewModel { RequestsViewModel(get()) }
    viewModel { WalletViewModel(get(), get()) }
    viewModel { RewardsViewModel(get()) }
    viewModel { LeaderboardViewModel(get(), get()) }
    viewModel { ProfileViewModel(get()) }
}
