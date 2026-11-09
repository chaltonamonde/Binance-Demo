package com.example.ui.screens.trade

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.model.OrderSide
import com.example.data.model.OrderStatus
import com.example.data.model.OrderType
import com.example.data.model.TradeOrder
import com.example.ui.BinanceViewModel
import com.example.ui.components.CustomBinanceTab
import com.example.ui.theme.BinanceDarkBorder
import com.example.ui.theme.BinanceDarkCard
import com.example.ui.theme.BinanceDarkElevated
import com.example.ui.theme.BinanceDarkInput
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceGreenBg
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceRedBg
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import com.example.ui.theme.RobotoMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeScreen(
    viewModel: BinanceViewModel,
    onNavigateToChart: (String) -> Unit
) {
    val currentTicker by viewModel.currentTicker.collectAsState()
    val allTickers by viewModel.tickers.collectAsState()
    val orderBook by viewModel.orderBook.collectAsState()
    val orderSide by viewModel.orderSide.collectAsState()
    val orderType by viewModel.orderType.collectAsState()
    val priceInput by viewModel.priceInput.collectAsState()
    val amountInput by viewModel.amountInput.collectAsState()
    val percentSlider by viewModel.percentSlider.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val walletAssets by viewModel.walletAssets.collectAsState()

    var showPairDropdown by remember { mutableStateOf(false) }
    var selectedOrderTab by remember { mutableStateOf("Open Orders") }

    val ticker = currentTicker ?: allTickers.firstOrNull() ?: return
    val baseAsset = ticker.baseAsset
    val availableUsdt = walletAssets.find { it.symbol == "USDT" }?.freeAmount ?: 0.0
    val availableCrypto = walletAssets.find { it.symbol == baseAsset }?.freeAmount ?: 0.0

    val openOrders = orders.filter { it.status == OrderStatus.OPEN }
    val orderHistory = orders.filter { it.status != OrderStatus.OPEN }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("trade_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Trade Header: Pair Selector, 24h Stats & Chart Button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pair Selector
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showPairDropdown = true }
                            .padding(4.dp)
                            .testTag("pair_selector_dropdown")
                    ) {
                        Text(
                            text = "${ticker.baseAsset}/USDT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BinanceTextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select pair",
                            tint = BinanceTextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val isUp = ticker.priceChangePercent >= 0
                        Text(
                            text = String.format("%s%.2f%%", if (isUp) "+" else "", ticker.priceChangePercent),
                            fontSize = 12.sp,
                            fontFamily = RobotoMonoFontFamily,
                            color = if (isUp) BinanceGreen else BinanceRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    DropdownMenu(
                        expanded = showPairDropdown,
                        onDismissRequest = { showPairDropdown = false },
                        modifier = Modifier.background(BinanceDarkCard)
                    ) {
                        allTickers.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = t.symbol, color = BinanceTextPrimary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = "$${String.format("%.2f", t.lastPrice)}",
                                            fontFamily = RobotoMonoFontFamily,
                                            color = BinanceTextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectSymbol(t.symbol)
                                    showPairDropdown = false
                                }
                            )
                        }
                    }
                }

                // 24h High/Low and Chart Icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "24h High: ${String.format("%.2f", ticker.highPrice)}",
                            fontSize = 9.sp,
                            color = BinanceTextMuted,
                            fontFamily = RobotoMonoFontFamily
                        )
                        Text(
                            text = "24h Low: ${String.format("%.2f", ticker.lowPrice)}",
                            fontSize = 9.sp,
                            color = BinanceTextMuted,
                            fontFamily = RobotoMonoFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onNavigateToChart(ticker.symbol) },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("open_chart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CandlestickChart,
                            contentDescription = "Candlestick Chart",
                            tint = BinanceYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Two-Column Section: Order Form (Left 58%) and Order Book (Right 42%)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Column: Order Form
                Column(
                    modifier = Modifier.weight(1.35f)
                ) {
                    // Buy / Sell Tabs
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
                                .background(if (orderSide == OrderSide.BUY) BinanceGreen else Color.Transparent)
                                .clickable { viewModel.setOrderSide(OrderSide.BUY) }
                                .testTag("order_side_buy"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Buy",
                                color = if (orderSide == OrderSide.BUY) Color.White else BinanceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (orderSide == OrderSide.SELL) BinanceRed else Color.Transparent)
                                .clickable { viewModel.setOrderSide(OrderSide.SELL) }
                                .testTag("order_side_sell"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sell",
                                color = if (orderSide == OrderSide.SELL) Color.White else BinanceTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Order Type (Limit vs Market)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (orderType == OrderType.LIMIT) BinanceDarkBorder else BinanceDarkInput)
                                .clickable { viewModel.setOrderType(OrderType.LIMIT) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Limit",
                                fontSize = 11.sp,
                                fontWeight = if (orderType == OrderType.LIMIT) FontWeight.Bold else FontWeight.Normal,
                                color = if (orderType == OrderType.LIMIT) BinanceYellow else BinanceTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (orderType == OrderType.MARKET) BinanceDarkBorder else BinanceDarkInput)
                                .clickable { viewModel.setOrderType(OrderType.MARKET) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Market",
                                fontSize = 11.sp,
                                fontWeight = if (orderType == OrderType.MARKET) FontWeight.Bold else FontWeight.Normal,
                                color = if (orderType == OrderType.MARKET) BinanceYellow else BinanceTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price Input
                    if (orderType == OrderType.LIMIT) {
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { viewModel.setPriceInput(it) },
                            label = { Text("Price (USDT)", fontSize = 10.sp, color = BinanceTextMuted) },
                            trailingIcon = {
                                Row {
                                    IconButton(
                                        onClick = {
                                            val p = priceInput.toDoubleOrNull() ?: ticker.lastPrice
                                            viewModel.setPriceInput(String.format("%.2f", p * 0.999))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "-", tint = BinanceTextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            val p = priceInput.toDoubleOrNull() ?: ticker.lastPrice
                                            viewModel.setPriceInput(String.format("%.2f", p * 1.001))
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "+", tint = BinanceTextSecondary, modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                                .testTag("trade_price_input")
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BinanceDarkInput)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Market Price (Best Available)",
                                fontSize = 12.sp,
                                color = BinanceTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Amount Input
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { viewModel.setAmountInput(it) },
                        label = { Text("Amount ($baseAsset)", fontSize = 10.sp, color = BinanceTextMuted) },
                        trailingIcon = {
                            Text(
                                text = baseAsset,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BinanceTextSecondary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            .testTag("trade_amount_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Percentage Quick Selector Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0.25f to "25%", 0.50f to "50%", 0.75f to "75%", 1.0f to "100%").forEach { (ratio, label) ->
                            val isSelected = percentSlider == ratio
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) BinanceYellow.copy(alpha = 0.2f) else BinanceDarkInput)
                                    .clickable { viewModel.setPercentSlider(ratio) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontFamily = RobotoMonoFontFamily,
                                    color = if (isSelected) BinanceYellow else BinanceTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Available Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Avbl:", fontSize = 11.sp, color = BinanceTextMuted)
                        val avblText = if (orderSide == OrderSide.BUY) {
                            "${String.format("%,.2f", availableUsdt)} USDT"
                        } else {
                            "${String.format("%.4f", availableCrypto)} $baseAsset"
                        }
                        Text(
                            text = avblText,
                            fontSize = 11.sp,
                            fontFamily = RobotoMonoFontFamily,
                            color = BinanceTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Total USD calculation
                    val p = priceInput.toDoubleOrNull() ?: ticker.lastPrice
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    val totalCalc = p * amt

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total:", fontSize = 11.sp, color = BinanceTextMuted)
                        Text(
                            text = "${String.format("%,.2f", totalCalc)} USDT",
                            fontSize = 11.sp,
                            fontFamily = RobotoMonoFontFamily,
                            color = BinanceTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Primary CTA Buy / Sell Button
                    val isBuy = orderSide == OrderSide.BUY
                    val ctaBg = if (isBuy) BinanceGreen else BinanceRed
                    val ctaText = if (isBuy) "Buy $baseAsset" else "Sell $baseAsset"

                    Button(
                        onClick = { viewModel.placeOrder() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("submit_order_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ctaBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = ctaText,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right Column: Live Order Book
                Column(
                    modifier = Modifier.weight(1.0f)
                ) {
                    OrderBookView(
                        orderBook = orderBook,
                        lastPrice = ticker.lastPrice,
                        isPriceUp = ticker.priceChangePercent >= 0,
                        onPriceClick = { clickedPrice ->
                            viewModel.setPriceInput(String.format("%.2f", clickedPrice))
                        }
                    )
                }
            }
        }

        // Lower Section: Orders Tabs & List
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                CustomBinanceTab(
                    title = "Open Orders",
                    isSelected = selectedOrderTab == "Open Orders",
                    onClick = { selectedOrderTab = "Open Orders" },
                    badgeText = "${openOrders.size}"
                )
                CustomBinanceTab(
                    title = "Order History",
                    isSelected = selectedOrderTab == "Order History",
                    onClick = { selectedOrderTab = "Order History" }
                )
            }
        }

        // Active Orders or History list
        val displayedOrders = if (selectedOrderTab == "Open Orders") openOrders else orderHistory
        if (displayedOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedOrderTab == "Open Orders") "No open orders" else "No order history",
                        fontSize = 13.sp,
                        color = BinanceTextMuted
                    )
                }
            }
        } else {
            items(displayedOrders, key = { it.id }) { order ->
                OrderItemCard(
                    order = order,
                    onCancel = { viewModel.cancelOrder(order.id) }
                )
            }
        }
    }
}

@Composable
fun OrderItemCard(
    order: TradeOrder,
    onCancel: () -> Unit
) {
    val isBuy = order.side == OrderSide.BUY
    val tagColor = if (isBuy) BinanceGreen else BinanceRed
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val timeFormatted = remember(order.timestamp) { dateFormat.format(Date(order.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
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
                            .background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isBuy) "BUY" else "SELL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = order.symbol,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.type.name,
                        fontSize = 11.sp,
                        color = BinanceTextMuted
                    )
                }

                if (order.status == OrderStatus.OPEN) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = BinanceDarkElevated),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(text = "Cancel", fontSize = 11.sp, color = BinanceYellow)
                    }
                } else {
                    Text(
                        text = order.status.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (order.status == OrderStatus.FILLED) BinanceGreen else BinanceTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Price", fontSize = 10.sp, color = BinanceTextMuted)
                    Text(
                        text = "$${String.format("%.2f", order.price)}",
                        fontSize = 12.sp,
                        fontFamily = RobotoMonoFontFamily,
                        color = BinanceTextPrimary
                    )
                }
                Column {
                    Text(text = "Amount", fontSize = 10.sp, color = BinanceTextMuted)
                    Text(
                        text = String.format("%.4f", order.amount),
                        fontSize = 12.sp,
                        fontFamily = RobotoMonoFontFamily,
                        color = BinanceTextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total (USDT)", fontSize = 10.sp, color = BinanceTextMuted)
                    Text(
                        text = "$${String.format("%.2f", order.totalUsd)}",
                        fontSize = 12.sp,
                        fontFamily = RobotoMonoFontFamily,
                        color = BinanceTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = BinanceTextMuted
                )
                val fillRatio = if (order.amount > 0) (order.filled / order.amount * 100.0) else 0.0
                Text(
                    text = "Filled: ${String.format("%.1f", fillRatio)}%",
                    fontSize = 10.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = BinanceTextSecondary
                )
            }
        }
    }
}
