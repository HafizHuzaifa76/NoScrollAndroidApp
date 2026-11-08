package com.noscroll.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noscroll.app.ui.screens.ReelsBlockerScreen
import com.noscroll.app.ui.screens.SocialAppsBlockerScreen
import com.noscroll.app.ui.theme.Canvas
import com.noscroll.app.ui.theme.Emerald
import com.noscroll.app.ui.theme.EmeraldDeep
import com.noscroll.app.ui.theme.Ink
import com.noscroll.app.ui.theme.Slate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoScrollApp(
    state: AppUiState,
    onOpenAccessibilitySettings: () -> Unit,
    onReelsProtectionToggled: (Boolean) -> Unit,
    onSocialProtectionToggled: (Boolean) -> Unit,
    onReelAppToggled: (String, Boolean) -> Unit,
    onSocialAppToggled: (String, Boolean) -> Unit,
    onRefresh: () -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(NoScrollDestination.REELS) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Canvas,
        topBar = {
            NoScrollTopBar(
                destination = destination,
                accessibilityEnabled = state.accessibilityEnabled,
            )
        },
        bottomBar = {
            NoScrollBottomBar(
                destination = destination,
                onDestinationSelected = { destination = it },
            )
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = destination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal) {
                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it / 4 } + fadeOut())
                } else {
                    (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                        (slideOutHorizontally { it / 4 } + fadeOut())
                }
            },
            label = "destination",
        ) { current ->
            when (current) {
                NoScrollDestination.REELS -> ReelsBlockerScreen(
                    state = state,
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    onProtectionToggled = onReelsProtectionToggled,
                    onAppToggled = onReelAppToggled,
                    onRefresh = onRefresh,
                )

                NoScrollDestination.SOCIAL -> SocialAppsBlockerScreen(
                    state = state,
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings,
                    onProtectionToggled = onSocialProtectionToggled,
                    onAppToggled = onSocialAppToggled,
                    onRefresh = onRefresh,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoScrollTopBar(
    destination: NoScrollDestination,
    accessibilityEnabled: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Canvas),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(EmeraldDeep, Emerald, Color(0xFF2DD4BF)),
                    )
                ),
        )
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Canvas,
                titleContentColor = Ink,
            ),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(EmeraldDeep, Emerald),
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = destination.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = destination.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate,
                        )
                    }
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (accessibilityEnabled) Emerald.copy(alpha = 0.12f)
                            else Color(0xFFFEF3C7)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = if (accessibilityEnabled) "Protected" else "Setup",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (accessibilityEnabled) EmeraldDeep else Color(0xFFB45309),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
        )
    }
}

@Composable
private fun NoScrollBottomBar(
    destination: NoScrollDestination,
    onDestinationSelected: (NoScrollDestination) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = destination == NoScrollDestination.REELS,
            onClick = { onDestinationSelected(NoScrollDestination.REELS) },
            icon = {
                Icon(Icons.Rounded.MovieFilter, contentDescription = NoScrollDestination.REELS.label)
            },
            label = { Text(NoScrollDestination.REELS.label) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Emerald,
                selectedTextColor = EmeraldDeep,
                indicatorColor = Emerald.copy(alpha = 0.14f),
                unselectedIconColor = Slate,
                unselectedTextColor = Slate,
            ),
        )
        NavigationBarItem(
            selected = destination == NoScrollDestination.SOCIAL,
            onClick = { onDestinationSelected(NoScrollDestination.SOCIAL) },
            icon = {
                Icon(Icons.Rounded.Apps, contentDescription = NoScrollDestination.SOCIAL.label)
            },
            label = { Text(NoScrollDestination.SOCIAL.label) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Emerald,
                selectedTextColor = EmeraldDeep,
                indicatorColor = Emerald.copy(alpha = 0.14f),
                unselectedIconColor = Slate,
                unselectedTextColor = Slate,
            ),
        )
    }
}
