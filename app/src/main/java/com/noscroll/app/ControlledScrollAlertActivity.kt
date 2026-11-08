package com.noscroll.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noscroll.app.ui.theme.Emerald
import com.noscroll.app.ui.theme.EmeraldDeep
import com.noscroll.app.ui.theme.NoScrollAppTheme
import java.util.concurrent.TimeUnit

class ControlledScrollAlertActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_WARN
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "this app"
        val blockedUntil = intent.getLongExtra(EXTRA_BLOCKED_UNTIL, 0L)
        val remainingMs = (blockedUntil - System.currentTimeMillis()).coerceAtLeast(0L)

        setContent {
            NoScrollAppTheme {
                AlertScreen(
                    mode = mode,
                    appName = appName,
                    remainingMs = remainingMs,
                    onDismiss = { finish() },
                )
            }
        }
    }

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_BLOCKED_UNTIL = "blocked_until"
        const val MODE_WARN = "warn"
        const val MODE_BLOCK = "block"
    }
}

@Composable
private fun AlertScreen(
    mode: String,
    appName: String,
    remainingMs: Long,
    onDismiss: () -> Unit,
) {
    val isBlock = mode == ControlledScrollAlertActivity.MODE_BLOCK
    val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(remainingMs).coerceAtLeast(1)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(EmeraldDeep, Emerald, Color(0xFF0F766E)),
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AlertIcon(
                icon = when {
                    isBlock -> Icons.Rounded.HourglassTop
                    else -> Icons.Rounded.WarningAmber
                }
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = if (isBlock) "Time's up" else "You're wasting time",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isBlock) {
                    "$appName is locked for about $minutesLeft more minutes. Take a break — come back after the cooldown."
                } else {
                    "You've been scrolling $appName for over 10 minutes. At 15 minutes it will lock for 40 minutes."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(22.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = EmeraldDeep,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (isBlock) "Got it" else "I'll slow down")
            }
        }
    }
}

@Composable
private fun AlertIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(36.dp),
        )
    }
}
