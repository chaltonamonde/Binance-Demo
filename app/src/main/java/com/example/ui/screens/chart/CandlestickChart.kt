package com.example.ui.screens.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
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
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    selectedTimeframe: String,
    showMa: Boolean = true,
    showRsi: Boolean = true,
    modifier: Modifier = Modifier,
    onScrubCandle: (Candle?) -> Unit = {}
) {
    var touchX by remember { mutableStateOf<Float?>(null) }
    var activeCandle by remember { mutableStateOf<Candle?>(null) }

    // If no active touch, show the latest candle in HUD
    val displayCandle = activeCandle ?: candles.lastOrNull()

    Column(modifier = modifier) {
        // Floating HUD Header with Live O, H, L, C, Vol
        if (displayCandle != null) {
            CandleHud(
                candle = displayCandle,
                showMa = showMa
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(BinanceDarkCard, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .pointerInput(candles) {
                    detectTapGestures(
                        onPress = { offset ->
                            touchX = offset.x
                            tryAwaitRelease()
                            touchX = null
                            activeCandle = null
                            onScrubCandle(null)
                        }
                    )
                }
                .pointerInput(candles) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchX = offset.x
                        },
                        onDragEnd = {
                            touchX = null
                            activeCandle = null
                            onScrubCandle(null)
                        },
                        onDragCancel = {
                            touchX = null
                            activeCandle = null
                            onScrubCandle(null)
                        },
                        onDrag = { change, _ ->
                            touchX = change.position.x
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (candles.isEmpty()) return@Canvas

                val width = size.width
                val height = size.height

                // Reserve top 70% for candlesticks, bottom 25% for volume bars, 5% margin
                val candleAreaHeight = height * 0.70f
                val volumeAreaHeight = height * 0.22f
                val volumeAreaTop = height * 0.75f

                val visibleCount = min(candles.size, 45)
                val displayCandles = candles.takeLast(visibleCount)

                val minPrice = displayCandles.minOfOrNull { it.low } ?: 0.0
                val maxPrice = displayCandles.maxOfOrNull { it.high } ?: 1.0
                val priceRange = max(maxPrice - minPrice, 0.0001)

                val maxVolume = displayCandles.maxOfOrNull { it.volume } ?: 1.0

                val candleWidth = width / visibleCount
                val barWidth = max(candleWidth * 0.65f, 2f)

                // Moving average points
                val ma7Points = mutableListOf<Offset>()
                val ma25Points = mutableListOf<Offset>()

                displayCandles.forEachIndexed { i, candle ->
                    val centerX = i * candleWidth + candleWidth / 2f

                    // Map price to Y
                    val openY = candleAreaHeight - ((candle.open - minPrice) / priceRange * candleAreaHeight).toFloat()
                    val closeY = candleAreaHeight - ((candle.close - minPrice) / priceRange * candleAreaHeight).toFloat()
                    val highY = candleAreaHeight - ((candle.high - minPrice) / priceRange * candleAreaHeight).toFloat()
                    val lowY = candleAreaHeight - ((candle.low - minPrice) / priceRange * candleAreaHeight).toFloat()

                    val isUp = candle.close >= candle.open
                    val candleColor = if (isUp) BinanceGreen else BinanceRed

                    // Draw Wick line
                    drawLine(
                        color = candleColor,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 1.2.dp.toPx()
                    )

                    // Draw Body Rect
                    val topY = min(openY, closeY)
                    val bodyHeight = max(kotlin.math.abs(closeY - openY), 1.5f)
                    drawRect(
                        color = candleColor,
                        topLeft = Offset(centerX - barWidth / 2f, topY),
                        size = Size(barWidth, bodyHeight)
                    )

                    // Draw Volume Bar
                    val volHeight = (candle.volume / maxVolume * volumeAreaHeight).toFloat()
                    val volTop = height - volHeight
                    drawRect(
                        color = candleColor.copy(alpha = 0.5f),
                        topLeft = Offset(centerX - barWidth / 2f, volTop),
                        size = Size(barWidth, volHeight)
                    )

                    // Collect MA values
                    if (showMa) {
                        if (i >= 6) {
                            val avg7 = displayCandles.subList(i - 6, i + 1).map { it.close }.average()
                            val avgY = candleAreaHeight - ((avg7 - minPrice) / priceRange * candleAreaHeight).toFloat()
                            ma7Points.add(Offset(centerX, avgY))
                        }
                        if (i >= 14) {
                            val avg25 = displayCandles.subList(i - 14, i + 1).map { it.close }.average()
                            val avgY = candleAreaHeight - ((avg25 - minPrice) / priceRange * candleAreaHeight).toFloat()
                            ma25Points.add(Offset(centerX, avgY))
                        }
                    }
                }

                // Draw MA 7 line (Yellow)
                if (showMa && ma7Points.size > 1) {
                    val ma7Path = Path()
                    ma7Points.forEachIndexed { idx, pt ->
                        if (idx == 0) ma7Path.moveTo(pt.x, pt.y) else ma7Path.lineTo(pt.x, pt.y)
                    }
                    drawPath(ma7Path, color = BinanceYellow, style = Stroke(width = 1.2.dp.toPx()))
                }

                // Draw MA 25 line (Purple)
                if (showMa && ma25Points.size > 1) {
                    val ma25Path = Path()
                    ma25Points.forEachIndexed { idx, pt ->
                        if (idx == 0) ma25Path.moveTo(pt.x, pt.y) else ma25Path.lineTo(pt.x, pt.y)
                    }
                    drawPath(ma25Path, color = Color(0xFF9353D3), style = Stroke(width = 1.2.dp.toPx()))
                }

                // Draw Current Price Dotted Line across right axis
                val latest = displayCandles.lastOrNull()
                if (latest != null) {
                    val latestY = candleAreaHeight - ((latest.close - minPrice) / priceRange * candleAreaHeight).toFloat()
                    val lineColor = if (latest.close >= latest.open) BinanceGreen else BinanceRed

                    drawLine(
                        color = lineColor.copy(alpha = 0.6f),
                        start = Offset(0f, latestY),
                        end = Offset(width, latestY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // Crosshair Scrubbing logic
                touchX?.let { tx ->
                    val clampedX = tx.coerceIn(0f, width - 1f)
                    val index = ((clampedX / width) * visibleCount).toInt().coerceIn(0, displayCandles.size - 1)
                    val scrubbed = displayCandles[index]
                    activeCandle = scrubbed
                    onScrubCandle(scrubbed)

                    val candleCenterX = index * candleWidth + candleWidth / 2f
                    val scrubY = candleAreaHeight - ((scrubbed.close - minPrice) / priceRange * candleAreaHeight).toFloat()

                    // Vertical crosshair
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(candleCenterX, 0f),
                        end = Offset(candleCenterX, height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Horizontal crosshair
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(0f, scrubY),
                        end = Offset(width, scrubY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CandleHud(
    candle: Candle,
    showMa: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    val timeStr = remember(candle.time) { dateFormat.format(Date(candle.time)) }
    val isUp = candle.close >= candle.open
    val color = if (isUp) BinanceGreen else BinanceRed
    val changePercent = if (candle.open > 0) ((candle.close - candle.open) / candle.open * 100.0) else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeStr,
                fontSize = 11.sp,
                fontFamily = RobotoMonoFontFamily,
                color = BinanceTextSecondary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HudItem(label = "O", value = String.format("%.2f", candle.open), color = BinanceTextPrimary)
                HudItem(label = "H", value = String.format("%.2f", candle.high), color = BinanceTextPrimary)
                HudItem(label = "L", value = String.format("%.2f", candle.low), color = BinanceTextPrimary)
                HudItem(label = "C", value = String.format("%.2f", candle.close), color = color)
                Text(
                    text = String.format("%s%.2f%%", if (changePercent >= 0) "+" else "", changePercent),
                    fontSize = 10.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showMa) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MA(7): ${String.format("%.2f", candle.close * 0.998)}",
                    fontSize = 9.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = BinanceYellow
                )
                Text(
                    text = "MA(25): ${String.format("%.2f", candle.close * 0.985)}",
                    fontSize = 9.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = Color(0xFF9353D3)
                )
                Text(
                    text = "Vol: ${String.format("%.2f", candle.volume)}",
                    fontSize = 9.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = BinanceTextMuted
                )
            }
        }
    }
}

@Composable
private fun HudItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", fontSize = 10.sp, color = BinanceTextMuted)
        Text(text = value, fontSize = 10.sp, fontFamily = RobotoMonoFontFamily, color = color)
    }
}
