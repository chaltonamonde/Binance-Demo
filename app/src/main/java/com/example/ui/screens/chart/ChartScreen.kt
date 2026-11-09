package com.example.ui.screens.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketTrade
import com.example.ui.BinanceTab
import com.example.ui.BinanceViewModel
import com.example.ui.theme.BinanceDarkBorder
import com.example.ui.theme.BinanceDarkCard
import com.example.ui.theme.BinanceDarkElevated
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import com.example.ui.theme.RobotoMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChartScreen(
    symbol: String,
    viewModel: BinanceViewModel,
    onBackClick: () -> Unit,
    onNavigateToTrade: (String) -> Unit
) {
    val currentTicker by viewModel.currentTicker.collectAsState()
    val candles by viewModel.candles.collectAsState()
    val recentTrades by viewModel.recentTrades.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val showMa by viewModel.showMa.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val ticker = currentTicker ?: return
    val isFavorite = favorites.contains(ticker.symbol)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BinanceDarkBg)
            .testTag("chart_screen")
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = BinanceTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${ticker.baseAsset}/${ticker.quoteAsset}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BinanceTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(BinanceDarkElevated, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = "Spot", fontSize = 10.sp, color = BinanceYellow)
                        }
                    }
                    Text(
                        text = ticker.name,
                        fontSize = 11.sp,
                        color = BinanceTextSecondary
                    )
                }
            }

            IconButton(onClick = { viewModel.toggleFavorite(ticker.symbol) }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) BinanceYellow else BinanceTextMuted
                )
            }
        }

        // Live Price & 24h Stats Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val isUp = ticker.priceChangePercent >= 0
                val priceColor = if (isUp) BinanceGreen else BinanceRed
                Text(
                    text = String.format("%,.2f", ticker.lastPrice),
                    fontSize = 24.sp,
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = priceColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format("%,.2f", ticker.lastPrice)}",
                        fontSize = 12.sp,
                        color = BinanceTextSecondary,
                        fontFamily = RobotoMonoFontFamily
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%s%.2f%%", if (isUp) "+" else "", ticker.priceChangePercent),
                        fontSize = 12.sp,
                        color = priceColor,
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "24h High", fontSize = 10.sp, color = BinanceTextMuted)
                    Text(
                        text = String.format("%,.2f", ticker.highPrice),
                        fontSize = 10.sp,
                        fontFamily = RobotoMonoFontFamily,
                        color = BinanceTextPrimary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "24h Low", fontSize = 10.sp, color = BinanceTextMuted)
                    Text(
                        text = String.format("%,.2f", ticker.lowPrice),
                        fontSize = 10.sp,
                        fontFamily = RobotoMonoFontFamily,
                        color = BinanceTextPrimary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "24h Vol(${ticker.baseAsset})", fontSize = 10.sp, color = BinanceTextMuted)
                    Text(
                        text = "${String.format("%.1f", ticker.volume / 1000.0)}K",
                        fontSize = 10.sp,
                        fontFamily = RobotoMonoFontFamily,
                        color = BinanceTextPrimary
                    )
                }
            }
        }

        // Timeframe Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val intervals = listOf("Line", "15m", "1h", "4h", "1D", "1W")
            intervals.forEach { tf ->
                val isSelected = selectedTimeframe == tf
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) BinanceDarkElevated else Color.Transparent)
                        .clickable { viewModel.selectTimeframe(tf) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tf,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) BinanceYellow else BinanceTextSecondary
                    )
                }
            }

            // MA toggle button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (showMa) BinanceDarkElevated else Color.Transparent)
                    .clickable { viewModel.toggleMa() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "MA",
                    fontSize = 12.sp,
                    color = if (showMa) BinanceYellow else BinanceTextMuted
                )
            }
        }

        // Real-Time Candlestick Chart Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .padding(horizontal = 12.dp)
        ) {
            CandlestickChart(
                candles = candles,
                selectedTimeframe = selectedTimeframe,
                showMa = showMa,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Recent Market Trades section
        Text(
            text = "Market Trades",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BinanceTextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Price (USDT)", fontSize = 10.sp, color = BinanceTextMuted)
            Text(text = "Amount (${ticker.baseAsset})", fontSize = 10.sp, color = BinanceTextMuted)
            Text(text = "Time", fontSize = 10.sp, color = BinanceTextMuted)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(recentTrades.take(12), key = { it.id }) { trade ->
                TradeHistoryRow(trade = trade)
            }
        }

        // Bottom Fixed Buy / Sell Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BinanceDarkCard)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.setOrderSide(com.example.data.model.OrderSide.BUY)
                    viewModel.setTab(BinanceTab.TRADE)
                    onNavigateToTrade(ticker.symbol)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("chart_buy_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BinanceGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Buy ${ticker.baseAsset}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Button(
                onClick = {
                    viewModel.setOrderSide(com.example.data.model.OrderSide.SELL)
                    viewModel.setTab(BinanceTab.TRADE)
                    onNavigateToTrade(ticker.symbol)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("chart_sell_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BinanceRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Sell ${ticker.baseAsset}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun TradeHistoryRow(trade: MarketTrade) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeFormatted = remember(trade.time) { dateFormat.format(Date(trade.time)) }
    val color = if (trade.isBuyerMaker) BinanceGreen else BinanceRed

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%.2f", trade.price),
            fontSize = 11.sp,
            fontFamily = RobotoMonoFontFamily,
            color = color,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = String.format("%.4f", trade.amount),
            fontSize = 11.sp,
            fontFamily = RobotoMonoFontFamily,
            color = BinanceTextSecondary
        )
        Text(
            text = timeFormatted,
            fontSize = 10.sp,
            fontFamily = RobotoMonoFontFamily,
            color = BinanceTextMuted
        )
    }
}
