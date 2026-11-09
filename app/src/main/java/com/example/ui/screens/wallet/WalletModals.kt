package com.example.ui.screens.wallet

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalletAddress
import com.example.ui.BinanceViewModel
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
import kotlin.math.abs

@Composable
fun MockQrCode(
    content: String,
    modifier: Modifier = Modifier.size(160.dp)
) {
    Canvas(modifier = modifier.background(Color.White, RoundedCornerShape(10.dp))) {
        val count = 21
        val cellSize = size.width / count
        val hash = content.hashCode()

        for (r in 0 until count) {
            for (c in 0 until count) {
                // Draw 3 corner locator squares
                val isTopLeftCorner = (r < 7 && c < 7)
                val isTopRightCorner = (r < 7 && c >= count - 7)
                val isBottomLeftCorner = (r >= count - 7 && c < 7)

                val shouldDraw = if (isTopLeftCorner || isTopRightCorner || isBottomLeftCorner) {
                    val localR = if (r >= count - 7) r - (count - 7) else r
                    val localC = if (c >= count - 7) c - (count - 7) else c
                    (localR == 0 || localR == 6 || localC == 0 || localC == 6) || (localR in 2..4 && localC in 2..4)
                } else {
                    abs((r * 31 + c * 17 + hash) % 5) < 2
                }

                if (shouldDraw) {
                    drawRect(
                        color = Color(0xFF181A20),
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositBottomSheet(
    viewModel: BinanceViewModel,
    addresses: List<WalletAddress>,
    onDismiss: () -> Unit
) {
    var selectedCoin by remember { mutableStateOf("USDT") }
    var selectedAddress by remember { mutableStateOf(addresses.first()) }
    var copied by remember { mutableStateOf(false) }
    val clipboard: ClipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BinanceDarkCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Deposit Crypto",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Coin selector row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("USDT", "BTC", "ETH", "BNB", "SOL").forEach { coin ->
                    val isSelected = selectedCoin == coin
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) BinanceYellow else BinanceDarkElevated)
                            .clickable {
                                selectedCoin = coin
                                selectedAddress = when (coin) {
                                    "BTC" -> addresses.find { it.chainName == "Bitcoin" } ?: addresses.first()
                                    "SOL" -> addresses.find { it.chainName == "Solana" } ?: addresses.first()
                                    else -> addresses.first()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coin,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else BinanceTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // QR Code
            MockQrCode(content = selectedAddress.address)

            Spacer(modifier = Modifier.height(12.dp))

            // Network Badge
            Text(
                text = "Network: ${selectedAddress.network}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BinanceYellow
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Address display card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BinanceDarkInput),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Wallet Address", fontSize = 10.sp, color = BinanceTextMuted)
                        Text(
                            text = selectedAddress.address,
                            fontSize = 11.sp,
                            fontFamily = RobotoMonoFontFamily,
                            color = BinanceTextPrimary
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(selectedAddress.address))
                            copied = true
                        }
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = "Copy Address",
                            tint = if (copied) BinanceGreen else BinanceYellow
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Simulate Free Testnet Deposit CTA
            Button(
                onClick = {
                    val amount = when (selectedCoin) {
                        "USDT" -> 5000.0
                        "BTC" -> 0.10
                        "ETH" -> 1.00
                        "BNB" -> 5.00
                        else -> 10.0
                    }
                    viewModel.simulateDeposit(selectedCoin, amount, selectedAddress.network)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("simulate_deposit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Deposit Testnet $selectedCoin (+funds)",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawBottomSheet(
    viewModel: BinanceViewModel,
    onDismiss: () -> Unit
) {
    var selectedCoin by remember { mutableStateOf("USDT") }
    var addressInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var showPinInput by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BinanceDarkCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Send & Withdraw Crypto",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Coin selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("USDT", "BTC", "ETH", "BNB").forEach { coin ->
                    val isSelected = selectedCoin == coin
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) BinanceYellow else BinanceDarkElevated)
                            .clickable { selectedCoin = coin },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = coin,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else BinanceTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Destination address input
            OutlinedTextField(
                value = addressInput,
                onValueChange = { addressInput = it },
                label = { Text("Recipient Address", fontSize = 11.sp, color = BinanceTextMuted) },
                placeholder = { Text("Long press or paste address", fontSize = 11.sp, color = BinanceTextMuted) },
                trailingIcon = {
                    Text(
                        text = "PASTE",
                        color = BinanceYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { addressInput = "0x49Ac7E90B01D92c8152cDb2a71a9382193" }
                            .padding(end = 12.dp)
                    )
                },
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
                    .height(54.dp)
                    .testTag("withdraw_address_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Amount input
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Withdraw Amount ($selectedCoin)", fontSize = 11.sp, color = BinanceTextMuted) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                trailingIcon = {
                    Text(
                        text = "MAX",
                        color = BinanceYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { amountInput = "500" }
                            .padding(end = 12.dp)
                    )
                },
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
                    .height(54.dp)
                    .testTag("withdraw_amount_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Network fee card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BinanceDarkInput),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Network Fee:", fontSize = 11.sp, color = BinanceTextMuted)
                    Text(text = "0.80 USDT (BSC BEP20)", fontSize = 11.sp, fontFamily = RobotoMonoFontFamily, color = BinanceTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4-digit PIN verification input
            OutlinedTextField(
                value = pinInput,
                onValueChange = { if (it.length <= 4) pinInput = it },
                label = { Text("Security PIN (Default: 1234)", fontSize = 11.sp, color = BinanceTextMuted) },
                visualTransformation = if (showPinInput) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPinInput = !showPinInput }) {
                        Icon(
                            imageVector = if (showPinInput) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle PIN",
                            tint = BinanceTextSecondary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                    .height(54.dp)
                    .testTag("withdraw_pin_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 100.0
                    val addr = addressInput.ifBlank { "0x49Ac7E90B01D92c8152cDb2a71a9382193" }
                    viewModel.simulateWithdraw(selectedCoin, amt, addr, "Binance Smart Chain (BEP20)", pinInput)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("confirm_withdraw_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Confirm Withdrawal", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferBottomSheet(
    viewModel: BinanceViewModel,
    onDismiss: () -> Unit
) {
    var fromAccount by remember { mutableStateOf("Spot") }
    var toAccount by remember { mutableStateOf("Funding") }
    var amountInput by remember { mutableStateOf("1000") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BinanceDarkCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Transfer Between Wallets",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BinanceTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BinanceDarkInput),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "From", fontSize = 11.sp, color = BinanceTextMuted)
                        Text(text = fromAccount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BinanceYellow)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "To", fontSize = 11.sp, color = BinanceTextMuted)
                        Text(text = toAccount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BinanceGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount (USDT)", fontSize = 11.sp, color = BinanceTextMuted) },
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
                    .height(54.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 100.0
                    viewModel.simulateTransfer(fromAccount, toAccount, "USDT", amt)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BinanceYellow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Confirm Transfer", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityCenterBottomSheet(
    viewModel: BinanceViewModel,
    addresses: List<WalletAddress>,
    onDismiss: () -> Unit
) {
    var showSeedPhrase by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = BinanceDarkCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = BinanceYellow, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security & Multi-Chain Wallet",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BinanceTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status items
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BinanceDarkInput)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "PIN & Biometric Lock", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = BinanceTextPrimary)
                    Text(text = "Protected for withdrawals and orders", fontSize = 11.sp, color = BinanceTextMuted)
                }
                Text(text = "Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BinanceGreen)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 12-Word Recovery Seed Phrase
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BinanceDarkInput),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "12-Word Recovery Phrase", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BinanceTextPrimary)
                        IconButton(onClick = { showSeedPhrase = !showSeedPhrase }) {
                            Icon(
                                imageVector = if (showSeedPhrase) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Phrase",
                                tint = BinanceYellow
                            )
                        }
                    }

                    if (showSeedPhrase) {
                        val words = viewModel.seedPhrase.split(" ")
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            words.chunked(3).forEach { chunk ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    chunk.forEach { w ->
                                        Text(
                                            text = "• $w",
                                            fontSize = 12.sp,
                                            fontFamily = RobotoMonoFontFamily,
                                            color = BinanceYellow
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Tap the eye icon to view your backed-up seed phrase.",
                            fontSize = 11.sp,
                            color = BinanceTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-chain addresses list
            Text(text = "Supported Networks & Addresses", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BinanceTextPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            addresses.take(3).forEach { addr ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = addr.chainName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BinanceTextPrimary)
                    Text(
                        text = "${addr.address.take(8)}...${addr.address.takeLast(6)}",
                        fontSize = 11.sp,
                        fontFamily = RobotoMonoFontFamily,
                        color = BinanceTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
