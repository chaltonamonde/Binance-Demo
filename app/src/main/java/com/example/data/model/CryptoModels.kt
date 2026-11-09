package com.example.data.model

enum class OrderSide {
    BUY, SELL
}

enum class OrderType {
    LIMIT, MARKET, STOP_LIMIT
}

enum class OrderStatus {
    OPEN, FILLED, CANCELED
}

data class CryptoTicker(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String = "USDT",
    val name: String,
    val lastPrice: Double,
    val priceChange: Double,
    val priceChangePercent: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val volume: Double,
    val quoteVolume: Double,
    val sparkline: List<Double> = emptyList()
) {
    val isPositive: Boolean get() = priceChangePercent >= 0
}

data class OrderBookEntry(
    val price: Double,
    val amount: Double,
    val total: Double,
    val percentDepth: Float = 0f
)

data class OrderBook(
    val symbol: String,
    val asks: List<OrderBookEntry>, // Sell orders (Red)
    val bids: List<OrderBookEntry>, // Buy orders (Green)
    val lastPrice: Double = 0.0,
    val spread: Double = 0.0
)

data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
) {
    val isGreen: Boolean get() = close >= open
}

data class MarketTrade(
    val id: String,
    val price: Double,
    val amount: Double,
    val time: Long,
    val isBuyerMaker: Boolean
)

data class TradeOrder(
    val id: String,
    val symbol: String,
    val side: OrderSide,
    val type: OrderType,
    val price: Double,
    val amount: Double,
    val totalUsd: Double,
    val filled: Double,
    val status: OrderStatus,
    val timestamp: Long
)

data class WalletAsset(
    val symbol: String,
    val name: String,
    val freeAmount: Double,
    val lockedAmount: Double = 0.0,
    val usdValue: Double = 0.0,
    val change24h: Double = 0.0
) {
    val totalAmount: Double get() = freeAmount + lockedAmount
}

data class WalletAddress(
    val network: String,
    val chainName: String,
    val address: String,
    val memo: String? = null,
    val contractType: String = "BEP-20"
)

data class WalletTransaction(
    val id: String,
    val type: String, // "Deposit", "Withdraw", "Spot Buy", "Spot Sell", "Transfer"
    val asset: String,
    val amount: Double,
    val status: String = "Completed",
    val timestamp: Long,
    val txHash: String,
    val network: String = "Binance Smart Chain (BEP20)"
)
