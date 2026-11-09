package com.example.ui.screens.wallet

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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.WalletAsset
import com.example.data.model.WalletTransaction
import com.example.ui.BinanceTab
import com.example.ui.BinanceViewModel
import com.example.ui.components.CustomBinanceTab
import com.example.ui.theme.BinanceDarkBorder
import com.example.ui.theme.BinanceDarkCard
import com.example.ui.theme.BinanceDarkElevated
import com.example.ui.theme.BinanceGold
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
fun WalletScreen(
    viewModel: BinanceViewModel,
    onNavigateToTrade: (String) -> Unit
) {
    val totalNetWorth by viewModel.totalNetWorthUsd.collectAsState()
    val isBalanceHidden by viewModel.isBalanceHidden.collectAsState()
    val walletAssets by viewModel.walletAssets.collectAsState()
    val fundingBalances by viewModel.fundingBalances.collectAsState()
    val earnBalances by viewModel.earnBalances.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val allTickers by viewModel.tickers.collectAsState()

    val showDepositSheet by viewModel.showDepositSheet.collectAsState()
    val showWithdrawSheet by viewModel.showWithdrawSheet.collectAsState()
    val showTransferSheet by viewModel.showTransferSheet.collectAsState()
    val showSecuritySheet by viewModel.showSecuritySheet.collectAsState()

    var selectedSubWallet by remember { mutableStateOf("Spot") }

    val btcTicker = allTickers.find { it.symbol == "BTCUSDT" }
    val btcPrice = btcTicker?.lastPrice ?: 92450.0
    val btcEquivalent = if (btcPrice > 0) totalNetWorth / btcPrice else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wallet_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Wallet Top Balance Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Estimated Value",
                                fontSize = 13.sp,
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
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Security Center button
                        IconButton(
                            onClick = { viewModel.setSecuritySheet(true) },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("security_center_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security Settings",
                                tint = BinanceYellow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isBalanceHidden) "********" else "$${String.format("%,.2f", totalNetWorth)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = RobotoMonoFontFamily,
                        color = BinanceTextPrimary
                    )

                    Text(
                        text = if (isBalanceHidden) "*** BTC" else "≈ ${String.format("%.4f", btcEquivalent)} BTC",
                        fontSize = 13.sp,
                        color = BinanceTextSecondary,
                        fontFamily = RobotoMonoFontFamily
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons (Deposit, Withdraw, Transfer)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setDepositSheet(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("wallet_deposit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Deposit", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.setWithdrawSheet(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("wallet_withdraw_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceDarkElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Withdraw", color = BinanceTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { viewModel.setTransferSheet(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("wallet_transfer_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BinanceDarkElevated),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Transfer", color = BinanceTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Sub-Wallet Tabs: Spot, Funding, Earn
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                CustomBinanceTab(
                    title = "Spot",
                    isSelected = selectedSubWallet == "Spot",
                    onClick = { selectedSubWallet = "Spot" }
                )
                CustomBinanceTab(
                    title = "Funding",
                    isSelected = selectedSubWallet == "Funding",
                    onClick = { selectedSubWallet = "Funding" }
                )
                CustomBinanceTab(
                    title = "Earn",
                    isSelected = selectedSubWallet == "Earn",
                    onClick = { selectedSubWallet = "Earn" }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Assets List for selected sub-wallet
        if (selectedSubWallet == "Spot") {
            items(walletAssets, key = { it.symbol }) { asset ->
                WalletAssetRow(
                    asset = asset,
                    isBalanceHidden = isBalanceHidden,
                    onClick = {
                        val sym = "${asset.symbol}USDT"
                        viewModel.selectSymbol(sym)
                        onNavigateToTrade(sym)
                    }
                )
            }
        } else if (selectedSubWallet == "Funding") {
            items(fundingBalances.entries.toList(), key = { it.key }) { entry ->
                FundingAssetRow(
                    symbol = entry.key,
                    amount = entry.value,
                    isBalanceHidden = isBalanceHidden
                )
            }
        } else {
            items(earnBalances.entries.toList(), key = { it.key }) { entry ->
                EarnAssetRow(
                    symbol = entry.key,
                    amount = entry.value,
                    isBalanceHidden = isBalanceHidden
                )
            }
        }

        // Recent Wallet Transactions Header
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Recent Transactions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Transactions List
        items(transactions, key = { it.id }) { tx ->
            WalletTransactionCard(tx = tx)
        }
    }

    // Bottom Sheets
    if (showDepositSheet) {
        DepositBottomSheet(
            viewModel = viewModel,
            addresses = viewModel.walletAddresses,
            onDismiss = { viewModel.setDepositSheet(false) }
        )
    }

    if (showWithdrawSheet) {
        WithdrawBottomSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.setWithdrawSheet(false) }
        )
    }

    if (showTransferSheet) {
        TransferBottomSheet(
            viewModel = viewModel,
            onDismiss = { viewModel.setTransferSheet(false) }
        )
    }

    if (showSecuritySheet) {
        SecurityCenterBottomSheet(
            viewModel = viewModel,
            addresses = viewModel.walletAddresses,
            onDismiss = { viewModel.setSecuritySheet(false) }
        )
    }
}

@Composable
fun WalletAssetRow(
    asset: WalletAsset,
    isBalanceHidden: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BinanceDarkElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = asset.symbol.take(2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceYellow
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = asset.symbol,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextPrimary
                )
                Text(
                    text = asset.name,
                    fontSize = 11.sp,
                    color = BinanceTextMuted
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val amountStr = if (isBalanceHidden) "******" else String.format("%.4f", asset.totalAmount)
            Text(
                text = amountStr,
                fontSize = 14.sp,
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = BinanceTextPrimary
            )
            val valStr = if (isBalanceHidden) "*** USD" else "$${String.format("%,.2f", asset.usdValue)}"
            Text(
                text = valStr,
                fontSize = 11.sp,
                fontFamily = RobotoMonoFontFamily,
                color = BinanceTextSecondary
            )
        }
    }
}

@Composable
fun FundingAssetRow(
    symbol: String,
    amount: Double,
    isBalanceHidden: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BinanceDarkElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(text = symbol.take(2), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BinanceYellow)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BinanceTextPrimary)
        }

        Text(
            text = if (isBalanceHidden) "******" else String.format("%.4f", amount),
            fontSize = 14.sp,
            fontFamily = RobotoMonoFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = BinanceTextPrimary
        )
    }
}

@Composable
fun EarnAssetRow(
    symbol: String,
    amount: Double,
    isBalanceHidden: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BinanceDarkElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(text = symbol.take(2), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BinanceGold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BinanceTextPrimary)
                Text(text = "APY 18.5% (Flexible)", fontSize = 11.sp, color = BinanceGreen)
            }
        }

        Text(
            text = if (isBalanceHidden) "******" else String.format("%.4f", amount),
            fontSize = 14.sp,
            fontFamily = RobotoMonoFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = BinanceTextPrimary
        )
    }
}

@Composable
fun WalletTransactionCard(tx: WalletTransaction) {
    val isPositive = tx.type in listOf("Deposit", "Spot Sell")
    val color = if (isPositive) BinanceGreen else BinanceTextPrimary
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val timeFormatted = remember(tx.timestamp) { dateFormat.format(Date(tx.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BinanceDarkCard),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tx.type,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BinanceTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tx.status,
                        fontSize = 10.sp,
                        color = BinanceGreen,
                        modifier = Modifier
                            .background(BinanceGreen.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = BinanceTextMuted
                )
                Text(
                    text = "Tx: ${tx.txHash}",
                    fontSize = 9.sp,
                    fontFamily = RobotoMonoFontFamily,
                    color = BinanceTextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else "-"}${String.format("%.4f", tx.amount)} ${tx.asset}",
                    fontSize = 13.sp,
                    fontFamily = RobotoMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = tx.network,
                    fontSize = 10.sp,
                    color = BinanceTextMuted
                )
            }
        }
    }
}
