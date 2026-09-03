package com.tutpro.baresip.plus

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(ctx: Context, viewModel: ViewModel, navController: NavController) {
    val aor by viewModel.selectedAor.collectAsState()
    val accountUpdate by viewModel.accountUpdate.collectAsState()
    val isDialpadVisible by viewModel.isDialpadVisible.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    val showVmIcon = remember(aor, accountUpdate) {
        if (aor.isNotEmpty()) Account.ofAor(aor)?.vmUri?.isNotEmpty() ?: false else false
    }
    val hasNewVoicemail = remember(aor, accountUpdate) {
        if (aor.isNotEmpty()) (Account.ofAor(aor)?.vmNew ?: 0) > 0 else false
    }
    val isMobile = remember(aor, accountUpdate) {
        if (aor.isNotEmpty()) Account.ofAor(aor)?.isMobile ?: false else false
    }
    val hasUnreadMessages = remember(aor, accountUpdate) {
        if (aor.isNotEmpty()) Account.ofAor(aor)?.unreadMessages ?: false else false
    }
    val hasMissedCalls = remember(aor, accountUpdate) {
        if (aor.isNotEmpty()) Account.ofAor(aor)?.missedCalls ?: false else false
    }

    // Glassmorphism translucent styling compatible with Android 8.0+
    val glassBg = if (isDark) {
        Color(0xFF0D1424).copy(alpha = 0.78f)
    } else {
        Color(0xFFFFFFFF).copy(alpha = 0.82f)
    }

    val glassBorder = Brush.verticalGradient(
        colors = if (isDark) listOf(
            Color.White.copy(alpha = 0.28f),
            Color.White.copy(alpha = 0.06f)
        ) else listOf(
            Color.White.copy(alpha = 0.95f),
            Color.White.copy(alpha = 0.45f)
        )
    )

    val shadowSpotColor = if (isDark) {
        Color(0xFF00B0FF).copy(alpha = 0.20f)
    } else {
        Color(0xFF0F172A).copy(alpha = 0.12f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = if (showVmIcon) 16.dp else 28.dp, end = if (showVmIcon) 16.dp else 28.dp, bottom = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = shadowSpotColor,
                ambientColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(glassBg)
            .border(width = 1.dp, brush = glassBorder, shape = RoundedCornerShape(32.dp))
            .height(62.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voicemail (if configured on active account)
            if (showVmIcon) {
                BottomNavItem(
                    icon = Icons.Filled.Voicemail,
                    contentDescription = "Voicemail",
                    isActive = false,
                    hasBadge = hasNewVoicemail,
                    onClick = {
                        val ua = UserAgent.ofAor(aor) ?: return@BottomNavItem
                        val acc = ua.account
                        if (acc.vmUri.isNotEmpty()) {
                            val intent = Intent(ctx, MainActivity::class.java).apply {
                                putExtra("uap", ua.uap)
                                putExtra("peer", acc.vmUri)
                            }
                            handleIntent(ctx, viewModel, intent, "call")
                        }
                    }
                )
            }

            // Dialer / Keypad
            BottomNavItem(
                icon = Icons.Filled.Dialpad,
                contentDescription = "Dialpad",
                isActive = (currentRoute == "main" || currentRoute == null) && isDialpadVisible,
                onClick = {
                    if (currentRoute != "main") {
                        navController.navigate("main") {
                            popUpTo("main") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    if (!isDialpadVisible) {
                        viewModel.toggleDialpadVisibility()
                    }
                }
            )

            // Contacts
            BottomNavItem(
                icon = Icons.Filled.Person,
                contentDescription = "Contacts",
                isActive = currentRoute == "contacts",
                onClick = {
                    if (currentRoute != "contacts") {
                        navController.navigate("contacts") {
                            popUpTo("main") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )

            // Call History
            BottomNavItem(
                icon = Icons.Filled.History,
                contentDescription = "History",
                isActive = currentRoute?.startsWith("calls") == true,
                hasBadge = hasMissedCalls,
                onClick = {
                    val targetAor = aor.ifEmpty { BaresipService.uas.value.firstOrNull()?.account?.aor ?: "" }
                    if (targetAor.isNotEmpty() && currentRoute?.startsWith("calls") != true) {
                        navController.navigate("calls/$targetAor") {
                            popUpTo("main") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )

            // Messages / Chats
            BottomNavItem(
                icon = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Messages",
                isActive = currentRoute?.startsWith("chats") == true,
                hasBadge = hasUnreadMessages,
                onClick = {
                    if (isMobile && !Utils.isDefaultSmsApp(ctx)) {
                        Toast.makeText(ctx, R.string.enable_default_messaging, Toast.LENGTH_LONG).show()
                        return@BottomNavItem
                    }
                    val targetAor = aor.ifEmpty { BaresipService.uas.value.firstOrNull()?.account?.aor ?: "" }
                    if (targetAor.isNotEmpty() && currentRoute?.startsWith("chats") != true) {
                        navController.navigate("chats/$targetAor") {
                            popUpTo("main") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    hasBadge: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    Box(contentAlignment = Alignment.TopEnd) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(8.dp, CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                    .background(
                        if (isDark) Color(0xFF1E293B).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f),
                        shape = CircleShape
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ),
                        CircleShape
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(46.dp),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (hasBadge) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, end = 2.dp)
                    .size(10.dp)
                    .shadow(2.dp, CircleShape)
                    .background(Color(0xFFFF3B30), CircleShape)
                    .border(1.5.dp, if (isDark) Color(0xFF0F172A) else Color.White, CircleShape)
            )
        }
    }
}

