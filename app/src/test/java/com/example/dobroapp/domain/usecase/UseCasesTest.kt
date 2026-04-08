package com.example.dobroapp.domain.usecase

import com.example.dobroapp.domain.model.HelpType
import org.junit.Assert.assertEquals
import org.junit.Test

class UseCasesTest {
    private val calculateCoinsUseCase = CalculateCoinsUseCase()
    private val resolveRankUseCase = ResolveRankUseCase()

    @Test
    fun `coins are limited by business range`() {
        val low = calculateCoinsUseCase(HelpType.Other, 1)
        val high = calculateCoinsUseCase(HelpType.Housework, 5)

        assertEquals(10, low)
        assertEquals(60, high)
    }

    @Test
    fun `rank resolves by coin thresholds`() {
        assertEquals("Новичок добра", resolveRankUseCase(200))
        assertEquals("Наставник", resolveRankUseCase(700))
        assertEquals("Герой района", resolveRankUseCase(1500))
    }
}
