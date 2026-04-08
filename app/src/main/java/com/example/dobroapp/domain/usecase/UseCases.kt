package com.example.dobroapp.domain.usecase

import com.example.dobroapp.domain.model.HelpType
import com.example.dobroapp.domain.model.RequestStatus
import kotlin.math.roundToInt

class CalculateCoinsUseCase {
    operator fun invoke(helpType: HelpType, rating: Int): Int {
        val safeRating = rating.coerceIn(1, 5)
        val multiplier = when (safeRating) {
            1 -> 0.5
            2 -> 0.8
            3 -> 1.0
            4 -> 1.2
            else -> 1.5
        }
        return (helpType.baseCoins * multiplier).roundToInt().coerceIn(10, 100)
    }
}

class ResolveRankUseCase {
    operator fun invoke(totalCoins: Int): String = when {
        totalCoins >= 1000 -> "Герой района"
        totalCoins >= 500 -> "Наставник"
        else -> "Новичок добра"
    }
}

class CanVolunteerSeeRequestUseCase {
    operator fun invoke(status: RequestStatus): Boolean = status == RequestStatus.Open
}
