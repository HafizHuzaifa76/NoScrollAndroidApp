package com.noscroll.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noscroll.app.model.MonitoredApp
import com.noscroll.app.ui.theme.Forest
import com.noscroll.app.ui.theme.ForestDark
import com.noscroll.app.ui.theme.Ink
import com.noscroll.app.ui.theme.Sand
import com.noscroll.app.ui.theme.SandDeep

data class HomeUiState(
    val accessibilityEnabled: Boolean = false,
    val protectionEnabled: Boolean = true,
    val blockCount: Int = 0,
    val apps: List<AppToggleState> = emptyList(),
)

data class AppToggleState(
    val app: MonitoredApp,
    val enabled: Boolean,
)

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenAccessibilitySettings: () -> Unit,
    onProtectionToggled: (Boolean) -> Unit,
    onAppToggled: (String, Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor by animateColorAsState(
        targetValue = when {
            !state.accessibilityEnabled -> Color(0xFFB45309)
            state.protectionEnabled -> Forest
            else -> Color(0xFF64748B)
        },
        label = "statusColor",
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Sand, SandDeep, Color(0xFFE8E0D4)),
                )
            ),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                Text(
                    text = "NO SCROLL",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Ink,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Reels and Shorts close the moment they open.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink.copy(alpha = 0.72f),
                )
            }
        }

        item {
            StatusCard(
                accessibilityEnabled = state.accessibilityEnabled,
                protectionEnabled = state.protectionEnabled,
                blockCount = state.blockCount,
                statusColor = statusColor,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                onRefresh = onRefresh,
            )
        }

        item {
            ProtectionCard(
                enabled = state.protectionEnabled,
                accessibilityEnabled = state.accessibilityEnabled,
                onToggled = onProtectionToggled,
            )
        }

        item {
            Text(
                text = "Apps to block",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        items(state.apps, key = { it.app.id }) { item ->
            AppToggleRow(
                item = item,
                enabled = state.accessibilityEnabled && state.protectionEnabled,
                onToggled = { onAppToggled(item.app.id, it) },
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusCard(
    accessibilityEnabled: Boolean,
    protectionEnabled: Boolean,
    blockCount: Int,
    statusColor: Color,
    onOpenAccessibilitySettings: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when {
                        !accessibilityEnabled -> "Accessibility required"
                        protectionEnabled -> "Protection active"
                        else -> "Protection paused"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    !accessibilityEnabled ->
                        "Enable the No Scroll accessibility service so the app can watch for Reels and Shorts in the background."
                    protectionEnabled ->
                        "Watching in the background. Opening a Reel or Short will close it instantly."
                    else ->
                        "Service is on, but protection is paused. Turn it back on below."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Ink.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "$blockCount closed so far",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = ForestDark,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!accessibilityEnabled) {
                Button(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Forest,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("Open accessibility settings")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("I've enabled it — refresh")
                }
            } else {
                OutlinedButton(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Manage accessibility permission")
                }
            }
        }
    }
}

@Composable
private fun ProtectionCard(
    enabled: Boolean,
    accessibilityEnabled: Boolean,
    onToggled: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Block Reels & Shorts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Instant close when short video opens",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.65f),
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggled,
                enabled = accessibilityEnabled,
            )
        }
    }
}

@Composable
private fun AppToggleRow(
    item: AppToggleState,
    enabled: Boolean,
    onToggled: (Boolean) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.88f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.app.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.app.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink.copy(alpha = 0.6f),
                )
            }
            Switch(
                checked = item.enabled,
                onCheckedChange = onToggled,
                enabled = enabled,
            )
        }
    }
}
