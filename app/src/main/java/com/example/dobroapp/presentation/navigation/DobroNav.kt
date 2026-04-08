package com.example.dobroapp.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dobroapp.R
import com.example.dobroapp.domain.model.HelpRequest
import com.example.dobroapp.domain.model.HelpType
import com.example.dobroapp.domain.model.ProfileSummary
import com.example.dobroapp.domain.model.RequestStatus
import com.example.dobroapp.domain.model.UserRole
import com.example.dobroapp.presentation.auth.AuthViewModel
import com.example.dobroapp.presentation.leaderboard.LeaderboardViewModel
import com.example.dobroapp.presentation.profile.ProfileViewModel
import com.example.dobroapp.presentation.requests.RequestsViewModel
import com.example.dobroapp.presentation.rewards.RewardsViewModel
import com.example.dobroapp.presentation.wallet.WalletViewModel
import com.example.dobroapp.ui.theme.BeigeSoft
import com.example.dobroapp.ui.theme.DeepGreen
import com.example.dobroapp.ui.theme.ForestGreen
import org.koin.androidx.compose.koinViewModel

private object Routes {
    const val Role = "role"
    const val Pensioner = "pensioner/dashboard"
    const val Volunteer = "volunteer/dashboard"
    const val Wallet = "wallet"
    const val Rewards = "rewards"
    const val Leaderboard = "leaderboard"
    const val Profile = "profile"
    const val Rate = "rate/{requestId}"
    const val RateTemplate = "rate/%s"
}

@Composable
fun DobroAppRoot() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val requestsViewModel: RequestsViewModel = koinViewModel()
    val walletViewModel: WalletViewModel = koinViewModel()
    val rewardsViewModel: RewardsViewModel = koinViewModel()
    val leaderboardViewModel: LeaderboardViewModel = koinViewModel()
    val profileViewModel: ProfileViewModel = koinViewModel()
    val session by authViewModel.session.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.errorMessage.collectAsState()

    LaunchedEffect(session) {
        session?.let {
            requestsViewModel.bindSession(it.role, it.userId, it.fullName)
            walletViewModel.bindUser(it.userId)
            rewardsViewModel.refresh()
            leaderboardViewModel.refresh()
            profileViewModel.load(it.role, it.userId)
        }
    }

    NavHost(navController = navController, startDestination = Routes.Role) {
        composable(Routes.Role) {
            RoleScreen(
                onRoleSelected = { role, fullName -> authViewModel.signIn(role, fullName) },
                sessionRole = session?.role,
                isLoading = isLoading,
                errorMessage = authError
            ) { role ->
                navController.navigate(if (role == UserRole.Pensioner) Routes.Pensioner else Routes.Volunteer) {
                    launchSingleTop = true
                    popUpTo(Routes.Role) { inclusive = false }
                }
            }
        }
        composable(Routes.Pensioner) {
            PensionerScreen(
                vm = requestsViewModel,
                onBack = {
                    authViewModel.signOut()
                    navController.navigate(Routes.Role) {
                        launchSingleTop = true
                        popUpTo(Routes.Role) { inclusive = true }
                    }
                },
                onLeaderboard = { navController.navigate(Routes.Leaderboard) },
                onProfile = { navController.navigate(Routes.Profile) },
                onRate = { requestId -> navController.navigate(Routes.RateTemplate.format(requestId)) }
            )
        }
        composable(Routes.Volunteer) {
            VolunteerScreen(
                vm = requestsViewModel,
                onBack = {
                    authViewModel.signOut()
                    navController.navigate(Routes.Role) {
                        launchSingleTop = true
                        popUpTo(Routes.Role) { inclusive = true }
                    }
                },
                onWallet = { navController.navigate(Routes.Wallet) },
                onRewards = { navController.navigate(Routes.Rewards) },
                onLeaderboard = { navController.navigate(Routes.Leaderboard) },
                onProfile = { navController.navigate(Routes.Profile) }
            )
        }
        composable(Routes.Wallet) {
            WalletScreen(walletViewModel, navController)
        }
        composable(Routes.Rewards) {
            RewardsScreen(rewardsViewModel, navController)
        }
        composable(Routes.Leaderboard) {
            LeaderboardScreen(leaderboardViewModel, navController)
        }
        composable(Routes.Profile) {
            ProfileScreen(profileViewModel, navController)
        }
        composable(
            route = Routes.Rate,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) { entry ->
            val requestId = entry.arguments?.getString("requestId").orEmpty()
            RateDialog(
                onDismiss = { navController.popBackStack() },
                onConfirm = { rating ->
                    requestsViewModel.completeRequest(requestId, rating)
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun RoleScreen(
    onRoleSelected: (UserRole, String) -> Unit,
    sessionRole: UserRole?,
    isLoading: Boolean,
    errorMessage: String?,
    onOpenDashboard: (UserRole) -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(sessionRole) {
        sessionRole?.let(onOpenDashboard)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BeigeSoft, ForestGreen.copy(alpha = 0.25f))))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.label_sign_in_hint))
            }
        }
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text(stringResource(R.string.label_full_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        if (!errorMessage.isNullOrBlank()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { onRoleSelected(UserRole.Pensioner, fullName) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_pensioner))
        }
        Button(
            onClick = { onRoleSelected(UserRole.Volunteer, fullName) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_volunteer))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.label_premium_hint),
            color = DeepGreen,
            style = MaterialTheme.typography.bodySmall
        )
        if (isLoading) {
            Text("Подключение к серверу...")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PensionerScreen(
    vm: RequestsViewModel,
    onBack: () -> Unit,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit,
    onRate: (String) -> Unit
) {
    val requests by vm.myRequests.collectAsState()
    var title by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("Центральный") }
    var address by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }
    var rewardCoinsText by rememberSaveable { mutableStateOf("30") }
    var type by rememberSaveable { mutableStateOf(HelpType.Groceries) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_pensioner),
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                NavigationActions(
                    onWallet = null,
                    onRewards = null,
                    onLeaderboard = onLeaderboard,
                    onProfile = onProfile
                )
            }
            item {
                Text(stringResource(R.string.btn_create_request), style = MaterialTheme.typography.titleMedium)
            }
            item {
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.label_title)) }, modifier = Modifier.fillMaxWidth())
            }
            item { OutlinedTextField(district, { district = it }, label = { Text(stringResource(R.string.label_district)) }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(address, { address = it }, label = { Text(stringResource(R.string.label_address)) }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(time, { time = it }, label = { Text(stringResource(R.string.label_time)) }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(comment, { comment = it }, label = { Text(stringResource(R.string.label_comment)) }, modifier = Modifier.fillMaxWidth()) }
            item {
                OutlinedTextField(
                    value = rewardCoinsText,
                    onValueChange = { rewardCoinsText = it.filter(Char::isDigit).take(4) },
                    label = { Text(stringResource(R.string.label_reward_coins)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HelpType.entries) { chipType ->
                        FilterChip(
                            selected = chipType == type,
                            onClick = { type = chipType },
                            label = { Text(chipType.title) }
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        vm.createRequest(
                            title = title,
                            rewardCoins = rewardCoinsText.toIntOrNull() ?: 30,
                            district = district,
                            address = address,
                            time = time,
                            comment = comment,
                            helpType = type
                        )
                        title = ""
                        address = ""
                        time = ""
                        comment = ""
                    },
                    enabled = title.isNotBlank() && address.isNotBlank() && time.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.btn_publish_request)) }
            }
            item { Text(stringResource(R.string.label_my_requests), style = MaterialTheme.typography.titleMedium) }
            if (requests.isEmpty()) {
                item { Text(stringResource(R.string.msg_empty)) }
            } else {
                items(requests) { request ->
                    RequestCard(
                        request = request,
                        primaryLabel = if (request.status == RequestStatus.InProgress) stringResource(R.string.btn_complete_request) else null,
                        onPrimaryClick = {
                            if (request.status == RequestStatus.InProgress) onRate(request.id)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VolunteerScreen(
    vm: RequestsViewModel,
    onBack: () -> Unit,
    onWallet: () -> Unit,
    onRewards: () -> Unit,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit
) {
    val openRequests by vm.openRequests.collectAsState()
    val acceptedRequests by vm.acceptedByMe.collectAsState()
    val districts = listOf(stringResource(R.string.district_all), "Центральный", "Северный", "Южный")
    var selected by rememberSaveable { mutableStateOf(districts.first()) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_volunteer),
                onBack = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                NavigationActions(
                    onWallet = onWallet,
                    onRewards = onRewards,
                    onLeaderboard = onLeaderboard,
                    onProfile = onProfile
                )
            }
            item {
                Text(stringResource(R.string.label_filter_district), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(districts) { district ->
                        FilterChip(
                            selected = selected == district,
                            onClick = {
                                selected = district
                                vm.setDistrictFilter(if (district == districts.first()) null else district)
                            },
                            label = { Text(district) }
                        )
                    }
                }
            }
            item { Text(stringResource(R.string.label_accepted_requests), style = MaterialTheme.typography.titleMedium) }
            if (acceptedRequests.isEmpty()) {
                item { Text(stringResource(R.string.msg_empty)) }
            } else {
                items(acceptedRequests) { request ->
                    val label = if (request.status == RequestStatus.Accepted) {
                        stringResource(R.string.btn_start_request)
                    } else null
                    RequestCard(
                        request = request,
                        primaryLabel = label,
                        onPrimaryClick = {
                            if (request.status == RequestStatus.Accepted) vm.startRequest(request.id)
                        }
                    )
                }
            }
            item { Text(stringResource(R.string.label_feed), style = MaterialTheme.typography.titleMedium) }
            if (openRequests.isEmpty()) {
                item { Text(stringResource(R.string.msg_empty)) }
            } else {
                items(openRequests) { request ->
                    RequestCard(
                        request = request,
                        primaryLabel = stringResource(R.string.btn_accept_request),
                        onPrimaryClick = { vm.acceptRequest(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: HelpRequest,
    primaryLabel: String?,
    onPrimaryClick: (() -> Unit)?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(request.title, style = MaterialTheme.typography.titleSmall)
            Text("${request.district} • ${request.time}")
            Text(stringResource(R.string.label_type, request.helpType.title))
            Text(stringResource(R.string.label_reward_coins_value, request.rewardCoins))
            Text(stringResource(R.string.label_status, request.status.toRuStatus()))
            if (!primaryLabel.isNullOrBlank() && onPrimaryClick != null) {
                Button(
                    onClick = onPrimaryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(primaryLabel, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun NavigationActions(
    onWallet: (() -> Unit)?,
    onRewards: (() -> Unit)?,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onWallet != null) {
            item { Button(onClick = onWallet) { Text(stringResource(R.string.btn_wallet)) } }
        }
        if (onRewards != null) {
            item { Button(onClick = onRewards) { Text(stringResource(R.string.btn_rewards)) } }
        }
        item { Button(onClick = onLeaderboard) { Text(stringResource(R.string.btn_leaderboard)) } }
        item { Button(onClick = onProfile) { Text(stringResource(R.string.btn_profile)) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletScreen(vm: WalletViewModel, navController: NavHostController) {
    val balance by vm.balance.collectAsState()
    val rank by vm.rank.collectAsState()
    val transactions by vm.transactions.collectAsState()
    Scaffold(topBar = { AppTopBar(title = stringResource(R.string.title_wallet), onBack = { navController.popBackStack() }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text(stringResource(R.string.label_balance, balance), style = MaterialTheme.typography.headlineSmall) }
            item { Text(stringResource(R.string.label_rank, rank)) }
            if (transactions.isEmpty()) {
                item { Text(stringResource(R.string.msg_empty)) }
            } else {
                items(transactions) { tx ->
                    Card {
                        Column(Modifier.padding(12.dp)) {
                            Text("${tx.amount} монет")
                            Text(tx.reason)
                            Text(tx.createdAt)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RewardsScreen(vm: RewardsViewModel, navController: NavHostController) {
    val rewards by vm.rewards.collectAsState()
    Scaffold(topBar = { AppTopBar(title = stringResource(R.string.title_rewards), onBack = { navController.popBackStack() }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rewards) { reward ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(reward.title, style = MaterialTheme.typography.titleSmall)
                        Text("${reward.category} • ${reward.cost} монет")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaderboardScreen(vm: LeaderboardViewModel, navController: NavHostController) {
    val items by vm.items.collectAsState()
    Scaffold(topBar = { AppTopBar(title = stringResource(R.string.title_leaderboard), onBack = { navController.popBackStack() }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(entry.volunteerName, style = MaterialTheme.typography.titleSmall)
                        Text("${entry.district} • ${entry.coins} монет")
                        Text(entry.rankTitle)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(vm: ProfileViewModel, navController: NavHostController) {
    val profile by vm.profile.collectAsState()
    Scaffold(topBar = { AppTopBar(title = stringResource(R.string.title_profile), onBack = { navController.popBackStack() }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profile?.let { value ->
                ProfileCard(value)
            } ?: Text(stringResource(R.string.msg_empty))
        }
    }
}

@Composable
private fun ProfileCard(profile: ProfileSummary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(profile.fullName, style = MaterialTheme.typography.titleLarge)
            Text(if (profile.role == UserRole.Pensioner) "Пенсионер" else "Волонтер")
            Text(stringResource(R.string.label_active_requests, profile.activeRequests))
            Text(stringResource(R.string.label_completed_requests, profile.completedRequests))
        }
    }
}

@Composable
private fun RateDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var rating by rememberSaveable { mutableIntStateOf(5) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_rate_request)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Выберите оценку выполнения: $rating")
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt().coerceIn(1, 5) },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(rating) }) {
                Text(stringResource(R.string.btn_send_rating))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_back))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Text("<")
            }
        }
    )
}

private fun RequestStatus.toRuStatus(): String = when (this) {
    RequestStatus.Open -> "Открыта"
    RequestStatus.Accepted -> "Принята"
    RequestStatus.InProgress -> "В работе"
    RequestStatus.Completed -> "Завершена"
}
