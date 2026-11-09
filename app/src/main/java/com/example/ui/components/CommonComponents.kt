package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BinanceDarkBorder
import com.example.ui.theme.BinanceDarkCard
import com.example.ui.theme.BinanceDarkElevated
import com.example.ui.theme.BinanceDarkInput
import com.example.ui.theme.BinanceGold
import com.example.ui.theme.BinanceGreen
import com.example.ui.theme.BinanceGreenBg
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceRedBg
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import com.example.ui.theme.RobotoMonoFontFamily
import kotlin.math.max
import kotlin.math.min

@Composable
fun BinanceTopHeader(
    onSearchClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // User Profile Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onProfileClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BinanceDarkElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = BinanceGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VIP 1",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceDarkCard,
                        modifier = Modifier
                            .background(BinanceYellow, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified",
                        fontSize = 11.sp,
                        color = BinanceGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Search Bar Pill
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onSearchClick() }
                .testTag("header_search_bar"),
            color = BinanceDarkInput,
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = BinanceTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BTC, ETH, BNB...",
                    color = BinanceTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Header Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onScanClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan QR",
                    tint = BinanceTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(36.dp)
            ) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = BinanceRed) {
                            Text("3", fontSize = 9.sp, color = Color.White)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = BinanceTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriceChangePill(
    changePercent: Double,
    modifier: Modifier = Modifier,
    minWidth: Dp = 72.dp
) {
    val isPositive = changePercent >= 0
    val bgColor = if (isPositive) BinanceGreen else BinanceRed
    val prefix = if (isPositive) "+" else ""
    val text = String.format("%s%.2f%%", prefix, changePercent)

    Box(
        modifier = modifier
            .width(minWidth)
            .height(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = RobotoMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun Sparkline(
    points: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
        .width(60.dp)
        .height(24.dp)
) {
    if (points.size < 2) return

    val lineColor = if (isPositive) BinanceGreen else BinanceRed

    Canvas(modifier = modifier) {
        val minVal = points.minOrNull() ?: 0.0
        val maxVal = points.maxOrNull() ?: 1.0
        val range = max(maxVal - minVal, 0.000001)

        val path = Path()
        val width = size.width
        val height = size.height
        val stepX = width / (points.size - 1)

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = (value - minVal) / range
            val y = height - (normalizedY * height).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
fun QuickServiceItem(
    icon: ImageVector,
    label: String,
    iconTint: Color = BinanceGold,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BinanceDarkElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = BinanceTextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CustomBinanceTab(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeText: String? = null
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) BinanceTextPrimary else BinanceTextSecondary
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    color = BinanceYellow,
                    modifier = Modifier
                        .background(BinanceDarkElevated, RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(20.dp)
                .background(if (isSelected) BinanceYellow else Color.Transparent, RoundedCornerShape(2.dp))
        )
    }
}

@Composable
fun FavoriteStarButton(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.size(32.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
            tint = if (isFavorite) BinanceYellow else BinanceTextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
