package com.example.ui.screens.futures

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BinanceViewModel
import com.example.ui.screens.trade.OrderBookView
import com.example.ui.theme.BinanceDarkBorder
import com.example.ui.theme.BinanceDarkCard
import com.example.ui.theme.BinanceDarkElevated
import com.example.ui.theme.BinanceDarkInput
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import com.example.ui.theme.RobotoMonoFontFamily
import kotlin.math.max

@Composable
fun FuturesScreen(
    viewModel: BinanceViewModel
) {
    val currentTicker by viewModel.currentTicker.collectAsState()
    val orderBook by viewModel.orderBook.collectAsState()
    val leverage by viewModel.leverage.collectAsState()
    val isLong by viewModel.isFuturesLong.collectAsState()
    val walletAssets by viewModel.walletAssets.collectAsState()

    val ticker = currentTicker ?: return
    val availableUsdt = walletAssets.find { it.symbol == "USDT" }?.freeAmount ?: 10000.0

    var marginText by remember { mutableStateOf("100") }
    var isCrossMargin by remember { mutableStateOf(true) }

    val marginUsdt = marginText.toDoubleOrNull() ?: 100.0
    val notionalPositionSize = marginUsdt * leverage
    val contractAmount = if (ticker.lastPrice > 0) notionalPositionSize / ticker.lastPrice else 0.0

    // Liquidation price calculation estimate
    val liqPrice = if (isLong) {
        ticker.lastPrice * (1.0 - (1.0 / leverage) * 0.90)
    } else {
        ticker.lastPrice * (1.0 + (1.0 / leverage) * 0.90)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("futures_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Futures Header: Symbol + Margin Type + Leverage Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${ticker.baseAsset}USDT",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Perpetual",
                        fontSize = 10.sp,
                        color = BinanceYellow,
                        modifier = Modifier
                            .background(BinanceDarkElevated, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Margin Mode Toggle (Cross / Isolated)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BinanceDarkElevated)
                            .clickable { isCrossMargin = !isCrossMargin }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isCrossMargin) "Cross" else "Isolated",
                            fontSize = 11.sp,
                            color = BinanceTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Leverage Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BinanceDarkElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${leverage}x",
                            fontSize = 11.sp,
                            color = BinanceYellow,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Main Trading & Leverage Panel + Mini Order Book
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Column: Futures Position Builder
                Column(modifier = Modifier.weight(1.35f)) {
                    // Long / Short Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BinanceDarkElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLong) BinanceGreen else Color.Transparent)
                                .clickable { viewModel.setFuturesSide(true) }
                                .testTag("futures_long_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Open Long",
                                color = if (isLong) Color.White else BinanceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isLong) BinanceRed else Color.Transparent)
                                .clickable { viewModel.setFuturesSide(false) }
                                .testTag("futures_short_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Open Short",
                                color = if (!isLong) Color.White else BinanceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Leverage Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Adjust Leverage", fontSize = 11.sp, color = BinanceTextSecondary)
                            Text(
                                text = "${leverage}x",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BinanceYellow,
                                fontFamily = RobotoMonoFontFamily
                            )
                        }
                        Slider(
                            value = leverage.toFloat(),
                            onValueChange = { viewModel.setLeverage(it.toInt().coerceIn(1, 125)) },
                            valueRange = 1f..100f,
                            steps = 19,
                            colors = SliderDefaults.colors(
                                thumbColor = BinanceYellow,
                                activeTrackColor = BinanceYellow,
                                inactiveTrackColor = BinanceDarkBorder
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }

                    // Margin Input
                    OutlinedTextField(
                        value = marginText,
                        onValueChange = { marginText = it },
                        label = { Text("Margin (USDT)", fontSize = 10.sp, color = BinanceTextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BinanceDarkInput,
                            unfocusedContainerColor = BinanceDarkInput,
                            focusedBorderColor = BinanceYellow,
                            unfocusedBorderColor = BinanceDarkBorder,
                            focusedTextColor = BinanceTextPrimary,
                            unfocusedTextColor = BinanceTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Margin buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(50, 100, 250, 500).forEach { amt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BinanceDarkInput)
                                    .clickable { marginText = amt.toString() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$$amt",
                                    fontSize = 10.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    color = BinanceTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Futures Position Metric Breakdown Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Position Size:", fontSize = 11.sp, color = BinanceTextMuted)
                                Text(
                                    text = "$${String.format("%,.2f", notionalPositionSize)} (${String.format("%.4f", contractAmount)} ${ticker.baseAsset})",
                                    fontSize = 11.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    color = BinanceTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Est. Liq Price:", fontSize = 11.sp, color = BinanceTextMuted)
                                Text(
                                    text = "$${String.format("%,.2f", liqPrice)}",
                                    fontSize = 11.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    color = BinanceRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Funding Rate:", fontSize = 11.sp, color = BinanceTextMuted)
                                Text(
                                    text = "0.0100% / 8h",
                                    fontSize = 11.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    color = BinanceGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Open Long / Short Button
                    val ctaBg = if (isLong) BinanceGreen else BinanceRed
                    val ctaText = if (isLong) "Buy / Long ${ticker.baseAsset}" else "Sell / Short ${ticker.baseAsset}"

                    Button(
                        onClick = { viewModel.executeFuturesPosition(marginUsdt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("execute_futures_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ctaBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = ctaText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right Column: Live Depth Order Book
                Column(modifier = Modifier.weight(1.0f)) {
                    OrderBookView(
                        orderBook = orderBook,
                        lastPrice = ticker.lastPrice,
                        isPriceUp = ticker.priceChangePercent >= 0,
                        onPriceClick = {}
                    )
                }
            }
        }

        // Active Positions / Open Orders preview section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Open Positions (1)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(BinanceGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "LONG 20X", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BinanceGreen)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "BTCUSDT Perpetual", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BinanceTextPrimary)
                        }

                        Text(
                            text = "+$382.40 (+18.4%)",
                            fontSize = 13.sp,
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = BinanceGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Size (BTC)", fontSize = 10.sp, color = BinanceTextMuted)
                            Text(text = "0.2500", fontSize = 12.sp, fontFamily = RobotoMonoFontFamily, color = BinanceTextPrimary)
                        }
                        Column {
                            Text(text = "Entry Price", fontSize = 10.sp, color = BinanceTextMuted)
                            Text(text = "$91,820.00", fontSize = 12.sp, fontFamily = RobotoMonoFontFamily, color = BinanceTextPrimary)
                        }
                        Column {
                            Text(text = "Mark Price", fontSize = 10.sp, color = BinanceTextMuted)
                            Text(text = "$${String.format("%.2f", ticker.lastPrice)}", fontSize = 12.sp, fontFamily = RobotoMonoFontFamily, color = BinanceTextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Liq. Price", fontSize = 10.sp, color = BinanceTextMuted)
                            Text(text = "$87,400.00", fontSize = 12.sp, fontFamily = RobotoMonoFontFamily, color = BinanceRed)
                        }
                    }
                }
            }
        }
    }
}
