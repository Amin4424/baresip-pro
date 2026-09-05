package io.github.amin4424.baresip.pro

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.DateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import io.github.amin4424.baresip.pro.BaresipService.Companion.circleGreen
import io.github.amin4424.baresip.pro.BaresipService.Companion.colorblind
import io.github.amin4424.baresip.pro.CustomElements.AlertDialog
import io.github.amin4424.baresip.pro.CustomElements.verticalScrollbar

fun NavGraphBuilder.callsScreenRoute(navController: NavController, viewModel: ViewModel) {
    composable(
        route = "calls/{aor}",
        arguments = listOf(navArgument("aor") { type = NavType.StringType })
    ) { backStackEntry ->
        val aor = backStackEntry.arguments?.getString("aor")!!
        CallsScreen(navController, viewModel, aor)
    }
    composable(route = "calls") {
        val aor = viewModel.selectedAor.value.ifEmpty { BaresipService.uas.value.firstOrNull()?.account?.aor ?: "none" }
        CallsScreen(navController, viewModel, aor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallsScreen(navController: NavController, viewModel: ViewModel, aor: String) {

    val ua = UserAgent.ofAor(aor)

    val callHistory: MutableState<List<CallRow>> = remember(aor) {
        mutableStateOf(if (ua != null) loadCallHistory(aor) else emptyList())
    }
    var isHistoryLoaded by remember { mutableStateOf(true) }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val ctx = LocalContext.current

    LaunchedEffect(ua, refreshTrigger) {
        if (ua != null) {
            if (ua.account.isMobile) Utils.cancelMissedCallsNotification(ctx)
            callHistory.value = loadCallHistory(aor)
            Log.d(TAG, "CallsScreen: Loaded ${callHistory.value.size} call history entries for $aor")
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshTrigger++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = true) {
        if (ua != null) {
            val serviceIntent = Intent(ctx, BaresipService::class.java)
            serviceIntent.action = "Clear Missed"
            serviceIntent.putExtra("uap", ua.uap)
            ctx.startService(serviceIntent)
        }
        navController.navigateUp()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
            val topBarGradient = if (isDark) {
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            } else {
                Brush.verticalGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarGradient)
                    .statusBarsPadding()
            ) {
                if (ua != null)
                    TopAppBar(navController, ua, callHistory)
                else
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.call_history),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        ),
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    )
            }
        },
        content = { contentPadding ->
            if (isHistoryLoaded) {
                if (ua != null) {
                    CallsContent(ctx, navController, viewModel, contentPadding, ua, callHistory)
                } else {
                    CustomElements.NoAccountView(
                        title = stringResource(R.string.no_account_found),
                        message = "Configure a SIP account to start tracking incoming and outgoing calls.",
                        onAddAccount = { navController.navigate("accounts") },
                        modifier = Modifier.padding(contentPadding).padding(bottom = 80.dp)
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(navController: NavController, ua: UserAgent, callHistory: MutableState<List<CallRow>>) {

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    var expanded by remember { mutableStateOf(false) }

    val delete = stringResource(R.string.delete)
    val disable = stringResource(R.string.disable_history)
    val enable = stringResource(R.string.enable_history)
    val blocked = stringResource(R.string.blocked)

    val showDialog = remember { mutableStateOf(false) }
    val lastAction = remember { mutableStateOf({}) }

    val account = ua.account

    AlertDialog(
        showDialog = showDialog,
        title = stringResource(R.string.confirmation),
        message = String.format(stringResource(R.string.delete_history_alert), account.text()),
        firstButtonText = stringResource(R.string.cancel),
        secondButtonText = "",
        thirdButtonText = "",
        fourthButtonText = "",
        lastButtonText = stringResource(R.string.delete),
        onLastClicked = lastAction.value,
    )

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.call_history),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.22f)
                ) {
                    Text(
                        text = "${callHistory.value.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = Color.White,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        windowInsets = WindowInsets(0, 0, 0, 0),
        navigationIcon = {
            val ctx = LocalContext.current
            IconButton(
                onClick = {
                    val serviceIntent = Intent(ctx, BaresipService::class.java)
                    serviceIntent.action = "Clear Missed"
                    serviceIntent.putExtra("uap", ua.uap)
                    ctx.startService(serviceIntent)
                    navController.navigateUp()
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More options", tint = Color.White)
            }
            val callMenuItems = if (account.callHistory) {
                listOf(
                    MenuItem(disable, Icons.Outlined.PauseCircle),
                    MenuItem(delete, Icons.Outlined.Delete),
                    MenuItem(blocked, Icons.Outlined.Block)
                )
            } else {
                listOf(
                    MenuItem(enable, Icons.Outlined.PlayCircle)
                )
            }
            CustomElements.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                menuItems = callMenuItems,
                onItemClick = { selectedItem ->
                    expanded = false
                    when (selectedItem) {
                        delete -> {
                            lastAction.value = {
                                Log.d(TAG, "CallsScreen: Clearing call history for ${account.aor}")
                                CallHistoryNew.clear(account.aor)
                                callHistory.value = emptyList()
                                Blocked.clear(account.aor)
                            }
                            showDialog.value = true
                        }
                        disable, enable -> {
                            account.callHistory = !account.callHistory
                            Account.saveAccounts()
                        }
                        blocked ->
                            navController.navigate("blocked/invite/${account.aor}")
                    }
                }
            )
        }
    )
}

@Composable
private fun CallsContent(
    ctx: Context,
    navController: NavController,
    viewModel: ViewModel,
    contentPadding: PaddingValues,
    ua: UserAgent,
    callHistory: MutableState<List<CallRow>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = contentPadding.calculateTopPadding()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Account(ua.account)
        if (!ua.account.callHistory) {
            CustomElements.EmptyStateBanner(
                icon = Icons.AutoMirrored.Filled.CallMissed,
                title = stringResource(R.string.history_disabled),
                message = stringResource(R.string.history_disabled_help)
            )
        } else if (callHistory.value.isEmpty()) {
            CustomElements.EmptyStateBanner(
                icon = Icons.Filled.History,
                title = stringResource(R.string.no_call_history),
                message = stringResource(R.string.no_call_history_help)
            )
        } else {
            Calls(ctx, navController, viewModel, ua, callHistory, bottomPadding = contentPadding.calculateBottomPadding())
        }
    }
}

@Composable
private fun Account(account: Account) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF161E2E) else Color(0xFFEFF3F8),
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = account.text(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
            }
        }
    }
}

private fun getCallDateGroup(ctx: Context, time: GregorianCalendar): String {
    val now = Calendar.getInstance()
    val isToday = now.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)
    if (isToday) return ctx.getString(R.string.today)

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == time.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == time.get(Calendar.DAY_OF_YEAR)
    if (isYesterday) return "Yesterday"

    val month = time.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
    val day = time.get(Calendar.DAY_OF_MONTH)
    return if (time.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
        "$month $day"
    } else {
        "$month $day, ${time.get(Calendar.YEAR)}"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Calls(
    ctx: Context,
    navController: NavController,
    viewModel: ViewModel,
    ua: UserAgent,
    callHistory: MutableState<List<CallRow>>,
    bottomPadding: androidx.compose.ui.unit.Dp = 80.dp
) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    val showDialog = remember { mutableStateOf(false) }
    val message = remember { mutableStateOf("") }
    val secondButtonText = remember { mutableStateOf("") }
    val secondAction = remember { mutableStateOf({}) }
    val thirdButtonText = remember { mutableStateOf("") }
    val thirdAction = remember { mutableStateOf({}) }
    val fourthButtonText = remember { mutableStateOf("") }
    val fourthAction = remember { mutableStateOf({}) }
    val lastButtonText = remember { mutableStateOf("") }
    val lastAction = remember { mutableStateOf({}) }

    AlertDialog(
        showDialog = showDialog,
        title = stringResource(R.string.confirmation),
        message = message.value,
        firstButtonText = stringResource(R.string.cancel),
        secondButtonText = secondButtonText.value,
        onSecondClicked = secondAction.value,
        thirdButtonText = thirdButtonText.value,
        onThirdClicked = thirdAction.value,
        fourthButtonText = fourthButtonText.value,
        onFourthClicked = fourthAction.value,
        lastButtonText = lastButtonText.value,
        onLastClicked = lastAction.value,
    )

    val alertTitle = remember { mutableStateOf("") }
    val alertMessage = remember { mutableStateOf("") }
    val showAlert = remember { mutableStateOf(false) }
    val unknown = stringResource(R.string.unknown)

    AlertDialog(
        showDialog = showAlert,
        title = alertTitle.value,
        message = alertMessage.value,
        lastButtonText = stringResource(R.string.ok),
    )

    LaunchedEffect(callHistory.value.size, ua.account.aor) {
        Log.d(TAG, "CallsScreen: loaded ${callHistory.value.size} call history entries for ${ua.account.aor}")
    }

    val groupedHistory = remember(callHistory.value) {
        callHistory.value.groupBy { callRow ->
            getCallDateGroup(ctx, callRow.stopTime)
        }
    }

    val lazyListState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScrollbar(state = lazyListState),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = bottomPadding + 80.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        groupedHistory.forEach { (dateGroup, callsInGroup) ->
            stickyHeader(key = "header_$dateGroup") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isDark) Color(0xFF0A0F1D).copy(alpha = 0.78f)
                            else Color(0xFFFBFBFC).copy(alpha = 0.82f)
                        )
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isDark) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFFE2E8F0).copy(alpha = 0.85f),
                        border = BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = dateGroup,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            items(items = callsInGroup, key = { callRow -> "${callRow.stopTime.timeInMillis}_${callRow.peerUri}" }) { callRow ->
                val peerUri = callRow.peerUri
                var hasRecordings = false
                for (d in callRow.details) {
                    if (d.recording.isNotEmpty() && d.recording[0] != "") hasRecordings = true
                }

                val isMissed = callRow.details.any { d ->
                    d.direction == CALL_DOWN_RED || d.direction == CALL_MISSED_IN
                }
                val primaryDirection = callRow.details.firstOrNull()?.direction ?: callRow.direction
                val isUp = callUp(primaryDirection)

                val badgeColor = when {
                    isMissed -> Color(0xFFEF4444)
                    primaryDirection == CALL_DOWN_GREEN || primaryDirection == CALL_UP_GREEN -> Color(0xFF10B981)
                    primaryDirection == CALL_DOWN_BLUE -> Color(0xFF0284C7)
                    else -> Color(0xFFF59E0B)
                }

                val badgeIcon = when {
                    isMissed -> Icons.AutoMirrored.Filled.CallMissed
                    isUp -> Icons.AutoMirrored.Filled.CallMade
                    else -> Icons.AutoMirrored.Filled.CallReceived
                }

                val friendlyName = Utils.friendlyUri(ctx, peerUri, ua.account)

                val timeFormat = remember { DateFormat.getTimeInstance(DateFormat.SHORT) }
                val timeStr = remember(callRow.stopTime) { timeFormat.format(callRow.stopTime.time) }
                val dirLabel = when {
                    isMissed -> stringResource(R.string.call_missed)
                    isUp -> stringResource(R.string.outgoing_call)
                    else -> stringResource(R.string.incoming_call)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .animateItem(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF161D2A) else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    Log.d(TAG, "CallsScreen: call row clicked for peer ${callRow.peerUri}, dir=${callRow.direction}")
                                    if (!peerUri.contains("anonymous") && peerUri != unknown) {
                                        val intent = Intent(ctx, MainActivity::class.java)
                                        intent.putExtra("uap", ua.uap)
                                        intent.putExtra("peer", peerUri)
                                        val peerNameWithLabel =
                                            Utils.friendlyUri(ctx, peerUri, ua.account)
                                        val contact = Contact.findContact(peerUri)
                                        if (contact is Contact.BaresipContact && contact.email.isNotEmpty())
                                            message.value = String.format(
                                                ctx.getString(R.string.contact_email_action_question),
                                                peerNameWithLabel
                                            )
                                        else
                                            message.value = String.format(
                                                ctx.getString(R.string.contact_action_question),
                                                peerNameWithLabel
                                            )
                                        secondButtonText.value = ctx.getString(R.string.call)
                                        secondAction.value = {
                                            if (ua.account.isMobile && ua.status != circleGreen.getValue(colorblind)) {
                                                alertTitle.value = ctx.getString(R.string.notice)
                                                alertMessage.value = Utils.mobileStatusMessage(ctx, ua.status)
                                                showAlert.value = true
                                            }
                                            else {
                                                handleIntent(ctx, viewModel, intent, "call")
                                                navController.navigate("main") {
                                                    popUpTo("main")
                                                    launchSingleTop = true
                                                }
                                            }
                                        }
                                        thirdButtonText.value = ctx.getString(R.string.send_message)
                                        thirdAction.value = {
                                            if (ua.account.isMobile) {
                                                if (ua.status != circleGreen.getValue(colorblind)) {
                                                    alertTitle.value = ctx.getString(R.string.notice)
                                                    alertMessage.value = Utils.mobileStatusMessage(ctx, ua.status)
                                                    showAlert.value = true
                                                }
                                                else if (!Utils.isDefaultSmsApp(ctx)) {
                                                    alertTitle.value = ctx.getString(R.string.notice)
                                                    alertMessage.value = ctx.getString(R.string.enable_default_messaging)
                                                    showAlert.value = true
                                                }
                                                else {
                                                    handleIntent(ctx, viewModel, intent, "message")
                                                    navController.navigateUp()
                                                }
                                            }
                                            else {
                                                handleIntent(ctx, viewModel, intent, "message")
                                                navController.navigateUp()
                                            }
                                        }
                                        if (contact is Contact.BaresipContact && contact.email.isNotEmpty()) {
                                            lastButtonText.value = ctx.getString(R.string.send_email)
                                            lastAction.value = {
                                                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = "mailto:${contact.email}".toUri()
                                                }
                                                try {
                                                    ctx.startActivity(emailIntent)
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Failed to start email activity: ${e.message}")
                                                }
                                            }
                                        }
                                        else
                                            lastButtonText.value = ""
                                        showDialog.value = true
                                    }
                                },
                                onLongClick = {
                                    Log.d(TAG, "CallsScreen: Long clicked call history item for $peerUri")
                                    val peerName = Utils.friendlyUri(ctx, peerUri, ua.account, includeLabel = false)
                                    val peerNameWithLabel = Utils.friendlyUri(ctx, peerUri, ua.account)
                                    val callText: String = if (callRow.details.size > 1)
                                        ctx.getString(R.string.calls_calls)
                                    else
                                        ctx.getString(R.string.calls_call)
                                    val contactExists = Contact.nameExists(peerName, BaresipService.contacts, false)
                                    if (contactExists || peerUri.contains("anonymous") || peerUri == unknown) {
                                        message.value = String.format(
                                            ctx.getString(R.string.calls_delete_question),
                                            peerNameWithLabel,
                                            callText
                                        )
                                        secondButtonText.value = ""
                                        thirdButtonText.value = ""
                                        fourthButtonText.value = ""
                                        lastButtonText.value = ctx.getString(R.string.delete)
                                        lastAction.value = {
                                            Log.d(TAG, "CallsScreen: Removing call row for $peerUri")
                                            removeFromHistory(callHistory, callRow)
                                        }
                                    }
                                    else {
                                        message.value = String.format(
                                            ctx.getString(R.string.calls_add_delete_question),
                                            peerNameWithLabel,
                                            callText
                                        )
                                        secondButtonText.value = ctx.getString(R.string.add_contact)
                                        secondAction.value = {
                                            val uri = Utils.sipToTel(peerUri)
                                            navController.navigate("contact/$uri/new")
                                        }
                                        thirdButtonText.value = ""
                                        fourthButtonText.value = ctx.getString(R.string.block)
                                        fourthAction.value = {
                                            if (!BlockRule.exists(ua.account.aor, peerUri)) {
                                                BaresipService.blockRules.add(BlockRule(ua.account.aor, peerUri))
                                                BlockRule.save()
                                            }
                                        }
                                        lastButtonText.value = ctx.getString(R.string.delete)
                                        lastAction.value = {
                                            Log.d(TAG, "CallsScreen: Removing call row for $peerUri")
                                            removeFromHistory(callHistory, callRow)
                                        }
                                    }
                                    showDialog.value = true
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar (48dp) with call direction badge
                        Box(modifier = Modifier.size(48.dp)) {
                            when (val contact = Contact.findContact(peerUri)) {
                                is Contact.BaresipContact -> {
                                    val avatarImage = contact.avatarImage
                                    if (avatarImage != null)
                                        Image(
                                            bitmap = avatarImage.asImageBitmap(),
                                            contentDescription = "Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        )
                                    else
                                        ModernCallAvatar(contact.name, contact.color)
                                }
                                is Contact.AndroidContact -> {
                                    val thumbNailUri = contact.thumbnailUri
                                    if (thumbNailUri != null)
                                        AsyncImage(
                                            model = thumbNailUri,
                                            contentDescription = "Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                        )
                                    else
                                        ModernCallAvatar(contact.name, contact.color)
                                }
                                null -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                                CircleShape
                                            )
                                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AccountCircle,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.size(32.dp),
                                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }

                            // Call direction badge
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(badgeColor, CircleShape)
                                    .border(1.5.dp, if (isDark) Color(0xFF161D2A) else Color.White, CircleShape)
                                    .align(Alignment.BottomEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = badgeIcon,
                                    contentDescription = "Direction",
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Contact Name & Subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = friendlyName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isMissed) (if (isDark) Color(0xFFFF6B6B) else Color(0xFFDC2626)) else (if (isDark) Color.White else Color(0xFF111827)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (callRow.details.size > 1) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier.padding(start = 6.dp)
                                    ) {
                                        Text(
                                            text = "(${callRow.details.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = timeStr,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = " • $dirLabel",
                                    fontSize = 12.sp,
                                    color = if (isMissed) (if (isDark) Color(0xFFFF8080) else Color(0xFFEF4444)) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                                if (hasRecordings) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "Recorded",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Quick Call back button
                            if (!peerUri.contains("anonymous") && peerUri != unknown) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = Color(0xFF10B981).copy(alpha = if (isDark) 0.18f else 0.12f)
                                        )
                                        .border(
                                            BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = if (isDark) 0.35f else 0.25f)),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            Log.d(TAG, "CallsScreen: Quick Call Back to $peerUri")
                                            val intent = Intent(ctx, MainActivity::class.java)
                                            intent.putExtra("uap", ua.uap)
                                            intent.putExtra("peer", peerUri)
                                            if (ua.account.isMobile && ua.status != circleGreen.getValue(colorblind)) {
                                                alertTitle.value = ctx.getString(R.string.notice)
                                                alertMessage.value = Utils.mobileStatusMessage(ctx, ua.status)
                                                showAlert.value = true
                                            } else {
                                                handleIntent(ctx, viewModel, intent, "call")
                                                navController.navigate("main") {
                                                    popUpTo("main")
                                                    launchSingleTop = true
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Call,
                                        contentDescription = "Call",
                                        tint = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            // Details button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        Log.d(TAG, "CallsScreen: Viewing call details for $peerUri")
                                        viewModel.selectCallRow(callRow)
                                        navController.navigate("call_details")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernCallAvatar(name: String, color: Int) {
    val baseColor = Color(color)
    val gradient = Brush.linearGradient(
        colors = listOf(
            baseColor,
            baseColor.copy(
                red = (baseColor.red * 0.72f).coerceIn(0f, 1f),
                green = (baseColor.green * 0.72f).coerceIn(0f, 1f),
                blue = (baseColor.blue * 0.72f).coerceIn(0f, 1f)
            )
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(gradient)
            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val initial = if (name.isNotEmpty()) name.first().uppercaseChar().toString() else ""
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

private fun loadCallHistory(aor: String): MutableList<CallRow> {
    val res = mutableListOf<CallRow>()
    for (i in BaresipService.callHistory.indices.reversed()) {
        val h = BaresipService.callHistory[i]
        if (h.aor == aor) {
            val direction: Int = if (h.direction == "in") {
                if (h.startTime != null) {
                    if (h.startTime != h.stopTime) CALL_DOWN_GREEN else CALL_DOWN_BLUE
                }
                else
                    if (h.rejected) CALL_DOWN_RED else CALL_MISSED_IN
            }
            else {
                if (h.startTime != null)
                    CALL_UP_GREEN
                else
                    if (h.rejected) CALL_UP_RED else CALL_MISSED_OUT
            }
            if (res.isNotEmpty() && res.last().peerUri == h.peerUri)
                res.last().details.add(
                    CallRow.Details(direction, h.startTime, h.stopTime, h.recording.toList())
                )
            else
                res.add(CallRow(h.aor, h.peerUri, direction, h.startTime, h.stopTime, h.recording.toList()))
        }
    }
    return res
}

private fun removeFromHistory(callHistory: MutableState<List<CallRow>>, callRow: CallRow) {
    for (details in callRow.details) {
        CallHistoryNew.deleteRecordingFiles(details.recording.toTypedArray())
        BaresipService.callHistory.removeAll {
            it.startTime == details.startTime && it.stopTime == details.stopTime
        }
    }
    CallHistoryNew.deleteRecordingFiles(callRow.recording.toTypedArray())
    val updatedList = callHistory.value.filterNot { it == callRow }
    callHistory.value = updatedList
    CallHistoryNew.save()
}

fun callUp(direction: Int): Boolean {
    return when (direction) {
        CALL_UP_GREEN, CALL_UP_RED, CALL_MISSED_OUT -> true
        else -> false
    }
}

fun callTint(direction: Int): Int {
    return when (direction) {
        CALL_UP_GREEN, CALL_DOWN_GREEN -> R.color.colorTrafficGreen
        CALL_UP_RED, CALL_DOWN_RED -> R.color.colorTrafficRed
        CALL_DOWN_BLUE -> R.color.colorPrimary
        else -> R.color.colorTrafficYellow
    }
}

