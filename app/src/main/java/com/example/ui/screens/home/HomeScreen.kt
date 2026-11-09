package com.example.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CryptoTicker
import com.example.ui.BinanceTab
import com.example.ui.BinanceViewModel
import com.example.ui.MarketCategory
import com.example.ui.components.BinanceTopHeader
import com.example.ui.components.CustomBinanceTab
import com.example.ui.components.PriceChangePill
import com.example.ui.components.QuickServiceItem
import com.example.ui.components.Sparkline
import com.example.ui.theme.BinanceBlue
import com.example.ui.theme.BinanceDarkBorder
import com.example.ui.theme.BinanceDarkCard
import com.example.ui.theme.BinanceDarkElevated
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinancePurple
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import com.example.ui.theme.RobotoMonoFontFamily

@Composable
fun HomeScreen(
    viewModel: BinanceViewModel,
    onNavigateToTrade: (String) -> Unit,
    onNavigateToChart: (String) -> Unit
) {
    val tickers by viewModel.filteredTickers.collectAsState()
    val allTickers by viewModel.tickers.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val totalNetWorth by viewModel.totalNetWorthUsd.collectAsState()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsState()

    val btcTicker = allTickers.find { it.symbol == "BTCUSDT" }
    val btcPrice = btcTicker?.lastPrice ?: 92450.0
    val btcEquivalent = if (btcPrice > 0) totalNetWorth / btcPrice else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Top Header
        item {
            BinanceTopHeader(
                onSearchClick = { viewModel.setTab(BinanceTab.MARKETS) },
                onScanClick = { viewModel.setDepositSheet(true) },
                onNotificationClick = {},
                onProfileClick = { viewModel.setSecuritySheet(true) }
            )
        }

        // Balance Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Total Balance",
                                fontSize = 12.sp,
                                color = BinanceTextSecondary
                            )
                            IconButton(
                                onClick = { viewModel.toggleBalanceHidden() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Balance",
                                    tint = BinanceTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "Today's PnL +$1,420.50 (+3.12%)",
                            fontSize = 11.sp,
                            color = BinanceGreen,
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (isBalanceHidden) "******" else "$${String.format("%,.2f", totalNetWorth)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = RobotoMonoFontFamily,
                            color = BinanceTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBalanceHidden) "*** BTC" else "≈ ${String.format("%.4f", btcEquivalent)} BTC",
                            fontSize = 12.sp,
                            color = BinanceTextSecondary,
                            fontFamily = RobotoMonoFontFamily,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setDepositSheet(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("home_deposit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Deposit",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.setTab(BinanceTab.TRADE) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("home_trade_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceDarkElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Trade Now",
                                color = BinanceTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Quick Service Icons Grid
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickServiceItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Deposit",
                    iconTint = BinanceYellow,
                    onClick = { viewModel.setDepositSheet(true) }
                )
                QuickServiceItem(
                    icon = Icons.Default.CurrencyExchange,
                    label = "Convert",
                    iconTint = BinanceBlue,
                    onClick = { viewModel.setTab(BinanceTab.TRADE) }
                )
                QuickServiceItem(
                    icon = Icons.Default.CandlestickChart,
                    label = "Spot",
                    iconTint = BinanceGreen,
                    onClick = { viewModel.setTab(BinanceTab.TRADE) }
                )
                QuickServiceItem(
                    icon = Icons.Default.Bolt,
                    label = "Futures",
                    iconTint = BinanceRed,
                    onClick = { viewModel.setTab(BinanceTab.FUTURES) }
                )
                QuickServiceItem(
                    icon = Icons.Default.Savings,
                    label = "Earn",
                    iconTint = BinanceGold,
                    onClick = { viewModel.setTab(BinanceTab.WALLETS) }
                )
            }
        }

        // Promotional Banner Carousel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.setTab(BinanceTab.TRADE) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.binance_banner_1794223531024),
                        contentDescription = "Binance Campaign Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BinanceYellow, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "EXCLUSIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Column {
                            Text(
                                text = "Zero Trading Fees on BTC/USDT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Join millions of traders worldwide with real-time liquidity",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Top Market Tickers Horizontal Scroll
        item {
            Text(
                text = "Market Trends",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val topFour = allTickers.take(5)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(topFour) { ticker ->
                    TrendTickerCard(
                        ticker = ticker,
                        onClick = {
                            viewModel.selectSymbol(ticker.symbol)
                            onNavigateToTrade(ticker.symbol)
                        }
                    )
                }
            }
        }

        // Category Tabs (Hot, Gainers, Losers, 24h Vol, Favorites)
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                CustomBinanceTab(
                    title = "Hot",
                    isSelected = selectedCategory == MarketCategory.HOT,
                    onClick = { viewModel.setCategory(MarketCategory.HOT) }
                )
                CustomBinanceTab(
                    title = "Gainers",
                    isSelected = selectedCategory == MarketCategory.GAINERS,
                    onClick = { viewModel.setCategory(MarketCategory.GAINERS) }
                )
                CustomBinanceTab(
                    title = "Losers",
                    isSelected = selectedCategory == MarketCategory.LOSERS,
                    onClick = { viewModel.setCategory(MarketCategory.LOSERS) }
                )
                CustomBinanceTab(
                    title = "24h Vol",
                    isSelected = selectedCategory == MarketCategory.VOLUME24H,
                    onClick = { viewModel.setCategory(MarketCategory.VOLUME24H) }
                )
                CustomBinanceTab(
                    title = "Favorites",
                    isSelected = selectedCategory == MarketCategory.FAVORITES,
                    onClick = { viewModel.setCategory(MarketCategory.FAVORITES) }
                )
            }
        }

        // Ticker List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Name / 24h Vol", fontSize = 11.sp, color = BinanceTextMuted)
                Text(text = "Last Price", fontSize = 11.sp, color = BinanceTextMuted)
                Text(text = "24h Chg%", fontSize = 11.sp, color = BinanceTextMuted)
            }
        }

        // Coin List Items
        items(tickers) { ticker ->
            CoinRowItem(
                ticker = ticker,
                onClick = {
                    viewModel.selectSymbol(ticker.symbol)
                    onNavigateToTrade(ticker.symbol)
                },
                onChartClick = {
                    viewModel.selectSymbol(ticker.symbol)
                    onNavigateToChart(ticker.symbol)
                }
            )
        }
    }
}

@Composable
fun TrendTickerCard(
    ticker: CryptoTicker,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(135.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ticker.baseAsset}/USDT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextPrimary
                )
                val isUp = ticker.priceChangePercent >= 0
                Text(
                    text = String.format("%s%.1f%%", if (isUp) "+" else "", ticker.priceChangePercent),
                    fontSize = 11.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = if (isUp) BinanceGreen else BinanceRed,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format("%.2f", ticker.lastPrice),
                fontSize = 14.sp,
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Sparkline(
                points = ticker.sparkline,
                isPositive = ticker.priceChangePercent >= 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )
        }
    }
}

@Composable
fun CoinRowItem(
    ticker: CryptoTicker,
    onClick: () -> Unit,
    onChartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Name & Volume
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1.3f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BinanceDarkElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ticker.baseAsset.take(2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceYellow
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = ticker.baseAsset,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextPrimary
                    )
                    Text(
                        text = "/USDT",
                        fontSize = 10.sp,
                        color = BinanceTextMuted
                    )
                }
                Text(
                    text = "${String.format("%.1f", ticker.volume / 1000.0)}K Vol",
                    fontSize = 11.sp,
                    color = BinanceTextSecondary,
                    fontFamily = RobotoMonoFontFamily
                )
            }
        }

        // Last Price
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1.2f)
        ) {
            Text(
                text = String.format("%.2f", ticker.lastPrice),
                fontSize = 14.sp,
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = BinanceTextPrimary
            )
            Text(
                text = "$${String.format("%.2f", ticker.lastPrice)}",
                fontSize = 11.sp,
                color = BinanceTextMuted,
                fontFamily = RobotoMonoFontFamily
            )
        }

        // 24h Change Pill
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            PriceChangePill(
                changePercent = ticker.priceChangePercent,
                minWidth = 68.dp
            )
        }
    }
}
