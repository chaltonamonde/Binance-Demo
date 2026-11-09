package com.example.ui.screens.markets

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CryptoTicker
import com.example.ui.BinanceViewModel
import com.example.ui.components.CustomBinanceTab
import com.example.ui.components.PriceChangePill
import com.example.ui.components.Sparkline
import com.example.ui.theme.BinanceDarkBorder
import com.example.ui.theme.BinanceDarkCard
import com.example.ui.theme.BinanceDarkElevated
import com.example.ui.theme.BinanceDarkInput
import com.example.ui.theme.BinanceTextMuted
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import com.example.ui.theme.RobotoMonoFontFamily

enum class MarketSubTab {
    ALL, FAVORITES, LAYER_1, DEFI, AI_COINS, MEMES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketsScreen(
    viewModel: BinanceViewModel,
    onNavigateToTrade: (String) -> Unit,
    onNavigateToChart: (String) -> Unit
) {
    val tickers by viewModel.tickers.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTab by remember { mutableStateOf(MarketSubTab.ALL) }

    val filteredList = remember(tickers, searchQuery, selectedTab, favorites) {
        var list = if (searchQuery.isBlank()) tickers else {
            tickers.filter {
                it.symbol.contains(searchQuery, ignoreCase = true) ||
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }

        when (selectedTab) {
            MarketSubTab.ALL -> list
            MarketSubTab.FAVORITES -> list.filter { favorites.contains(it.symbol) }
            MarketSubTab.LAYER_1 -> list.filter { it.symbol in listOf("BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "AVAXUSDT", "SUIUSDT", "ADAUSDT", "NEARUSDT") }
            MarketSubTab.DEFI -> list.filter { it.symbol in listOf("LINKUSDT", "AVAXUSDT", "ETHUSDT") }
            MarketSubTab.AI_COINS -> list.filter { it.symbol in listOf("NEARUSDT", "SUIUSDT") }
            MarketSubTab.MEMES -> list.filter { it.symbol in listOf("DOGEUSDT", "PEPEUSDT") }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("markets_screen")
    ) {
        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(text = "Search coin name or pair...", fontSize = 13.sp, color = BinanceTextMuted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BinanceTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = BinanceTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BinanceDarkInput,
                    unfocusedContainerColor = BinanceDarkInput,
                    focusedBorderColor = BinanceYellow,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = BinanceTextPrimary,
                    unfocusedTextColor = BinanceTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("markets_search_input")
            )
        }

        // Sub Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                CustomBinanceTab(
                    title = "All Spot",
                    isSelected = selectedTab == MarketSubTab.ALL,
                    onClick = { selectedTab = MarketSubTab.ALL }
                )
            }
            item {
                CustomBinanceTab(
                    title = "Favorites",
                    isSelected = selectedTab == MarketSubTab.FAVORITES,
                    onClick = { selectedTab = MarketSubTab.FAVORITES },
                    badgeText = "${favorites.size}"
                )
            }
            item {
                CustomBinanceTab(
                    title = "Layer 1",
                    isSelected = selectedTab == MarketSubTab.LAYER_1,
                    onClick = { selectedTab = MarketSubTab.LAYER_1 }
                )
            }
            item {
                CustomBinanceTab(
                    title = "DeFi",
                    isSelected = selectedTab == MarketSubTab.DEFI,
                    onClick = { selectedTab = MarketSubTab.DEFI }
                )
            }
            item {
                CustomBinanceTab(
                    title = "AI",
                    isSelected = selectedTab == MarketSubTab.AI_COINS,
                    onClick = { selectedTab = MarketSubTab.AI_COINS }
                )
            }
            item {
                CustomBinanceTab(
                    title = "Memes",
                    isSelected = selectedTab == MarketSubTab.MEMES,
                    onClick = { selectedTab = MarketSubTab.MEMES }
                )
            }
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Pair / Vol", fontSize = 11.sp, color = BinanceTextMuted)
            Text(text = "Last Price", fontSize = 11.sp, color = BinanceTextMuted)
            Text(text = "24h Chg%", fontSize = 11.sp, color = BinanceTextMuted)
        }

        // Coins List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredList, key = { it.symbol }) { ticker ->
                MarketCoinRow(
                    ticker = ticker,
                    isFavorite = favorites.contains(ticker.symbol),
                    onToggleFavorite = { viewModel.toggleFavorite(ticker.symbol) },
                    onClick = {
                        viewModel.selectSymbol(ticker.symbol)
                        onNavigateToTrade(ticker.symbol)
                    }
                )
            }
        }
    }
}

@Composable
fun MarketCoinRow(
    ticker: CryptoTicker,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Star & Pair
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1.3f)
        ) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Star",
                    tint = if (isFavorite) BinanceYellow else BinanceTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
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
                        fontSize = 11.sp,
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

        // Sparkline curve
        Box(
            modifier = Modifier
                .width(60.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Sparkline(
                points = ticker.sparkline,
                isPositive = ticker.priceChangePercent >= 0
            )
        }

        // Last Price
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1.1f)
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
                .weight(0.9f)
                .padding(start = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            PriceChangePill(
                changePercent = ticker.priceChangePercent,
                minWidth = 66.dp
            )
        }
    }
}
