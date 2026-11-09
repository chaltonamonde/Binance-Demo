package com.example

import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.repository.BinanceRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test spot buy order updates wallet balance`() {
        val repository = BinanceRepository()
        val initialUsdt = repository.walletAssets.value.find { it.symbol == "USDT" }?.freeAmount ?: 0.0
        assertTrue("Initial USDT should be positive", initialUsdt > 1000.0)

        val result = repository.placeOrder(
            symbol = "BTCUSDT",
            side = OrderSide.BUY,
            type = OrderType.MARKET,
            price = 90000.0,
            amount = 0.01
        )

        assertTrue("Order placement should succeed", result.isSuccess)
        val order = result.getOrNull()
        assertNotNull(order)
        assertEquals(OrderStatus.FILLED, order?.status)

        val updatedUsdt = repository.walletAssets.value.find { it.symbol == "USDT" }?.freeAmount ?: 0.0
        assertEquals(initialUsdt - 900.0, updatedUsdt, 0.001)
    }

    @Test
    fun `test spot sell order credits USDT`() {
        val repository = BinanceRepository()
        val initialBtc = repository.walletAssets.value.find { it.symbol == "BTC" }?.freeAmount ?: 0.0
        val initialUsdt = repository.walletAssets.value.find { it.symbol == "USDT" }?.freeAmount ?: 0.0

        val result = repository.placeOrder(
            symbol = "BTCUSDT",
            side = OrderSide.SELL,
            type = OrderType.LIMIT,
            price = 95000.0,
            amount = 0.10
        )

        assertTrue("Sell order should succeed", result.isSuccess)
        val updatedBtc = repository.walletAssets.value.find { it.symbol == "BTC" }?.freeAmount ?: 0.0
        val updatedUsdt = repository.walletAssets.value.find { it.symbol == "USDT" }?.freeAmount ?: 0.0

        assertEquals(initialBtc - 0.10, updatedBtc, 0.0001)
        assertEquals(initialUsdt + 9500.0, updatedUsdt, 0.001)
    }

    @Test
    fun `test wallet deposit simulation`() {
        val repository = BinanceRepository()
        val initialUsdt = repository.walletAssets.value.find { it.symbol == "USDT" }?.freeAmount ?: 0.0

        val result = repository.depositFunds("USDT", 2500.0, "Binance Smart Chain (BEP20)")
        assertTrue(result.isSuccess)

        val updatedUsdt = repository.walletAssets.value.find { it.symbol == "USDT" }?.freeAmount ?: 0.0
        assertEquals(initialUsdt + 2500.0, updatedUsdt, 0.001)
    }
}
