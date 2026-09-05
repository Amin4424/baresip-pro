package io.github.amin4424.baresip.promax

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.amin4424.baresip.promax.CustomElements.AlertDialog
import io.github.amin4424.baresip.promax.CustomElements.verticalScrollbar
import java.util.GregorianCalendar

fun NavGraphBuilder.blockedScreenRoute(navController: NavController) {
    composable(
        route = "blocked/{request}/{aor}",
        arguments = listOf(
            navArgument("aor") { type = NavType.StringType },
            navArgument("request") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val aor = backStackEntry.arguments?.getString("aor")!!
        val request = backStackEntry.arguments?.getString("request")!!
        BlockedScreen(navController, request, aor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlockedScreen(navController: NavController, request: String, aor: String) {

    val account = Account.ofAor(aor)!!

    val blocked: MutableState<List<Blocked>> = remember { mutableStateOf(emptyList()) }
    var isBlockedLoaded by remember { mutableStateOf(false) }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(aor, refreshTrigger) {
        blocked.value = loadBlocked(request, aor)
        isBlockedLoaded = true
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshTrigger++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BackHandler(enabled = true) { navController.navigateUp() }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(navController, account, request, blocked)
        },
        content = { contentPadding ->
            if (isBlockedLoaded)
                BlockedContent(LocalContext.current, navController, contentPadding, account, blocked)
        },
    )
}

@Composable
private fun TopAppBar(
    navController: NavController,
    account: Account,
    request: String,
    blocked: MutableState<List<Blocked>>
) {
    var expanded by remember { mutableStateOf(false) }
    val delete = stringResource(R.string.delete)
    val showDialog = remember { mutableStateOf(false) }
    val lastAction = remember { mutableStateOf({}) }

    AlertDialog(
        showDialog = showDialog,
        title = stringResource(R.string.confirmation),
        message = String.format(stringResource(R.string.blocked_delete_alert), account.text()),
        firstButtonText = stringResource(R.string.cancel),
        lastButtonText = stringResource(R.string.delete),
        onLastClicked = lastAction.value,
    )

    CustomElements.ModernTopAppBar(
        title = if (request == "invite")
            stringResource(R.string.blocked_calls)
        else
            stringResource(R.string.blocked_messages),
        badge = blocked.value.size,
        onBack = { navController.navigateUp() },
        actions = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More options", tint = Color.White)
            }
            val blockedMenuItems = listOf(
                MenuItem(delete, Icons.Outlined.Delete)
            )
            CustomElements.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                menuItems = blockedMenuItems,
                onItemClick = { selectedItem ->
                    expanded = false
                    when (selectedItem) {
                        delete -> {
                            lastAction.value = {
                                Blocked.clear(account.aor)
                                blocked.value = emptyList()
                            }
                            showDialog.value = true
                        }
                    }
                }
            )
        }
    )
}

@Composable
private fun BlockedContent(
    ctx: Context,
    navController: NavController,
    contentPadding: PaddingValues,
    account: Account,
    blocked: MutableState<List<Blocked>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Account(account)
        Blocked(ctx, navController, account, blocked)
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
            color = if (isDark) Color(0xFF131C2E) else Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFEF4444), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = account.text(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Blocked(
    ctx: Context,
    navController: NavController,
    acc: Account,
    blocked: MutableState<List<Blocked>>
) {
    val showDialog = remember { mutableStateOf(false) }
    val message = remember { mutableStateOf("") }
    val lastButtonText = remember { mutableStateOf("") }
    val lastAction = remember { mutableStateOf({}) }
    val unknown = stringResource(R.string.unknown)

    AlertDialog(
        showDialog = showDialog,
        title = stringResource(R.string.confirmation),
        message = message.value,
        firstButtonText = stringResource(R.string.cancel),
        lastButtonText = lastButtonText.value,
        onLastClicked = lastAction.value,
    )

    if (blocked.value.isEmpty()) {
        CustomElements.EmptyStateBanner(
            icon = Icons.Filled.Block,
            title = "No Blocked Entries",
            message = "Blocked calls and messages for this account will appear here."
        )
    } else {
        val lazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScrollbar(state = lazyListState),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items = blocked.value, key = { blocked -> blocked.timeStamp }) { blockedItem ->
                val peerUri = blockedItem.peerUri
                val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
                val calendar = GregorianCalendar()
                calendar.timeInMillis = blockedItem.timeStamp
                val timeStr = Utils.relativeTime(ctx, calendar)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            enabled = !peerUri.contains("anonymous") && peerUri != unknown,
                            onClick = {
                                message.value = String.format(
                                    ctx.getString(R.string.blocked_contact_question), peerUri
                                )
                                lastButtonText.value = ctx.getString(R.string.add_contact)
                                lastAction.value = { navController.navigate("contact/$peerUri/new") }
                                showDialog.value = true
                            }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF131C2E) else Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = Utils.friendlyUri(ctx, peerUri, acc),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = timeStr,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!peerUri.contains("anonymous") && peerUri != unknown) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = "Add Contact",
                                    tint = MaterialTheme.colorScheme.primary,
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

private fun loadBlocked(request: String, aor: String): MutableList<Blocked> {
    val res = mutableListOf<Blocked>()
    for (i in BaresipService.blocked.indices.reversed()) {
        val b = BaresipService.blocked[i]
        if (b.aor == aor && b.request == request)
            res.add(Blocked("", b.peerUri, "", b.timeStamp))
    }
    Log.d(TAG, "Loaded ${res.size} blocked $request requests")
    return res
}
