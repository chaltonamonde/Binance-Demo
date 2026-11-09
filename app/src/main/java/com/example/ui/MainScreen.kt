package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CandlestickChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.chart.ChartScreen
import com.example.ui.screens.futures.FuturesScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.markets.MarketsScreen
import com.example.ui.screens.trade.TradeScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.BinanceDarkBorder
import com.example.ui.theme.BinanceDarkCard
import com.example.ui.theme.BinanceDarkElevated
import com.example.ui.theme.BinanceRed
import com.example.ui.theme.BinanceTextPrimary
import com.example.ui.theme.BinanceTextSecondary
import com.example.ui.theme.BinanceYellow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(
    viewModel: BinanceViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isChartFullscreen by viewModel.isChartFullscreen.collectAsState()
    val selectedSymbol by viewModel.selectedSymbol.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collectLatest { msg ->
            snackbarHostState.showSnackbar(message = msg.text)
        }
    }

    // Hardware back handler
    BackHandler(enabled = isChartFullscreen || currentTab != BinanceTab.HOME) {
        if (isChartFullscreen) {
            viewModel.setChartFullscreen(false)
        } else {
            viewModel.setTab(BinanceTab.HOME)
        }
    }

    if (isChartFullscreen) {
        ChartScreen(
            symbol = selectedSymbol,
            viewModel = viewModel,
            onBackClick = { viewModel.setChartFullscreen(false) },
            onNavigateToTrade = { sym ->
                viewModel.selectSymbol(sym)
                viewModel.setChartFullscreen(false)
                viewModel.setTab(BinanceTab.TRADE)
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = com.example.ui.theme.BinanceDarkBg,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = BinanceDarkElevated,
                        contentColor = BinanceTextPrimary,
                        actionColor = BinanceYellow
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = BinanceDarkCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("binance_bottom_navigation")
                ) {
                    val tabs = listOf(
                        BinanceTab.HOME to ("Home" to (Icons.Filled.Home to Icons.Outlined.Home)),
                        BinanceTab.MARKETS to ("Markets" to (Icons.Filled.ShowChart to Icons.Outlined.ShowChart)),
                        BinanceTab.TRADE to ("Trade" to (Icons.Filled.CandlestickChart to Icons.Outlined.CandlestickChart)),
                        BinanceTab.FUTURES to ("Futures" to (Icons.Filled.Bolt to Icons.Outlined.Bolt)),
                        BinanceTab.WALLETS to ("Wallets" to (Icons.Filled.AccountBalanceWallet to Icons.Outlined.AccountBalanceWallet))
                    )

                    tabs.forEach { (tab, details) ->
                        val (label, icons) = details
                        val isSelected = currentTab == tab
                        val icon = if (isSelected) icons.first else icons.second

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BinanceYellow,
                                selectedTextColor = BinanceYellow,
                                unselectedIconColor = BinanceTextSecondary,
                                unselectedTextColor = BinanceTextSecondary,
                                indicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("nav_tab_${label.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    BinanceTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTrade = { sym ->
                            viewModel.selectSymbol(sym)
                            viewModel.setTab(BinanceTab.TRADE)
                        },
                        onNavigateToChart = { sym ->
                            viewModel.selectSymbol(sym)
                            viewModel.setChartFullscreen(true)
                        }
                    )
                    BinanceTab.MARKETS -> MarketsScreen(
                        viewModel = viewModel,
                        onNavigateToTrade = { sym ->
                            viewModel.selectSymbol(sym)
                            viewModel.setTab(BinanceTab.TRADE)
                        },
                        onNavigateToChart = { sym ->
                            viewModel.selectSymbol(sym)
                            viewModel.setChartFullscreen(true)
                        }
                    )
                    BinanceTab.TRADE -> TradeScreen(
                        viewModel = viewModel,
                        onNavigateToChart = { sym ->
                            viewModel.selectSymbol(sym)
                            viewModel.setChartFullscreen(true)
                        }
                    )
                    BinanceTab.FUTURES -> FuturesScreen(
                        viewModel = viewModel
                    )
                    BinanceTab.WALLETS -> WalletScreen(
                        viewModel = viewModel,
                        onNavigateToTrade = { sym ->
                            viewModel.selectSymbol(sym)
                            viewModel.setTab(BinanceTab.TRADE)
                        }
                    )
                }
            }
        }
    }
}
