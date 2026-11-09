package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Candle
import com.example.data.model.CryptoTicker
import com.example.data.model.MarketTrade
import com.example.data.model.OrderBook
import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.TradeOrder
import com.example.data.model.WalletAddress
import com.example.data.model.WalletAsset
import com.example.data.model.WalletTransaction
import com.example.data.repository.BinanceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

enum class BinanceTab {
    HOME, MARKETS, TRADE, FUTURES, WALLETS
}

enum class MarketCategory {
    FAVORITES, HOT, GAINERS, LOSERS, VOLUME24H
}

data class UiMessage(
    val text: String,
    val isError: Boolean = false
)

class BinanceViewModel(
    private val repository: BinanceRepository = BinanceRepository()
) : ViewModel() {

    // Navigation Tab
    private val _currentTab = MutableStateFlow(BinanceTab.HOME)
    val currentTab: StateFlow<BinanceTab> = _currentTab.asStateFlow()

    // Search query across markets
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected Market category in Home / Markets
    private val _selectedCategory = MutableStateFlow(MarketCategory.HOT)
    val selectedCategory: StateFlow<MarketCategory> = _selectedCategory.asStateFlow()

    // Snackbars / UI feedback
    private val _uiEvents = MutableSharedFlow<UiMessage>()
    val uiEvents: SharedFlow<UiMessage> = _uiEvents.asSharedFlow()

    // Market Flows from Repository
    val tickers: StateFlow<List<CryptoTicker>> = repository.tickers
    val selectedSymbol: StateFlow<String> = repository.selectedSymbol
    val orderBook: StateFlow<OrderBook> = repository.orderBook
    val candles: StateFlow<List<Candle>> = repository.candles
    val recentTrades: StateFlow<List<MarketTrade>> = repository.recentTrades
    val selectedTimeframe: StateFlow<String> = repository.selectedTimeframe
    val favorites: StateFlow<Set<String>> = repository.favorites

    // Filtered Tickers based on search & category
    val filteredTickers: StateFlow<List<CryptoTicker>> = combine(
        tickers,
        searchQuery,
        selectedCategory,
        favorites
    ) { list, query, category, favs ->
        val searched = if (query.isBlank()) list else {
            list.filter { it.symbol.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true) }
        }
        when (category) {
            MarketCategory.FAVORITES -> searched.filter { favs.contains(it.symbol) }
            MarketCategory.HOT -> searched.sortedByDescending { it.quoteVolume }
            MarketCategory.GAINERS -> searched.sortedByDescending { it.priceChangePercent }
            MarketCategory.LOSERS -> searched.sortedBy { it.priceChangePercent }
            MarketCategory.VOLUME24H -> searched.sortedByDescending { it.quoteVolume }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently active ticker
    val currentTicker: StateFlow<CryptoTicker?> = combine(tickers, selectedSymbol) { list, sym ->
        list.find { it.symbol == sym } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Wallet Flows
    val walletAssets: StateFlow<List<WalletAsset>> = repository.walletAssets
    val fundingBalances: StateFlow<Map<String, Double>> = repository.fundingBalances
    val earnBalances: StateFlow<Map<String, Double>> = repository.earnBalances
    val walletAddresses: List<WalletAddress> = repository.walletAddresses
    val orders: StateFlow<List<TradeOrder>> = repository.orders
    val transactions: StateFlow<List<WalletTransaction>> = repository.transactions
    val seedPhrase: String = repository.seedPhrase
    val userPin: String = repository.userPin

    // Total Wallet Net Worth in USD
    val totalNetWorthUsd: StateFlow<Double> = walletAssets.combine(fundingBalances) { spotAssets, funding ->
        val spotSum = spotAssets.sumOf { it.usdValue }
        val fundingUsdt = funding["USDT"] ?: 0.0
        spotSum + fundingUsdt
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Trade Order Form State
    private val _orderSide = MutableStateFlow(OrderSide.BUY)
    val orderSide: StateFlow<OrderSide> = _orderSide.asStateFlow()

    private val _orderType = MutableStateFlow(OrderType.LIMIT)
    val orderType: StateFlow<OrderType> = _orderType.asStateFlow()

    private val _priceInput = MutableStateFlow("")
    val priceInput: StateFlow<String> = _priceInput.asStateFlow()

    private val _amountInput = MutableStateFlow("")
    val amountInput: StateFlow<String> = _amountInput.asStateFlow()

    private val _percentSlider = MutableStateFlow(0f)
    val percentSlider: StateFlow<Float> = _percentSlider.asStateFlow()

    // Balance visibility toggle
    private val _isBalanceHidden = MutableStateFlow(false)
    val isBalanceHidden: StateFlow<Boolean> = _isBalanceHidden.asStateFlow()

    // Chart Fullscreen toggle
    private val _isChartFullscreen = MutableStateFlow(false)
    val isChartFullscreen: StateFlow<Boolean> = _isChartFullscreen.asStateFlow()

    // Selected Candle for scrubber
    private val _scrubbedCandle = MutableStateFlow<Candle?>(null)
    val scrubbedCandle: StateFlow<Candle?> = _scrubbedCandle.asStateFlow()

    // Technical Indicators Toggle
    private val _showMa = MutableStateFlow(true)
    val showMa: StateFlow<Boolean> = _showMa.asStateFlow()

    private val _showRsi = MutableStateFlow(true)
    val showRsi: StateFlow<Boolean> = _showRsi.asStateFlow()

    // Futures Tab State
    private val _leverage = MutableStateFlow(20)
    val leverage: StateFlow<Int> = _leverage.asStateFlow()

    private val _isFuturesLong = MutableStateFlow(true)
    val isFuturesLong: StateFlow<Boolean> = _isFuturesLong.asStateFlow()

    // Modals
    private val _showDepositSheet = MutableStateFlow(false)
    val showDepositSheet: StateFlow<Boolean> = _showDepositSheet.asStateFlow()

    private val _showWithdrawSheet = MutableStateFlow(false)
    val showWithdrawSheet: StateFlow<Boolean> = _showWithdrawSheet.asStateFlow()

    private val _showTransferSheet = MutableStateFlow(false)
    val showTransferSheet: StateFlow<Boolean> = _showTransferSheet.asStateFlow()

    private val _showSecuritySheet = MutableStateFlow(false)
    val showSecuritySheet: StateFlow<Boolean> = _showSecuritySheet.asStateFlow()

    // Navigation and Tab Control
    fun setTab(tab: BinanceTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: MarketCategory) {
        _selectedCategory.value = category
    }

    fun toggleFavorite(symbol: String) {
        repository.toggleFavorite(symbol)
    }

    fun selectSymbol(symbol: String) {
        repository.selectSymbol(symbol)
        val ticker = tickers.value.find { it.symbol == symbol }
        if (ticker != null) {
            _priceInput.value = formatDecimal(ticker.lastPrice)
        }
    }

    fun selectTimeframe(tf: String) {
        repository.selectTimeframe(tf)
    }

    fun setScrubbedCandle(candle: Candle?) {
        _scrubbedCandle.value = candle
    }

    fun toggleMa() {
        _showMa.value = !_showMa.value
    }

    fun toggleRsi() {
        _showRsi.value = !_showRsi.value
    }

    fun toggleBalanceHidden() {
        _isBalanceHidden.value = !_isBalanceHidden.value
    }

    fun setChartFullscreen(open: Boolean) {
        _isChartFullscreen.value = open
    }

    // Trading Form Actions
    fun setOrderSide(side: OrderSide) {
        _orderSide.value = side
        _percentSlider.value = 0f
        _amountInput.value = ""
    }

    fun setOrderType(type: OrderType) {
        _orderType.value = type
        if (type == OrderType.MARKET) {
            val curr = currentTicker.value
            if (curr != null) _priceInput.value = formatDecimal(curr.lastPrice)
        }
    }

    fun setPriceInput(price: String) {
        _priceInput.value = price
    }

    fun setAmountInput(amount: String) {
        _amountInput.value = amount
        _percentSlider.value = 0f
    }

    fun setPercentSlider(percent: Float) {
        _percentSlider.value = percent
        val side = _orderSide.value
        val ticker = currentTicker.value ?: return
        val price = _priceInput.value.toDoubleOrNull() ?: ticker.lastPrice
        val baseAsset = ticker.baseAsset
        val assets = walletAssets.value

        if (side == OrderSide.BUY) {
            val usdtBalance = assets.find { it.symbol == "USDT" }?.freeAmount ?: 0.0
            val allocUsdt = usdtBalance * percent
            if (price > 0) {
                val amt = allocUsdt / price
                _amountInput.value = String.format("%.4f", amt)
            }
        } else {
            val cryptoBalance = assets.find { it.symbol == baseAsset }?.freeAmount ?: 0.0
            val amt = cryptoBalance * percent
            _amountInput.value = String.format("%.4f", amt)
        }
    }

    fun placeOrder() {
        val ticker = currentTicker.value ?: return
        val symbol = ticker.symbol
        val side = _orderSide.value
        val type = _orderType.value

        val price = if (type == OrderType.MARKET) {
            ticker.lastPrice
        } else {
            _priceInput.value.toDoubleOrNull() ?: ticker.lastPrice
        }

        val amount = _amountInput.value.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            viewModelScope.launch {
                _uiEvents.emit(UiMessage("Please enter a valid amount", isError = true))
            }
            return
        }

        val result = repository.placeOrder(symbol, side, type, price, amount)
        result.fold(
            onSuccess = { order ->
                viewModelScope.launch {
                    val sideLabel = if (side == OrderSide.BUY) "Bought" else "Sold"
                    _uiEvents.emit(UiMessage("Order Executed: $sideLabel ${formatCrypto(amount)} ${ticker.baseAsset} @ $$price"))
                    _amountInput.value = ""
                    _percentSlider.value = 0f
                }
            },
            onFailure = { err ->
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage(err.message ?: "Failed to place order", isError = true))
                }
            }
        )
    }

    fun cancelOrder(orderId: String) {
        repository.cancelOrder(orderId)
        viewModelScope.launch {
            _uiEvents.emit(UiMessage("Order $orderId cancelled"))
        }
    }

    // Futures Form Actions
    fun setLeverage(lev: Int) {
        _leverage.value = lev
    }

    fun setFuturesSide(isLong: Boolean) {
        _isFuturesLong.value = isLong
    }

    fun executeFuturesPosition(marginUsdt: Double) {
        val ticker = currentTicker.value ?: return
        val lev = _leverage.value
        val positionSize = marginUsdt * lev
        val dir = if (_isFuturesLong.value) "LONG" else "SHORT"
        viewModelScope.launch {
            _uiEvents.emit(UiMessage("Opened $dir ${ticker.symbol} ${lev}x Position: $$positionSize"))
        }
    }

    // Modals Control
    fun setDepositSheet(show: Boolean) {
        _showDepositSheet.value = show
    }

    fun setWithdrawSheet(show: Boolean) {
        _showWithdrawSheet.value = show
    }

    fun setTransferSheet(show: Boolean) {
        _showTransferSheet.value = show
    }

    fun setSecuritySheet(show: Boolean) {
        _showSecuritySheet.value = show
    }

    fun simulateDeposit(asset: String, amount: Double, network: String) {
        val res = repository.depositFunds(asset, amount, network)
        res.fold(
            onSuccess = { tx ->
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage("Deposit Successful: +$amount $asset ($network)"))
                    _showDepositSheet.value = false
                }
            },
            onFailure = { err ->
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage(err.message ?: "Deposit failed", isError = true))
                }
            }
        )
    }

    fun simulateWithdraw(asset: String, amount: Double, address: String, network: String, pin: String) {
        val res = repository.withdrawFunds(asset, amount, address, network, pin)
        res.fold(
            onSuccess = { tx ->
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage("Withdrawal of $amount $asset initiated. TxHash: ${tx.txHash}"))
                    _showWithdrawSheet.value = false
                }
            },
            onFailure = { err ->
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage(err.message ?: "Withdrawal failed", isError = true))
                }
            }
        )
    }

    fun simulateTransfer(from: String, to: String, asset: String, amount: Double) {
        val res = repository.transferBetweenAccounts(from, to, asset, amount)
        res.fold(
            onSuccess = {
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage("Transferred $amount $asset from $from to $to"))
                    _showTransferSheet.value = false
                }
            },
            onFailure = { err ->
                viewModelScope.launch {
                    _uiEvents.emit(UiMessage(err.message ?: "Transfer failed", isError = true))
                }
            }
        )
    }

    private fun formatDecimal(value: Double): String {
        return if (value >= 1.0) String.format("%.2f", value) else String.format("%.6f", value)
    }

    private fun formatCrypto(value: Double): String {
        return if (value < 0.001) String.format("%.6f", value) else String.format("%.4f", value)
    }
}
