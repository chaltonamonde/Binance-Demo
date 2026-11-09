package com.example.ui.screens.trade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderBook
import com.example.data.model.OrderBookEntry
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceGreenBg
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceRedBg
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.RobotoMonoFontFamily
import kotlin.math.min

enum class OrderBookDisplayMode {
    BOTH, BIDS_ONLY, ASKS_ONLY
}

@Composable
fun OrderBookView(
    orderBook: OrderBook,
    lastPrice: Double,
    isPriceUp: Boolean,
    onPriceClick: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var displayMode by remember { mutableStateOf(OrderBookDisplayMode.BOTH) }

    Column(modifier = modifier) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Price (USDT)", fontSize = 10.sp, color = BinanceTextMuted)
            Text(text = "Amount", fontSize = 10.sp, color = BinanceTextMuted)
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Asks (Red)
        if (displayMode == OrderBookDisplayMode.BOTH || displayMode == OrderBookDisplayMode.ASKS_ONLY) {
            val visibleAsks = orderBook.asks.take(if (displayMode == OrderBookDisplayMode.BOTH) 6 else 12).reversed()
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                visibleAsks.forEach { entry ->
                    OrderBookRow(
                        entry = entry,
                        isAsk = true,
                        onClick = { onPriceClick(entry.price) }
                    )
                }
            }
        }

        // Center Price Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val priceColor = if (isPriceUp) BinanceGreen else BinanceRed
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.2f", lastPrice),
                    fontSize = 15.sp,
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = priceColor
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = if (isPriceUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = priceColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = "$${String.format("%.2f", lastPrice)}",
                fontSize = 10.sp,
                color = BinanceTextMuted,
                fontFamily = RobotoMonoFontFamily
            )
        }

        // Bids (Green)
        if (displayMode == OrderBookDisplayMode.BOTH || displayMode == OrderBookDisplayMode.BIDS_ONLY) {
            val visibleBids = orderBook.bids.take(if (displayMode == OrderBookDisplayMode.BOTH) 6 else 12)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                visibleBids.forEach { entry ->
                    OrderBookRow(
                        entry = entry,
                        isAsk = false,
                        onClick = { onPriceClick(entry.price) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderBookRow(
    entry: OrderBookEntry,
    isAsk: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isAsk) BinanceRed else BinanceGreen
    val depthBarColor = if (isAsk) BinanceRedBg else BinanceGreenBg

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .clickable { onClick() }
    ) {
        // Depth background bar extending from right to left
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(min(entry.percentDepth, 1.0f))
                .align(Alignment.CenterEnd)
                .background(depthBarColor)
        )

        // Text content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%.2f", entry.price),
                fontSize = 11.sp,
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = String.format("%.4f", entry.amount),
                fontSize = 11.sp,
                fontFamily = RobotoMonoFontFamily,
                color = BinanceTextSecondary
            )
        }
    }
}
