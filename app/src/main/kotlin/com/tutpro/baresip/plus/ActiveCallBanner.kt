package com.tutpro.baresip.plus

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.util.GregorianCalendar
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ActiveCallBanner(
    ctx: Context,
    call: Call,
    navController: NavController,
    viewModel: ViewModel,
    modifier: Modifier = Modifier
) {
    val status by call.status
    val isHold = call.onhold
    val isHeldByPeer = (call.showOnHoldNotice.value || call.held) && !call.onhold
    val isOnHold = isHold || isHeldByPeer || call.callOnHold.value
    val peerUri = call.peerUri
    val isVideo = call.videoCall || call.hasVideo()

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    val contact = remember(peerUri) {
        Contact.findContact(peerUri)
    }
    val displayName = when (contact) {
        is Contact.BaresipContact -> contact.name
        is Contact.AndroidContact -> contact.name
        null -> Utils.friendlyUri(ctx, peerUri, call.ua.account)
    }

    val greenColor = Color(0xFF2ABB86)
    val redColor = Color(0xFFEA4335)
    val yellowColor = Color(0xFFF9A825)
    val primaryCyan = Color(0xFF00B0FF)

    val statusText = when {
        isOnHold -> stringResource(R.string.on_hold)
        status == "ringing" -> stringResource(R.string.ringing)
        status == "outgoing" || status == "calling" -> stringResource(R.string.calling)
        status == "incoming" -> stringResource(R.string.incoming_call)
        status == "connected" || status == "answered" -> stringResource(R.string.connected)
        else -> status
    }

    val statusColor = when {
        isOnHold -> yellowColor
        status == "ringing" -> greenColor
        status == "outgoing" || status == "calling" -> primaryCyan
        status == "incoming" -> greenColor
        status == "connected" || status == "answered" -> greenColor
        else -> MaterialTheme.colorScheme.primary
    }

    val containerBg = if (isDark) Color(0xFF131B2E) else Color(0xFFFFFFFF)
    val borderStroke = BorderStroke(1.dp, if (isDark) statusColor.copy(alpha = 0.35f) else statusColor.copy(alpha = 0.5f))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .shadow(4.dp, RoundedCornerShape(22.dp))
            .clickable {
                viewModel.setFocusedCall(call)
                if (call.ua.account.aor != viewModel.selectedAor.value) {
                    viewModel.updateSelectedAor(call.ua.account.aor)
                }
                navController.navigate("call") {
                    launchSingleTop = true
                }
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon Surface
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = if (isDark) 0.18f else 0.12f),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            isOnHold -> Icons.Outlined.PauseCircle
                            isVideo -> Icons.Filled.Videocam
                            else -> Icons.Filled.Call
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                    if ((status == "connected" || status == "answered") && call.startTime != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        CallDurationBannerTimer(call.startTime!!)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick End Call Button
            IconButton(
                onClick = {
                    call.terminated.value = true
                    call.hangup(0, "")
                },
                modifier = Modifier
                    .size(38.dp)
                    .background(redColor.copy(alpha = if (isDark) 0.18f else 0.12f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = stringResource(R.string.hangup),
                    tint = redColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CallDurationBannerTimer(startTime: GregorianCalendar) {
    var durationText by remember { mutableStateOf("") }
    LaunchedEffect(startTime) {
        while (true) {
            val elapsedMillis = System.currentTimeMillis() - startTime.timeInMillis
            val seconds = if (elapsedMillis > 0) elapsedMillis / 1000 else 0
            durationText = android.text.format.DateUtils.formatElapsedTime(seconds)
            delay(1000.milliseconds)
        }
    }
    Text(
        text = "• $durationText",
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
