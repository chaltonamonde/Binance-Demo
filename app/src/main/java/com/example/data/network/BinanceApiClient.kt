package com.example.data.network

import com.example.data.model.Candle
import com.example.data.model.CryptoTicker
import com.example.data.model.MarketTrade
import com.example.data.model.OrderBook
import com.example.data.model.OrderBookEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class BinanceApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.binance.com/api/v3"

    // Supported top pairs on Binance
    val popularSymbols = listOf(
        "BTCUSDT" to "Bitcoin",
        "ETHUSDT" to "Ethereum",
        "BNBUSDT" to "BNB",
        "SOLUSDT" to "Solana",
        "DOGEUSDT" to "Dogecoin",
        "XRPUSDT" to "Ripple",
        "ADAUSDT" to "Cardano",
        "AVAXUSDT" to "Avalanche",
        "LINKUSDT" to "Chainlink",
        "SUIUSDT" to "Sui",
        "NEARUSDT" to "Near Protocol",
        "PEPEUSDT" to "Pepe"
    )

    suspend fun get24HrTickers(): List<CryptoTicker> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/ticker/24hr")
                .header("User-Agent", "BinanceAndroidApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext getFallbackTickers()
                val jsonArray = JSONArray(body)
                val tickerMap = mutableMapOf<String, JSONObject>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    tickerMap[obj.getString("symbol")] = obj
                }

                val result = mutableListOf<CryptoTicker>()
                for ((symbol, name) in popularSymbols) {
                    val obj = tickerMap[symbol]
                    if (obj != null) {
                        val lastPrice = obj.optDouble("lastPrice", 0.0)
                        val priceChange = obj.optDouble("priceChange", 0.0)
                        val priceChangePercent = obj.optDouble("priceChangePercent", 0.0)
                        val highPrice = obj.optDouble("highPrice", lastPrice * 1.05)
                        val lowPrice = obj.optDouble("lowPrice", lastPrice * 0.95)
                        val volume = obj.optDouble("volume", 1000.0)
                        val quoteVolume = obj.optDouble("quoteVolume", 1000000.0)

                        val sparkline = generateSparkline(lastPrice, priceChangePercent)

                        result.add(
                            CryptoTicker(
                                symbol = symbol,
                                baseAsset = symbol.replace("USDT", ""),
                                quoteAsset = "USDT",
                                name = name,
                                lastPrice = lastPrice,
                                priceChange = priceChange,
                                priceChangePercent = priceChangePercent,
                                highPrice = highPrice,
                                lowPrice = lowPrice,
                                volume = volume,
                                quoteVolume = quoteVolume,
                                sparkline = sparkline
                            )
                        )
                    }
                }
                if (result.isNotEmpty()) return@withContext result
            }
        } catch (_: Exception) {
            // Network fallback
        }
        getFallbackTickers()
    }

    suspend fun getOrderBook(symbol: String, limit: Int = 15): OrderBook = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/depth?symbol=$symbol&limit=$limit")
                .header("User-Agent", "BinanceAndroidApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext generateFallbackOrderBook(symbol, 90000.0)
                val json = JSONObject(body)
                val bidsArray = json.getJSONArray("bids")
                val asksArray = json.getJSONArray("asks")

                val bids = mutableListOf<OrderBookEntry>()
                var bidTotal = 0.0
                for (i in 0 until bidsArray.length()) {
                    val entry = bidsArray.getJSONArray(i)
                    val price = entry.getString(0).toDoubleOrNull() ?: 0.0
                    val amount = entry.getString(1).toDoubleOrNull() ?: 0.0
                    bidTotal += amount
                    bids.add(OrderBookEntry(price = price, amount = amount, total = bidTotal))
                }

                val asks = mutableListOf<OrderBookEntry>()
                var askTotal = 0.0
                for (i in 0 until asksArray.length()) {
                    val entry = asksArray.getJSONArray(i)
                    val price = entry.getString(0).toDoubleOrNull() ?: 0.0
                    val amount = entry.getString(1).toDoubleOrNull() ?: 0.0
                    askTotal += amount
                    asks.add(OrderBookEntry(price = price, amount = amount, total = askTotal))
                }

                // Compute depth percent relative to max
                val maxBid = bids.maxOfOrNull { it.total } ?: 1.0
                val maxAsk = asks.maxOfOrNull { it.total } ?: 1.0

                val normalizedBids = bids.map { it.copy(percentDepth = (it.total / maxBid).toFloat()) }
                val normalizedAsks = asks.map { it.copy(percentDepth = (it.total / maxAsk).toFloat()) }

                val topBid = bids.firstOrNull()?.price ?: 0.0
                val topAsk = asks.firstOrNull()?.price ?: 0.0
                val spread = if (topAsk > 0 && topBid > 0) topAsk - topBid else 0.0

                return@withContext OrderBook(
                    symbol = symbol,
                    bids = normalizedBids,
                    asks = normalizedAsks,
                    lastPrice = topBid,
                    spread = spread
                )
            }
        } catch (_: Exception) {
            // fallback
        }
        val defaultPrice = getDefaultPrice(symbol)
        generateFallbackOrderBook(symbol, defaultPrice)
    }

    suspend fun getKlines(symbol: String, interval: String = "1h", limit: Int = 60): List<Candle> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/klines?symbol=$symbol&interval=$interval&limit=$limit")
                .header("User-Agent", "BinanceAndroidApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext generateFallbackCandles(getDefaultPrice(symbol))
                val jsonArray = JSONArray(body)
                val candles = mutableListOf<Candle>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONArray(i)
                    val openTime = item.getLong(0)
                    val open = item.getString(1).toDoubleOrNull() ?: 0.0
                    val high = item.getString(2).toDoubleOrNull() ?: 0.0
                    val low = item.getString(3).toDoubleOrNull() ?: 0.0
                    val close = item.getString(4).toDoubleOrNull() ?: 0.0
                    val volume = item.getString(5).toDoubleOrNull() ?: 0.0
                    candles.add(Candle(openTime, open, high, low, close, volume))
                }
                if (candles.isNotEmpty()) return@withContext candles
            }
        } catch (_: Exception) {
            // fallback
        }
        generateFallbackCandles(getDefaultPrice(symbol))
    }

    suspend fun getRecentTrades(symbol: String, limit: Int = 20): List<MarketTrade> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/trades?symbol=$symbol&limit=$limit")
                .header("User-Agent", "BinanceAndroidApp/1.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return@withContext generateFallbackTrades(getDefaultPrice(symbol))
                val jsonArray = JSONArray(body)
                val trades = mutableListOf<MarketTrade>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    trades.add(
                        MarketTrade(
                            id = obj.optString("id", "${System.currentTimeMillis()}_$i"),
                            price = obj.optString("price").toDoubleOrNull() ?: 0.0,
                            amount = obj.optString("qty").toDoubleOrNull() ?: 0.0,
                            time = obj.optLong("time", System.currentTimeMillis()),
                            isBuyerMaker = obj.optBoolean("isBuyerMaker", false)
                        )
                    )
                }
                if (trades.isNotEmpty()) return@withContext trades
            }
        } catch (_: Exception) {
            // fallback
        }
        generateFallbackTrades(getDefaultPrice(symbol))
    }

    private fun getDefaultPrice(symbol: String): Double {
        return when (symbol) {
            "BTCUSDT" -> 92450.0
            "ETHUSDT" -> 3420.0
            "BNBUSDT" -> 645.0
            "SOLUSDT" -> 218.0
            "DOGEUSDT" -> 0.38
            "XRPUSDT" -> 1.45
            "ADAUSDT" -> 0.85
            "AVAXUSDT" -> 42.50
            "LINKUSDT" -> 18.20
            "SUIUSDT" -> 3.35
            "NEARUSDT" -> 6.80
            "PEPEUSDT" -> 0.000021
            else -> 100.0
        }
    }

    fun getFallbackTickers(): List<CryptoTicker> {
        val list = mutableListOf<CryptoTicker>()
        for ((symbol, name) in popularSymbols) {
            val basePrice = getDefaultPrice(symbol)
            val changePercent = when (symbol) {
                "BTCUSDT" -> 4.35
                "ETHUSDT" -> 2.82
                "BNBUSDT" -> 5.12
                "SOLUSDT" -> 8.45
                "DOGEUSDT" -> -1.25
                "XRPUSDT" -> 6.78
                "ADAUSDT" -> -0.45
                "AVAXUSDT" -> 3.20
                "LINKUSDT" -> 4.90
                "SUIUSDT" -> 12.40
                "NEARUSDT" -> -2.10
                "PEPEUSDT" -> 15.60
                else -> 1.5
            }
            val priceChange = basePrice * (changePercent / 100.0)
            list.add(
                CryptoTicker(
                    symbol = symbol,
                    baseAsset = symbol.replace("USDT", ""),
                    quoteAsset = "USDT",
                    name = name,
                    lastPrice = basePrice,
                    priceChange = priceChange,
                    priceChangePercent = changePercent,
                    highPrice = basePrice * 1.04,
                    lowPrice = basePrice * 0.96,
                    volume = basePrice * 850.0,
                    quoteVolume = basePrice * 25000000.0,
                    sparkline = generateSparkline(basePrice, changePercent)
                )
            )
        }
        return list
    }

    fun generateSparkline(basePrice: Double, trend: Double): List<Double> {
        val points = mutableListOf<Double>()
        var current = basePrice * (1.0 - (trend / 100.0))
        points.add(current)
        val step = (basePrice - current) / 10.0
        for (i in 1..12) {
            val noise = (Random.nextDouble() - 0.48) * (basePrice * 0.01)
            current += step + noise
            points.add(current)
        }
        points.add(basePrice)
        return points
    }

    fun generateFallbackOrderBook(symbol: String, centerPrice: Double): OrderBook {
        val bids = mutableListOf<OrderBookEntry>()
        val asks = mutableListOf<OrderBookEntry>()

        val step = max(centerPrice * 0.0003, 0.01)
        var bidSum = 0.0
        var askSum = 0.0

        for (i in 0 until 12) {
            val bidPrice = centerPrice - (i + 1) * step
            val bidAmount = (Random.nextDouble() * 1.5 + 0.1) * (1000.0 / max(centerPrice, 1.0))
            bidSum += bidAmount
            bids.add(OrderBookEntry(price = bidPrice, amount = bidAmount, total = bidSum))

            val askPrice = centerPrice + (i + 1) * step
            val askAmount = (Random.nextDouble() * 1.5 + 0.1) * (1000.0 / max(centerPrice, 1.0))
            askSum += askAmount
            asks.add(OrderBookEntry(price = askPrice, amount = askAmount, total = askSum))
        }

        val maxBid = bids.last().total
        val maxAsk = asks.last().total

        return OrderBook(
            symbol = symbol,
            bids = bids.map { it.copy(percentDepth = (it.total / maxBid).toFloat()) },
            asks = asks.map { it.copy(percentDepth = (it.total / maxAsk).toFloat()) },
            lastPrice = centerPrice,
            spread = step
        )
    }

    fun generateFallbackCandles(basePrice: Double): List<Candle> {
        val candles = mutableListOf<Candle>()
        var currentPrice = basePrice * 0.92
        val now = System.currentTimeMillis()
        val intervalMs = 60 * 60 * 1000L // 1 hour

        for (i in 50 downTo 0) {
            val time = now - (i * intervalMs)
            val change = (Random.nextDouble() - 0.48) * (currentPrice * 0.015)
            val open = currentPrice
            val close = currentPrice + change
            val high = max(open, close) + Random.nextDouble() * (currentPrice * 0.008)
            val low = min(open, close) - Random.nextDouble() * (currentPrice * 0.008)
            val volume = (Random.nextDouble() * 50 + 10) * (10000.0 / max(currentPrice, 1.0))

            candles.add(Candle(time, open, high, low, close, volume))
            currentPrice = close
        }
        return candles
    }

    fun generateFallbackTrades(currentPrice: Double): List<MarketTrade> {
        val trades = mutableListOf<MarketTrade>()
        val now = System.currentTimeMillis()
        for (i in 0 until 15) {
            val isBuy = Random.nextBoolean()
            val diff = (Random.nextDouble() - 0.5) * (currentPrice * 0.001)
            val price = currentPrice + diff
            val amount = (Random.nextDouble() * 1.2 + 0.05) * (1000.0 / max(currentPrice, 1.0))
            trades.add(
                MarketTrade(
                    id = (now - i * 3000L).toString(),
                    price = price,
                    amount = amount,
                    time = now - i * 2500L,
                    isBuyerMaker = isBuy
                )
            )
        }
        return trades
    }
}
