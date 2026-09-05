package io.github.amin4424.baresip.pro

import android.content.Intent
import android.text.format.DateUtils.isToday
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import io.github.amin4424.baresip.pro.CustomElements.AlertDialog
import io.github.amin4424.baresip.pro.CustomElements.DropdownMenu
import io.github.amin4424.baresip.pro.CustomElements.SelectableAlertDialog
import io.github.amin4424.baresip.pro.CustomElements.verticalScrollbar
import java.text.DateFormat
import java.util.GregorianCalendar

fun NavGraphBuilder.chatsScreenRoute(navController: NavController, viewModel: ViewModel) {
    composable(
        route = "chats/{aor}",
        arguments = listOf(navArgument("aor") { type = NavType.StringType })
    ) { backStackEntry ->
        val aor = backStackEntry.arguments?.getString("aor")!!
        ChatsScreen(navController, viewModel, aor)
    }
    composable(route = "chats") {
        val aor = viewModel.selectedAor.value.ifEmpty { BaresipService.uas.value.firstOrNull()?.account?.aor ?: "none" }
        ChatsScreen(navController, viewModel, aor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsScreen(navController: NavController, viewModel: ViewModel, aor: String) {

    val ctx = LocalContext.current
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    val account = Account.ofAor(aor)
    val uaMessages: MutableState<List<Message>> = remember(aor) {
        mutableStateOf(if (account != null) loadMessages(account) else emptyList())
    }
    var areMessagesLoaded by remember { mutableStateOf(true) }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(aor, refreshTrigger) {
        if (account != null) {
            val serviceIntent = Intent(ctx, BaresipService::class.java)
            serviceIntent.action = "Clear Unread"
            serviceIntent.putExtra("uap", account.accp)
            ctx.startService(serviceIntent)
            uaMessages.value = loadMessages(account)
            Log.d(TAG, "ChatsScreen: Loaded ${uaMessages.value.size} conversations for ${account.aor}")
        }
    }

    DisposableEffect(lifecycleOwner, aor) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME)
                refreshTrigger++
        }
        val messageLiveObserver = Observer<Long> {
            if (account != null) {
                Log.d(TAG, "ChatsScreen: messageUpdate LiveData triggered, reloading messages for $aor")
                uaMessages.value = loadMessages(account)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        BaresipService.messageUpdate.observe(lifecycleOwner, messageLiveObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            BaresipService.messageUpdate.removeObserver(messageLiveObserver)
        }
    }

    BackHandler(enabled = true) {
        if (account != null) {
            val serviceIntent = Intent(ctx, BaresipService::class.java)
            serviceIntent.action = "Clear Unread"
            serviceIntent.putExtra("uap", account.accp)
            ctx.startService(serviceIntent)
        }
        navController.navigateUp()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (account != null)
                TopAppBar(navController, account, uaMessages)
            else
                CustomElements.ModernTopAppBar(
                    title = stringResource(R.string.chats),
                    onBack = { navController.navigateUp() }
                )
        },
        content = { contentPadding ->
            if (areMessagesLoaded) {
                if (account != null) {
                    ChatsContent(navController, contentPadding, account, uaMessages)
                } else {
                    CustomElements.NoAccountView(
                        title = stringResource(R.string.no_account_found),
                        message = "Configure a SIP account to start sending and receiving messages.",
                        onAddAccount = { navController.navigate("accounts") },
                        modifier = Modifier.padding(contentPadding).padding(bottom = 80.dp)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    navController: NavController,
    account: Account,
    uaMessages: MutableState<List<Message>>
) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    var menuExpanded by remember { mutableStateOf(false) }
    val delete = stringResource(R.string.delete)
    val blocked = stringResource(R.string.blocked)
    val showDialog = remember { mutableStateOf(false) }
    val lastAction = remember { mutableStateOf({}) }

    AlertDialog(
        showDialog = showDialog,
        title = stringResource(R.string.confirmation),
        message = String.format(stringResource(R.string.delete_chats_alert), account.text()),
        firstButtonText = stringResource(R.string.cancel),
        lastButtonText = stringResource(R.string.delete),
        onLastClicked = lastAction.value,
    )

    CustomElements.ModernTopAppBar(
        title = stringResource(R.string.chats),
        badge = uaMessages.value.size,
        navigationIcon = {
            val ctx = LocalContext.current
            IconButton(
                onClick = {
                    val serviceIntent = Intent(ctx, BaresipService::class.java)
                    serviceIntent.action = "Clear Unread"
                    serviceIntent.putExtra("uap", account.accp)
                    ctx.startService(serviceIntent)
                    navController.navigateUp()
                }
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        actions = {
            IconButton(
                onClick = { menuExpanded = !menuExpanded }
            ) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More options", tint = Color.White)
            }
            val chatMenuItems = listOf(
                MenuItem(delete, Icons.Outlined.Delete),
                MenuItem(blocked, Icons.Outlined.Block)
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                menuItems = chatMenuItems,
                onItemClick = { selectedItem ->
                    menuExpanded = false
                    when (selectedItem) {
                        delete -> {
                            lastAction.value = {
                                Log.d(TAG, "ChatsScreen: Deleting all messages for ${account.aor}")
                                deleteMessages(uaMessages, account, "")
                                account.unreadMessages = false
                            }
                            showDialog.value = true
                        }
                        blocked ->
                            navController.navigate("blocked/message/${account.aor}")
                    }
                }
            )
        }
    )
}

@Composable
private fun ChatsContent(
    navController: NavController,
    contentPadding: PaddingValues,
    account: Account,
    uaMessages: MutableState<List<Message>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = contentPadding.calculateTopPadding()),
        verticalArrangement = Arrangement.Top
    ) {
        Account(account)
        NewChatPeer(navController, account)
        if (uaMessages.value.isEmpty()) {
            CustomElements.EmptyStateBanner(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = stringResource(R.string.no_chats),
                message = stringResource(R.string.no_chats_help),
                actionLabel = "Start a Chat",
                onActionClick = { navController.navigate("chat/${account.aor}/new") }
            )
        } else {
            Chats(navController, account, uaMessages, bottomPadding = contentPadding.calculateBottomPadding())
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
                        .background(Color(0xFF10B981), CircleShape)
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
private fun Chats(
    navController: NavController,
    account: Account,
    uaMessages: MutableState<List<Message>>,
    bottomPadding: androidx.compose.ui.unit.Dp = 80.dp
) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    val aor = account.aor

    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val secondButtonText = remember { mutableStateOf("") }
    val secondAction = remember { mutableStateOf({}) }
    val thirdButtonText = remember { mutableStateOf("") }
    val thirdAction = remember { mutableStateOf({}) }
    val lastButtonText = remember { mutableStateOf("") }
    val lastAction = remember { mutableStateOf({}) }

    val shortChatQuestion = stringResource(R.string.short_chat_question)
    val longChatQuestion = stringResource(R.string.long_chat_question)
    val addContactText = stringResource(R.string.add_contact)
    val blockText = stringResource(R.string.block)
    val deleteText = stringResource(R.string.delete)
    val anonymousText = stringResource(R.string.anonymous)
    val unknownText = stringResource(R.string.unknown)

    if (showDialog.value)
        AlertDialog(
            showDialog = showDialog,
            title = stringResource(R.string.confirmation),
            message = dialogMessage.value,
            firstButtonText = stringResource(R.string.cancel),
            secondButtonText = secondButtonText.value,
            onSecondClicked = secondAction.value,
            thirdButtonText = thirdButtonText.value,
            onThirdClicked = thirdAction.value,
            lastButtonText = lastButtonText.value,
            onLastClicked = lastAction.value,
        )

    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScrollbar(state = lazyListState),
        reverseLayout = true,
        state = lazyListState,
        contentPadding = PaddingValues(bottom = bottomPadding + 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = uaMessages.value, key = { message -> message.timeStamp }) { message ->
            val isUnread = account.unreadMessages && Message.unreadMessagesFromPeer(aor, message.peerUri)
            val peerName = Utils.friendlyUri(message.peerUri, account, anonymous = anonymousText, unknown = unknownText)

            val cal = GregorianCalendar()
            cal.timeInMillis = message.timeStamp
            val fmt: DateFormat = if (isToday(message.timeStamp))
                DateFormat.getTimeInstance(DateFormat.SHORT)
            else
                DateFormat.getDateInstance(DateFormat.SHORT)
            val info = fmt.format(cal.time)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF151B26) else Color.White
                ),
                border = BorderStroke(
                    width = if (isUnread) 1.5.dp else 1.dp,
                    color = if (isUnread) MaterialTheme.colorScheme.primary else (if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                Log.d(TAG, "ChatsScreen: Opening conversation with peer ${message.peerUri}")
                                navController.navigate("chat/${aor}/${message.peerUri}")
                            },
                            onLongClick = {
                                Log.d(TAG, "ChatsScreen: Long-pressed conversation with peer ${message.peerUri}")
                                val plainPeerName = Utils.friendlyUri(message.peerUri, account, includeLabel = false, anonymous = anonymousText, unknown = unknownText)
                                val peerNameWithLabel = Utils.friendlyUri(message.peerUri, account, anonymous = anonymousText, unknown = unknownText)
                                val contactExists = Contact.nameExists(plainPeerName, BaresipService.contacts, false)
                                if (contactExists) {
                                    dialogMessage.value = String.format(shortChatQuestion, peerNameWithLabel)
                                    secondButtonText.value = ""
                                    lastButtonText.value = deleteText
                                    lastAction.value = {
                                        deleteMessages(uaMessages, account, message.peerUri)
                                    }
                                } else {
                                    dialogMessage.value = String.format(longChatQuestion, plainPeerName)
                                    secondButtonText.value = addContactText
                                    secondAction.value = { navController.navigate("contact/${message.peerUri}/new") }
                                    thirdButtonText.value = blockText
                                    thirdAction.value = {
                                        if (!BlockRule.exists(account.aor, message.peerUri)) {
                                            BaresipService.blockRules.add(BlockRule(account.aor, message.peerUri))
                                            BlockRule.save()
                                        }
                                    }
                                    lastButtonText.value = deleteText
                                    lastAction.value = { deleteMessages(uaMessages, account, message.peerUri) }
                                }
                                showDialog.value = true
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar (48dp)
                    Box(modifier = Modifier.size(48.dp)) {
                        when (val contact = Contact.findContact(message.peerUri)) {
                            is Contact.BaresipContact -> {
                                val avatarImage = contact.avatarImage
                                if (avatarImage != null)
                                    Image(
                                        bitmap = avatarImage.asImageBitmap(),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                else
                                    ModernChatAvatar(contact.name, contact.color)
                            }
                            is Contact.AndroidContact -> {
                                val thumbNailUri = contact.thumbnailUri
                                if (thumbNailUri != null)
                                    AsyncImage(
                                        model = thumbNailUri,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                else
                                    ModernChatAvatar(contact.name, contact.color)
                            }
                            null -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        // Unread Dot
                        if (isUnread) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Message & Contact Info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = peerName,
                                fontSize = 16.sp,
                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF111827),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)
                            ) {
                                Text(
                                    text = info,
                                    fontSize = 11.sp,
                                    color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.message,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp,
                                fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isUnread) (if (isDark) Color.White else Color.Black) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernChatAvatar(name: String, color: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(Color(color)),
        contentAlignment = Alignment.Center
    ) {
        val initial = if (name.isNotEmpty()) name.first().uppercaseChar().toString() else ""
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp
        )
    }
}

@Composable
private fun NewChatPeer(navController: NavController, account: Account) {

    val alertTitle = remember { mutableStateOf("") }
    val alertMessage = remember { mutableStateOf("") }
    val showAlert = remember { mutableStateOf(false) }

    val noticeText = stringResource(R.string.notice)
    val noTelephonyProviderText = stringResource(R.string.no_telephony_provider)
    val invalidSipOrTelUriText = stringResource(R.string.invalid_sip_or_tel_uri)
    val contactNoTelUriText = stringResource(R.string.contact_no_tel_uri)
    val contactNoSipOrTelUriText = stringResource(R.string.contact_no_sip_or_tel_uri)

    fun makeChat(navController: NavController, account: Account, chatPeer: String) {
        val peerUri = if (Utils.isTelNumber(chatPeer))
            "tel:$chatPeer"
        else
            chatPeer
        val uri = if (Utils.isTelUri(peerUri)) {
            if (account.isMobile)
                peerUri
            else
                if (account.telProvider == "") {
                    alertTitle.value = noticeText
                    alertMessage.value =
                        String.format(noTelephonyProviderText, account.aor)
                    showAlert.value = true
                    ""
                }
                else
                    Utils.telToSip(peerUri, account)
        }
        else
            Utils.uriComplete(peerUri, account.aor)
        if (alertMessage.value.isEmpty()) {
            if (!Utils.checkUri(uri)) {
                alertTitle.value = noticeText
                alertMessage.value = String.format(invalidSipOrTelUriText, uri)
                showAlert.value = true
            }
            else
                navController.navigate("chat/${account.aor}/${uri}")
        }
    }

    if (showAlert.value) {
        AlertDialog(
            showDialog = showAlert,
            title = alertTitle.value,
            message = alertMessage.value,
            lastButtonText = stringResource(R.string.ok),
        )
    }

    val showDialog = remember { mutableStateOf(false) }
    val items = remember { mutableStateOf(listOf<String>()) }
    val itemAction = remember { mutableStateOf<(Int) -> Unit>({ _ -> run {} }) }

    SelectableAlertDialog(
        openDialog = showDialog,
        title = stringResource(R.string.choose_destination_uri),
        items = items.value,
        onItemClicked = itemAction.value,
        neutralButtonText = stringResource(R.string.cancel),
        onNeutralClicked = {}
    )

    var filteredSuggestions by remember { mutableStateOf<List<Triple<Contact, AnnotatedString, Contact.ContactUri?>>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var newPeer by remember { mutableStateOf("") }
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.weight(1f)) {
            if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .animateContentSize()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScrollbar(state = lazyListState, width = 6.dp),
                            horizontalAlignment = Alignment.Start,
                            state = lazyListState
                        ) {
                            items(
                                items = filteredSuggestions,
                                key = { (contact, _, matchingUri) -> "${contact.id()}:${matchingUri?.uri ?: ""}" }
                            ) { (contact, annotatedName, matchingUri) ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val uri = matchingUri?.uri ?: contact.uris().firstOrNull()?.uri ?: contact.name()
                                            newPeer = Utils.friendlyUri(uri, account, unique = true)
                                            showSuggestions = false
                                        }
                                        .padding(12.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = annotatedName,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 18.sp
                                        )
                                        if (matchingUri != null) {
                                            val uriPart = matchingUri.uri.substringAfter(":")
                                            val annotatedUri = if (matchingUri.uri.startsWith("sip:")) {
                                                val userPart = Utils.uriUserPart(matchingUri.uri)
                                                val restPart = uriPart.substring(userPart.length)
                                                buildAnnotatedString {
                                                    append(Utils.buildAnnotatedStringWithHighlight(userPart, newPeer))
                                                    append(restPart)
                                                }
                                            }
                                            else {
                                                val highlightPart = newPeer.filter { c -> c.isDigit() || c == '+' }
                                                Utils.buildAnnotatedStringWithHighlight(uriPart, highlightPart)
                                            }
                                            Text(
                                                text = buildAnnotatedString {
                                                    if (matchingUri.label.isNotEmpty() &&
                                                        !listOf("SIP", "TEL").contains(matchingUri.label.uppercase()))
                                                        append("${matchingUri.label} ")
                                                    append(annotatedUri)
                                                },
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
            OutlinedTextField(
                value = newPeer,
                placeholder = {
                    Text(
                        stringResource(R.string.new_chat_peer) + "...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                onValueChange = { input ->
                    newPeer = input
                    showSuggestions = newPeer.length > 1
                    filteredSuggestions = if (input.length <= 1)
                        emptyList()
                    else {
                        val normalizedInput = Utils.unaccent(input)
                        val numericInput = input.filter { c -> c.isDigit() || c == '+' }
                        val currentAor = account.aor
                        val e164Input = if (numericInput.isNotEmpty())
                            Utils.e164Uri("tel:$numericInput", account.countryCode).substring(4)
                        else
                            ""
                        BaresipService.contacts.flatMap { contact ->
                            val nameMatch = Utils.unaccent(contact.name()).contains(normalizedInput, ignoreCase = true)
                            val uris = contact.uris().filter { !Utils.uriMatch(it.uri, currentAor) }
                            val matchingUris = uris.filter { u ->
                                (u.uri.startsWith("tel:") && numericInput.isNotEmpty() &&
                                        (u.uri.substring(4).contains(numericInput) ||
                                                (e164Input != "" && u.uri.substring(4).contains(e164Input)))) ||
                                        (u.uri.startsWith("sip:") &&
                                                Utils.uriUserPart(u.uri).contains(normalizedInput, ignoreCase = true))
                            }
                            if (nameMatch) {
                                val annotatedName = Utils.buildAnnotatedStringWithHighlight(contact.name(), input)
                                if (uris.isEmpty())
                                    listOf(Triple(contact, annotatedName, null))
                                else
                                    uris.map { Triple(contact, annotatedName, it) }
                            }
                            else if (matchingUris.isNotEmpty())
                                matchingUris.map { Triple(contact, AnnotatedString(contact.name()), it) }
                            else
                                emptyList()
                        }
                    }
                },
                modifier = Modifier.padding(end = 6.dp).fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    focusedContainerColor = if (isDark) Color(0xFF161C26) else Color(0xFFF1F4F8),
                    unfocusedContainerColor = if (isDark) Color(0xFF141822) else Color(0xFFF6F8FA)
                ),
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (newPeer.isNotEmpty())
                        IconButton(onClick = {
                            if (showSuggestions)
                                showSuggestions = false
                            newPeer = ""
                        }) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                },
                textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            modifier = Modifier
                .size(48.dp)
                .shadow(4.dp, CircleShape)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            onClick = {
                showSuggestions = false
                val peerText = newPeer.trim()
                if (peerText.isNotEmpty()) {
                    Log.d(TAG, "ChatsScreen: New chat requested with peer '$peerText'")
                    val uris = Contact.contactUrisOfNameOrNumber(peerText, account)
                    if (uris.isEmpty()) {
                        if (Contact.nameExists(peerText, BaresipService.contacts, true)) {
                            alertTitle.value = noticeText
                            alertMessage.value = if (account.isMobile)
                                String.format(contactNoTelUriText, peerText)
                            else
                                String.format(contactNoSipOrTelUriText, peerText)
                            showAlert.value = true
                        }
                        else
                            makeChat(navController, account, peerText)
                    }
                    else if (uris.size == 1)
                        makeChat(navController, account, uris[0].uri)
                    else {
                        items.value = uris.map { it.label.ifEmpty { it.uri.substringAfter(":") } }
                        itemAction.value = { index ->
                            makeChat(navController, account, uris[index].uri)
                        }
                        showDialog.value = true
                    }
                }
                newPeer = ""
                focusManager.clearFocus()
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                modifier = Modifier.size(24.dp),
                tint = Color.White,
                contentDescription = stringResource(R.string.add)
            )
        }
    }
}

private fun loadMessages(account: Account) : List<Message> {
    val res = mutableListOf<Message>()
    account.unreadMessages = false
    for (m in BaresipService.messages.reversed()) {
        if (m.aor != account.aor) continue
        var found = false
        for (r in res)
            if (r.peerUri == m.peerUri) {
                found = true
                break
            }
        if (!found) {
            res.add(0, m)
            if (m.new)
                account.unreadMessages = true
        }
    }
    return res.toList()
}

private fun deleteMessages(uaMessages: MutableState<List<Message>>, account: Account, peerUri: String) {
    val updatedMessages = BaresipService.messages.toMutableList()
    updatedMessages.removeAll { it.aor == account.aor && (peerUri == "" || it.peerUri == peerUri) }
    BaresipService.messages = updatedMessages.toList()
    Message.save()
    uaMessages.value = loadMessages(account)
}