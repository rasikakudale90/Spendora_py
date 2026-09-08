package com.spendora.app.ui.components.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendora.app.data.model.LeakAnalysisResponse
import com.spendora.app.ui.components.formatInr
import com.spendora.app.ui.theme.*
import com.spendora.app.ui.viewmodel.AiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeakHunterSheet(
    aiViewModel: AiViewModel,
    onDismiss: () -> Unit
) {
    val uiState by aiViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Subscriptions, 1 = Micro-leaks

    LaunchedEffect(Unit) {
        if (uiState.leakAnalysis == null) {
            aiViewModel.loadLeakAnalysis()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = RoseDanger,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Leak Hunter & Subscriptions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
            Text(
                text = "Autonomous 90-day transaction pattern and recurring charge audit",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLeakLoading && uiState.leakAnalysis == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else {
                val analysis = uiState.leakAnalysis
                if (analysis != null) {
                    // Summary Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Total Monthly Drain", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = formatInr(analysis.totalMonthlyLeak),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = RoseDanger
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Annualized Projection", fontSize = 11.sp, color = TextMuted)
                                Text(
                                    text = formatInr(analysis.totalAnnualProjectedLeak),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = AmberWarning
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 0) PrimaryIndigo else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Subscriptions (${analysis.subscriptionCount})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) TextPrimary else TextMuted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 1) PrimaryIndigo else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Micro-Leaks (${analysis.microLeakCount})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) TextPrimary else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (selectedTab == 0) {
                            if (analysis.detectedSubscriptions.isNotEmpty()) {
                                items(analysis.detectedSubscriptions) { sub ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SurfaceDark,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(PrimaryIndigo.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Subscriptions,
                                                        contentDescription = null,
                                                        tint = PrimaryIndigoLight,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(text = sub.title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                                    Text(text = "Last billed: ${sub.lastDate}", fontSize = 11.sp, color = TextMuted)
                                                }
                                            }
                                            Text(
                                                text = "${formatInr(sub.estimatedMonthlyCost)}/mo",
                                                fontWeight = FontWeight.Bold,
                                                color = RoseDanger
                                            )
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No recurring subscription leaks detected! 🎉", color = EmeraldSuccess, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            if (analysis.microSpendingLeaks.isNotEmpty()) {
                                items(analysis.microSpendingLeaks) { leak ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SurfaceDark,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = leak.categoryOrLabel, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text(
                                                    text = "${formatInr(leak.monthlyTotal)}/mo",
                                                    fontWeight = FontWeight.Bold,
                                                    color = AmberWarning
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${leak.transactionCount} micro-transactions (avg ${formatInr(leak.averageAmount)})",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                            Text(
                                                text = "Annual impact: ${formatInr(leak.annualProjectedDrain)} / year",
                                                fontSize = 11.sp,
                                                color = RoseDanger,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            } else {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No micro-spending leaks detected! 🎉", color = EmeraldSuccess, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
