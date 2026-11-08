package com.noscroll.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noscroll.app.ui.theme.Amber
import com.noscroll.app.ui.theme.Emerald
import com.noscroll.app.ui.theme.EmeraldBright
import com.noscroll.app.ui.theme.EmeraldDeep
import com.noscroll.app.ui.theme.Ink
import com.noscroll.app.ui.theme.Slate

@Composable
fun HeroStatusCard(
    accessibilityEnabled: Boolean,
    protectionEnabled: Boolean,
    blockCount: Int,
    activeLabel: String,
    idleLabel: String,
    pausedLabel: String,
    activeDescription: String,
    idleDescription: String,
    pausedDescription: String,
    onOpenAccessibilitySettings: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColor by animateColorAsState(
        targetValue = when {
            !accessibilityEnabled -> Amber
            protectionEnabled -> EmeraldBright
            else -> Slate
        },
        label = "heroStatus",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(EmeraldDeep, Emerald, Color(0xFF0F766E)),
                    )
                )
                .padding(22.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = when {
                                !accessibilityEnabled -> Icons.Rounded.Lock
                                protectionEnabled -> Icons.Rounded.Shield
                                else -> Icons.Rounded.WarningAmber
                            },
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "NO SCROLL",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.72f),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = when {
                                !accessibilityEnabled -> "Setup needed"
                                protectionEnabled -> activeLabel
                                else -> pausedLabel
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                        )
                    }
                    StatusPill(
                        text = if (accessibilityEnabled) "ON" else "OFF",
                        color = statusColor,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when {
                        !accessibilityEnabled -> idleDescription
                        protectionEnabled -> activeDescription
                        else -> pausedDescription
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.86f),
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatChip(
                        icon = Icons.Rounded.Bolt,
                        label = "Blocked",
                        value = blockCount.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatChip(
                        icon = if (accessibilityEnabled) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                        label = "Service",
                        value = if (accessibilityEnabled) "Ready" else "Needed",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!accessibilityEnabled) {
                    Button(
                        onClick = onOpenAccessibilitySettings,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = EmeraldDeep,
                        ),
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enable accessibility")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRefresh,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.35f),
                        ),
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I've enabled it — refresh")
                    }
                } else {
                    OutlinedButton(
                        onClick = onOpenAccessibilitySettings,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.35f),
                        ),
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage accessibility")
                    }
                }
            }
        }
    }
}

@Composable
fun MasterToggleCard(
    title: String,
    subtitle: String,
    enabled: Boolean,
    interactionEnabled: Boolean,
    onToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Emerald.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = Emerald,
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggled,
                enabled = interactionEnabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Emerald,
                    checkedThumbColor = Color.White,
                ),
            )
        }
    }
}

@Composable
fun AppToggleCard(
    title: String,
    subtitle: String,
    accent: Color,
    checked: Boolean,
    enabled: Boolean,
    onToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(18.dp),
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (accent == Color(0xFFFFFC00)) Ink else accent,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ink,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onToggled,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Emerald,
                    checkedThumbColor = Color.White,
                ),
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Slate,
        )
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
            )
        }
    }
}
