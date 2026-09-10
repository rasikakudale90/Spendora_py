package com.spendora.app.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.ChatMessage
import com.spendora.app.data.model.FinancialActionIntent
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.AiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialAssistantScreen(
    viewModel: AiViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    Scaffold(
        containerColor = SpendoraTheme.colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SpendoraTheme.colors.primary.copy(alpha = 0.15f))
                                .border(1.dp, SpendoraTheme.colors.primary.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SpendoraTheme.colors.primaryLight,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Spendora AI Advisor",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                ),
                                color = SpendoraTheme.colors.textPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SpendoraTheme.colors.emerald)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "In-Database RAG • Grounded Facts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SpendoraTheme.colors.emerald
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SpendoraTheme.colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SpendoraTheme.colors.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.chatMessages) { message ->
                    ChatMessageBubble(
                        message = message,
                        onActionClick = { intent ->
                            if (intent.action == "simulate_purchase" && intent.payload != null) {
                                val title = intent.payload["title"]?.toString() ?: "Item"
                                val amount = (intent.payload["amount"] as? Number)?.toDouble() ?: 1000.0
                                viewModel.sendChatMessage("Simulate purchase for $title costing $amount")
                            } else if (intent.label.isNotBlank()) {
                                viewModel.sendChatMessage(intent.label)
                            }
                        }
                    )
                }

                if (uiState.isChatLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = SpendoraTheme.colors.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Analyzing financial telemetry & retrieved knowledge...",
                                fontSize = 12.sp,
                                color = SpendoraTheme.colors.textMuted
                            )
                        }
                    }
                }
            }

            // Quick Starter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.suggestedPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SpendoraTheme.colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpendoraTheme.colors.border),
                        modifier = Modifier.clickable {
                            viewModel.sendChatMessage(prompt)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SpendoraTheme.colors.primaryLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SpendoraTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            // Message Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SpendoraTheme.colors.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Ask Spendora AI anything...",
                                color = SpendoraTheme.colors.textMuted,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp, max = 110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SpendoraTheme.colors.surfaceElevated,
                            unfocusedContainerColor = SpendoraTheme.colors.surfaceElevated,
                            focusedBorderColor = SpendoraTheme.colors.primary,
                            unfocusedBorderColor = SpendoraTheme.colors.border,
                            focusedTextColor = SpendoraTheme.colors.textPrimary,
                            unfocusedTextColor = SpendoraTheme.colors.textPrimary
                        ),
                        shape = RoundedCornerShape(23.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText
                                inputText = ""
                                viewModel.sendChatMessage(text)
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SpendoraTheme.colors.primary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onActionClick: (FinancialActionIntent) -> Unit = {}
) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(SpendoraTheme.colors.primary.copy(alpha = 0.15f))
                    .border(1.dp, SpendoraTheme.colors.primary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SpendoraTheme.colors.primaryLight,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.90f)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) SpendoraTheme.colors.primary else SpendoraTheme.colors.surface,
                border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, SpendoraTheme.colors.border) else null
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        fontSize = 13.5.sp,
                        color = if (isUser) Color(0xFF03151E) else SpendoraTheme.colors.textPrimary,
                        lineHeight = 20.sp,
                        fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                    )

                    // Render Action Intent Button if present
                    if (!isUser && message.actionIntent != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SpendoraTheme.colors.primary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SpendoraTheme.colors.primary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onActionClick(message.actionIntent) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = message.actionIntent.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SpendoraTheme.colors.primaryLight
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = SpendoraTheme.colors.primaryLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
