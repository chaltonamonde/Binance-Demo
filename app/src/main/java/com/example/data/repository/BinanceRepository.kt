package com.example.data.repository

import com.example.data.model.Candle
import com.example.data.model.CryptoTicker
import com.example.data.model.MarketTrade
import com.example.data.model.OrderBook
import com.example.data.model.OrderBookEntry
import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.TradeOrder
import com.example.data.model.WalletAddress
import com.example.data.model.WalletAsset
import com.example.data.model.WalletTransaction
import com.example.data.network.BinanceApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BinanceRepository(
    private val apiClient: BinanceApiClient = BinanceApiClient()
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    // Market State
    private val _tickers = MutableStateFlow<List<CryptoTicker>>(apiClient.getFallbackTickers())
    val tickers: StateFlow<List<CryptoTicker>> = _tickers.asStateFlow()

    private val _selectedSymbol = MutableStateFlow("BTCUSDT")
    val selectedSymbol: StateFlow<String> = _selectedSymbol.asStateFlow()

    private val _orderBook = MutableStateFlow(apiClient.generateFallbackOrderBook("BTCUSDT", 92450.0))
    val orderBook: StateFlow<OrderBook> = _orderBook.asStateFlow()

    private val _candles = MutableStateFlow<List<Candle>>(apiClient.generateFallbackCandles(92450.0))
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

    private val _recentTrades = MutableStateFlow<List<MarketTrade>>(apiClient.generateFallbackTrades(92450.0))
    val recentTrades: StateFlow<List<MarketTrade>> = _recentTrades.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("15m")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _favorites = MutableStateFlow(setOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT"))
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // Wallet State
    private val _walletAssets = MutableStateFlow(
        listOf(
            WalletAsset(symbol = "USDT", name = "Tether USD", freeAmount = 18500.0, lockedAmount = 500.0, usdValue = 19000.0, change24h = 0.0),
            WalletAsset(symbol = "BTC", name = "Bitcoin", freeAmount = 0.285, lockedAmount = 0.0, usdValue = 26348.25, change24h = 4.35),
            WalletAsset(symbol = "ETH", name = "Ethereum", freeAmount = 2.40, lockedAmount = 0.0, usdValue = 8208.0, change24h = 2.82),
            WalletAsset(symbol = "BNB", name = "BNB", freeAmount = 14.5, lockedAmount = 0.0, usdValue = 9352.5, change24h = 5.12),
            WalletAsset(symbol = "SOL", name = "Solana", freeAmount = 25.0, lockedAmount = 0.0, usdValue = 5450.0, change24h = 8.45),
            WalletAsset(symbol = "DOGE", name = "Dogecoin", freeAmount = 3500.0, lockedAmount = 0.0, usdValue = 1330.0, change24h = -1.25),
            WalletAsset(symbol = "SUI", name = "Sui", freeAmount = 800.0, lockedAmount = 0.0, usdValue = 2680.0, change24h = 12.40)
        )
    )
    val walletAssets: StateFlow<List<WalletAsset>> = _walletAssets.asStateFlow()

    private val _fundingBalances = MutableStateFlow(
        mapOf("USDT" to 2500.0, "BNB" to 2.0, "BTC" to 0.015)
    )
    val fundingBalances: StateFlow<Map<String, Double>> = _fundingBalances.asStateFlow()

    private val _earnBalances = MutableStateFlow(
        mapOf("USDT" to 10000.0, "BNB" to 5.0, "ETH" to 0.5)
    )
    val earnBalances: StateFlow<Map<String, Double>> = _earnBalances.asStateFlow()

    // Multi-chain Wallet Addresses
    val walletAddresses = listOf(
        WalletAddress(network = "BNB Smart Chain (BEP20)", chainName = "BSC", address = "0x71C6793F1646271EbA08d95B309A44eB2bE84982", contractType = "BEP-20"),
        WalletAddress(network = "Ethereum (ERC20)", chainName = "Ethereum", address = "0x71C6793F1646271EbA08d95B309A44eB2bE84982", contractType = "ERC-20"),
        WalletAddress(network = "Bitcoin", chainName = "Bitcoin", address = "bc1q9u30s0x43k9p8z6q4l2w4f8m9n7y5x3w2p8q1", contractType = "Native SegWit"),
        WalletAddress(network = "Solana", chainName = "Solana", address = "7vE4K3gR8yW2eP6Q1mL5zT9Yx1dC4bA8fS0nK2mP9qZ", contractType = "SPL"),
        WalletAddress(network = "Tron (TRC20)", chainName = "Tron", address = "TVy7Z4dE9rF2mK8bQ5xW1pT6nC3yL9aB8x", contractType = "TRC-20")
    )

    // Orders & Transactions
    private val _orders = MutableStateFlow(
        listOf(
            TradeOrder(
                id = "ORD-94812",
                symbol = "BTCUSDT",
                side = OrderSide.BUY,
                type = OrderType.LIMIT,
                price = 91200.0,
                amount = 0.05,
                totalUsd = 4560.0,
                filled = 0.0,
                status = OrderStatus.OPEN,
                timestamp = System.currentTimeMillis() - 45 * 60 * 1000L
            ),
            TradeOrder(
                id = "ORD-89211",
                symbol = "ETHUSDT",
                side = OrderSide.BUY,
                type = OrderType.MARKET,
                price = 3390.0,
                amount = 1.20,
                totalUsd = 4068.0,
                filled = 1.20,
                status = OrderStatus.FILLED,
                timestamp = System.currentTimeMillis() - 4 * 3600 * 1000L
            ),
            TradeOrder(
                id = "ORD-76192",
                symbol = "SOLUSDT",
                side = OrderSide.SELL,
                type = OrderType.LIMIT,
                price = 225.0,
                amount = 10.0,
                totalUsd = 2250.0,
                filled = 10.0,
                status = OrderStatus.FILLED,
                timestamp = System.currentTimeMillis() - 24 * 3600 * 1000L
            )
        )
    )
    val orders: StateFlow<List<TradeOrder>> = _orders.asStateFlow()

    private val _transactions = MutableStateFlow(
        listOf(
            WalletTransaction(
                id = "TX-882194",
                type = "Deposit",
                asset = "USDT",
                amount = 10000.0,
                status = "Completed",
                timestamp = System.currentTimeMillis() - 2 * 3600 * 1000L,
                txHash = "0x8fa4b...39e12"
            ),
            WalletTransaction(
                id = "TX-773124",
                type = "Spot Buy",
                asset = "BTC",
                amount = 0.15,
                status = "Completed",
                timestamp = System.currentTimeMillis() - 12 * 3600 * 1000L,
                txHash = "0x4b7c1...99a01"
            ),
            WalletTransaction(
                id = "TX-654129",
                type = "Withdraw",
                asset = "ETH",
                amount = 0.50,
                status = "Completed",
                timestamp = System.currentTimeMillis() - 48 * 3600 * 1000L,
                txHash = "0x1d3e8...55b20"
            )
        )
    )
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    // Security Settings
    val seedPhrase = "alpha galaxy lunar matrix orbit rocket crypto shield nebula quantum eclipse cosmic"
    val userPin = "1234"
    private val _isPinProtected = MutableStateFlow(true)
    val isPinProtected: StateFlow<Boolean> = _isPinProtected.asStateFlow()

    init {
        startRealtimeMarketEngine()
    }

    private fun startRealtimeMarketEngine() {
        // Background loop to fetch real Binance data and generate live ticks
        scope.launch {
            // Initial fetch
            refreshMarketData()

            while (isActive) {
                delay(2000L) // Refresh every 2 seconds
                try {
                    val freshTickers = apiClient.get24HrTickers()
                    _tickers.value = freshTickers

                    // Update selected symbol ticker and recalculate wallet usd values
                    val currentSym = _selectedSymbol.value
                    val currentTicker = freshTickers.find { it.symbol == currentSym }
                    if (currentTicker != null) {
                        updateOrderBookAndTrades(currentSym, currentTicker.lastPrice)
                    }

                    recalculateWalletUsdValues(freshTickers)
                } catch (_: Exception) {
                    // Apply micro-fluctuation to keep UI responsive and dynamic
                    simulateMicroFluctuation()
                }
            }
        }
    }

    suspend fun refreshMarketData() {
        val sym = _selectedSymbol.value
        val freshTickers = apiClient.get24HrTickers()
        _tickers.value = freshTickers

        val freshBook = apiClient.getOrderBook(sym, 14)
        _orderBook.value = freshBook

        val freshCandles = apiClient.getKlines(sym, mapInterval(_selectedTimeframe.value), 60)
        _candles.value = freshCandles

        val freshTrades = apiClient.getRecentTrades(sym, 15)
        _recentTrades.value = freshTrades

        recalculateWalletUsdValues(freshTickers)
    }

    private fun mapInterval(tf: String): String {
        return when (tf) {
            "Line", "15m" -> "15m"
            "1h" -> "1h"
            "4h" -> "4h"
            "1D" -> "1d"
            "1W" -> "1w"
            else -> "1h"
        }
    }

    fun selectSymbol(symbol: String) {
        _selectedSymbol.value = symbol
        scope.launch {
            val ticker = _tickers.value.find { it.symbol == symbol }
            val centerPrice = ticker?.lastPrice ?: 100.0
            _orderBook.value = apiClient.getOrderBook(symbol, 14)
            _candles.value = apiClient.getKlines(symbol, mapInterval(_selectedTimeframe.value), 60)
            _recentTrades.value = apiClient.getRecentTrades(symbol, 15)
        }
    }

    fun selectTimeframe(tf: String) {
        _selectedTimeframe.value = tf
        scope.launch {
            val sym = _selectedSymbol.value
            _candles.value = apiClient.getKlines(sym, mapInterval(tf), 60)
        }
    }

    fun toggleFavorite(symbol: String) {
        _favorites.update { favs ->
            if (favs.contains(symbol)) favs - symbol else favs + symbol
        }
    }

    private fun updateOrderBookAndTrades(symbol: String, lastPrice: Double) {
        // High frequency live order book tick
        val currentBook = _orderBook.value
        if (currentBook.symbol != symbol || currentBook.asks.isEmpty() || currentBook.bids.isEmpty()) {
            _orderBook.value = apiClient.generateFallbackOrderBook(symbol, lastPrice)
            return
        }

        // Slightly jitter the first 3 rows of bids & asks for live tick realism
        val jitterBids = currentBook.bids.mapIndexed { index, entry ->
            if (index < 3) {
                val newAmt = max(0.01, entry.amount + (Random.nextDouble() - 0.5) * 0.1)
                entry.copy(amount = newAmt)
            } else entry
        }
        val jitterAsks = currentBook.asks.mapIndexed { index, entry ->
            if (index < 3) {
                val newAmt = max(0.01, entry.amount + (Random.nextDouble() - 0.5) * 0.1)
                entry.copy(amount = newAmt)
            } else entry
        }

        _orderBook.value = currentBook.copy(
            bids = jitterBids,
            asks = jitterAsks,
            lastPrice = lastPrice
        )

        // Inject live trade
        val newTrade = MarketTrade(
            id = System.currentTimeMillis().toString(),
            price = lastPrice + (Random.nextDouble() - 0.5) * (lastPrice * 0.0004),
            amount = Random.nextDouble() * 0.5 + 0.01,
            time = System.currentTimeMillis(),
            isBuyerMaker = Random.nextBoolean()
        )
        _recentTrades.update { (listOf(newTrade) + it).take(20) }
    }

    private fun simulateMicroFluctuation() {
        _tickers.update { list ->
            list.map { ticker ->
                val delta = (Random.nextDouble() - 0.49) * (ticker.lastPrice * 0.001)
                val newPrice = max(0.000001, ticker.lastPrice + delta)
                ticker.copy(lastPrice = newPrice)
            }
        }
    }

    private fun recalculateWalletUsdValues(tickerList: List<CryptoTicker>) {
        val priceMap = tickerList.associate { it.baseAsset to it.lastPrice }.toMutableMap()
        priceMap["USDT"] = 1.0

        _walletAssets.update { assets ->
            assets.map { asset ->
                val price = priceMap[asset.symbol] ?: 1.0
                val total = asset.totalAmount
                val change = tickerList.find { it.baseAsset == asset.symbol }?.priceChangePercent ?: 0.0
                asset.copy(
                    usdValue = total * price,
                    change24h = change
                )
            }
        }
    }

    // Trading Engine Execution
    fun placeOrder(
        symbol: String,
        side: OrderSide,
        type: OrderType,
        price: Double,
        amount: Double
    ): Result<TradeOrder> {
        val totalUsd = price * amount
        val baseAsset = symbol.replace("USDT", "")
        val currentAssets = _walletAssets.value.toMutableList()

        if (side == OrderSide.BUY) {
            // Check USDT balance
            val usdtAsset = currentAssets.find { it.symbol == "USDT" }
            if (usdtAsset == null || usdtAsset.freeAmount < totalUsd) {
                return Result.failure(Exception("Insufficient USDT balance. Available: ${usdtAsset?.freeAmount ?: 0.0} USDT"))
            }

            // Deduct USDT
            val updatedUsdt = usdtAsset.copy(freeAmount = usdtAsset.freeAmount - totalUsd)
            currentAssets[currentAssets.indexOf(usdtAsset)] = updatedUsdt

            // If Market order, instantly credit baseAsset
            if (type == OrderType.MARKET || type == OrderType.LIMIT) {
                val target = currentAssets.find { it.symbol == baseAsset }
                if (target != null) {
                    currentAssets[currentAssets.indexOf(target)] = target.copy(freeAmount = target.freeAmount + amount)
                } else {
                    currentAssets.add(WalletAsset(symbol = baseAsset, name = baseAsset, freeAmount = amount))
                }
            }
        } else {
            // Sell baseAsset
            val target = currentAssets.find { it.symbol == baseAsset }
            if (target == null || target.freeAmount < amount) {
                return Result.failure(Exception("Insufficient $baseAsset balance. Available: ${target?.freeAmount ?: 0.0} $baseAsset"))
            }

            // Deduct baseAsset
            currentAssets[currentAssets.indexOf(target)] = target.copy(freeAmount = target.freeAmount - amount)

            // Credit USDT
            val usdtAsset = currentAssets.find { it.symbol == "USDT" }
            if (usdtAsset != null) {
                currentAssets[currentAssets.indexOf(usdtAsset)] = usdtAsset.copy(freeAmount = usdtAsset.freeAmount + totalUsd)
            } else {
                currentAssets.add(WalletAsset(symbol = "USDT", name = "Tether USD", freeAmount = totalUsd))
            }
        }

        _walletAssets.value = currentAssets
        recalculateWalletUsdValues(_tickers.value)

        val newOrder = TradeOrder(
            id = "ORD-${Random.nextInt(10000, 99999)}",
            symbol = symbol,
            side = side,
            type = type,
            price = price,
            amount = amount,
            totalUsd = totalUsd,
            filled = amount,
            status = OrderStatus.FILLED,
            timestamp = System.currentTimeMillis()
        )

        _orders.update { listOf(newOrder) + it }

        // Add to transactions
        val tx = WalletTransaction(
            id = "TX-${Random.nextInt(100000, 999999)}",
            type = if (side == OrderSide.BUY) "Spot Buy" else "Spot Sell",
            asset = baseAsset,
            amount = amount,
            status = "Completed",
            timestamp = System.currentTimeMillis(),
            txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16)
        )
        _transactions.update { listOf(tx) + it }

        return Result.success(newOrder)
    }

    fun cancelOrder(orderId: String) {
        _orders.update { list ->
            list.map {
                if (it.id == orderId && it.status == OrderStatus.OPEN) {
                    it.copy(status = OrderStatus.CANCELED)
                } else it
            }
        }
    }

    // Wallet Operations
    fun depositFunds(asset: String, amount: Double, network: String): Result<WalletTransaction> {
        val currentAssets = _walletAssets.value.toMutableList()
        val index = currentAssets.indexOfFirst { it.symbol == asset }
        if (index >= 0) {
            val item = currentAssets[index]
            currentAssets[index] = item.copy(freeAmount = item.freeAmount + amount)
        } else {
            currentAssets.add(WalletAsset(symbol = asset, name = asset, freeAmount = amount))
        }
        _walletAssets.value = currentAssets
        recalculateWalletUsdValues(_tickers.value)

        val tx = WalletTransaction(
            id = "TX-${Random.nextInt(100000, 999999)}",
            type = "Deposit",
            asset = asset,
            amount = amount,
            status = "Completed",
            timestamp = System.currentTimeMillis(),
            txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16),
            network = network
        )
        _transactions.update { listOf(tx) + it }
        return Result.success(tx)
    }

    fun withdrawFunds(asset: String, amount: Double, destinationAddress: String, network: String, enteredPin: String): Result<WalletTransaction> {
        if (enteredPin != userPin) {
            return Result.failure(Exception("Incorrect Security PIN. Please verify your 4-digit code."))
        }

        val currentAssets = _walletAssets.value.toMutableList()
        val index = currentAssets.indexOfFirst { it.symbol == asset }
        if (index < 0 || currentAssets[index].freeAmount < amount) {
            return Result.failure(Exception("Insufficient $asset balance for withdrawal."))
        }

        val item = currentAssets[index]
        currentAssets[index] = item.copy(freeAmount = item.freeAmount - amount)
        _walletAssets.value = currentAssets
        recalculateWalletUsdValues(_tickers.value)

        val tx = WalletTransaction(
            id = "TX-${Random.nextInt(100000, 999999)}",
            type = "Withdraw",
            asset = asset,
            amount = amount,
            status = "Completed",
            timestamp = System.currentTimeMillis(),
            txHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16),
            network = network
        )
        _transactions.update { listOf(tx) + it }
        return Result.success(tx)
    }

    fun transferBetweenAccounts(fromAccount: String, toAccount: String, asset: String, amount: Double): Result<Boolean> {
        // e.g. "Spot" to "Funding"
        if (fromAccount == "Spot") {
            val spotList = _walletAssets.value.toMutableList()
            val idx = spotList.indexOfFirst { it.symbol == asset }
            if (idx < 0 || spotList[idx].freeAmount < amount) {
                return Result.failure(Exception("Insufficient balance in Spot Wallet."))
            }
            spotList[idx] = spotList[idx].copy(freeAmount = spotList[idx].freeAmount - amount)
            _walletAssets.value = spotList

            if (toAccount == "Funding") {
                val current = _fundingBalances.value[asset] ?: 0.0
                _fundingBalances.update { it + (asset to current + amount) }
            } else if (toAccount == "Earn") {
                val current = _earnBalances.value[asset] ?: 0.0
                _earnBalances.update { it + (asset to current + amount) }
            }
        } else if (fromAccount == "Funding") {
            val current = _fundingBalances.value[asset] ?: 0.0
            if (current < amount) return Result.failure(Exception("Insufficient balance in Funding Wallet."))
            _fundingBalances.update { it + (asset to current - amount) }

            if (toAccount == "Spot") {
                val spotList = _walletAssets.value.toMutableList()
                val idx = spotList.indexOfFirst { it.symbol == asset }
                if (idx >= 0) spotList[idx] = spotList[idx].copy(freeAmount = spotList[idx].freeAmount + amount)
                else spotList.add(WalletAsset(symbol = asset, name = asset, freeAmount = amount))
                _walletAssets.value = spotList
            }
        }
        recalculateWalletUsdValues(_tickers.value)
        return Result.success(true)
    }
}
