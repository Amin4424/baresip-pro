package io.github.amin4424.baresip.promax

import android.content.Intent
import android.text.format.DateUtils.isToday
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.amin4424.baresip.promax.BaresipService.Companion.circleGreen
import io.github.amin4424.baresip.promax.BaresipService.Companion.colorblind
import io.github.amin4424.baresip.promax.CustomElements.AlertDialog
import io.github.amin4424.baresip.promax.CustomElements.verticalScrollbar
import kotlinx.coroutines.launch
import java.lang.String.format
import java.text.DateFormat
import java.util.GregorianCalendar

fun NavGraphBuilder.chatScreenRoute(navController: NavController, viewModel: ViewModel) {
    composable(
        route = "chat/{aor}/{peer}",
        arguments = listOf(
            navArgument("aor") { type = NavType.StringType },
            navArgument("peer") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val aor = backStackEntry.arguments?.getString("aor")!!
        val peerUri = backStackEntry.arguments?.getString("peer")!!
        ChatScreen(
            navController = navController,
            viewModel = viewModel,
            account = Account.ofAor(aor)!!,
            peerUri = peerUri
        )
    }
}

@Composable
private fun ChatScreen(
    navController: NavController,
    viewModel: ViewModel,
    account: Account,
    peerUri: String
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val ctx = LocalContext.current

    val aor = account.aor

    var chatMessages by remember(aor, peerUri) { mutableStateOf<List<Message>>(emptyList()) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(TAG, "Resumed to ChatScreen for AOR: $aor peer $peerUri")
                chatMessages = loadPeerMessages(aor, peerUri)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var areMessagesLoaded by remember(aor, peerUri) { mutableStateOf(false) }

    val reloadMessages = {
        Log.d(TAG, "Reloading messages for $aor peer $peerUri")
        chatMessages = loadPeerMessages(aor, peerUri)
        if (!areMessagesLoaded)
            areMessagesLoaded = true
    }

    val addMessage = { newMessage: Message ->
        chatMessages = listOf(newMessage) + chatMessages
    }

    DisposableEffect(key1 = lifecycleOwner, key2 = account.aor, key3 = peerUri) {
        val messagesObserver = Observer<Long> { timestamp ->
            Log.d(TAG, "Message update received via LiveData for $peerUri, timestamp: $timestamp")
            reloadMessages()
        }
        reloadMessages() // Initial load
        Log.d(TAG, "Observing message updates for $peerUri")
        BaresipService.messageUpdate.observe(lifecycleOwner, messagesObserver)
        onDispose {
            Log.d(TAG, "Removing message observer for $peerUri")
            BaresipService.messageUpdate.removeObserver(messagesObserver)
        }
    }

    BackHandler(enabled = true) {
        val serviceIntent = Intent(ctx, BaresipService::class.java)
        serviceIntent.action = "Clear Unread"
        serviceIntent.putExtra("uap", account.accp)
        serviceIntent.putExtra("peer", peerUri)
        ctx.startService(serviceIntent)
        backAction(navController, account, peerUri)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(navController, viewModel, account, peerUri)
        },
        bottomBar = {
            NewMessage(
                viewModel,
                account = account,
                peerUri = peerUri,
                addMessage = addMessage
            )
        },
        content = { contentPadding ->
            if (areMessagesLoaded)
                ChatContent(
                    contentPadding,
                    account, peerUri,
                    chatMessages,
                    reloadMessages
                )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    navController: NavController,
    viewModel: ViewModel,
    account: Account,
    peerUri: String
) {
    val ctx = LocalContext.current
    val aor = account.aor
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    val peerName = Utils.friendlyUri(
        uri = peerUri,
        account = account,
        anonymous = stringResource(R.string.anonymous),
        unknown = stringResource(R.string.unknown)
    )
    val contact = Contact.findContact(peerUri)

    CustomElements.ModernTopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    val serviceIntent = Intent(ctx, BaresipService::class.java)
                    serviceIntent.action = "Clear Unread"
                    serviceIntent.putExtra("uap", account.accp)
                    serviceIntent.putExtra("peer", peerUri)
                    ctx.startService(serviceIntent)
                    backAction(navController, account, peerUri)
                }
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Peer Avatar
                Box(modifier = Modifier.size(40.dp)) {
                    when (contact) {
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
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = peerName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = peerUri.substringAfter(":"),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            if (!account.isMobile)
                IconButton(
                    onClick = {
                        val ua = UserAgent.ofAor(account.aor)
                        if (ua != null) {
                            Log.d(TAG, "ChatScreen: Starting video call with $peerUri")
                            val intent = Intent(ctx, MainActivity::class.java)
                            intent.putExtra("uap", ua.uap)
                            intent.putExtra("peer", peerUri)
                            handleIntent(ctx, viewModel, intent, "video call")
                            navController.navigate("main") {
                                popUpTo("main")
                                launchSingleTop = true
                            }
                        }
                        else
                            Log.w(TAG, "Video Call button onClick listener did not find UA for $aor")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Videocam,
                        modifier = Modifier.size(26.dp),
                        contentDescription = "Video Call",
                        tint = Color.White
                    )
                }
            IconButton(
                onClick = {
                    val ua = UserAgent.ofAor(account.aor)
                    if (ua != null) {
                        Log.d(TAG, "ChatScreen: Starting voice call with $peerUri")
                        val callIntent = Intent(ctx, MainActivity::class.java)
                            .putExtra("uap", ua.uap)
                            .putExtra("peer", peerUri)
                        handleIntent(ctx, viewModel, callIntent, "call")
                        navController.navigate("main") {
                            popUpTo("main")
                            launchSingleTop = true
                        }
                    }
                    else
                        Log.w(TAG, "Call button onClick listener did not find UA for $aor")
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Call",
                    tint = Color.White
                )
            }
            if (contact != null && contact is Contact.BaresipContact && contact.email.isNotEmpty())
                IconButton(
                    onClick = {
                        val ua = UserAgent.ofAor(account.aor)
                        if (ua != null) {
                            if (contact.email.isNotEmpty()) {
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
                            Log.w(TAG, "Email button onClick listener did not find UA for $aor")
                    }
                ) {
                    Icon(imageVector = Icons.Outlined.Email, modifier = Modifier.size(24.dp), contentDescription = "Email", tint = Color.White)
                }
        }
    )
}

@Composable
private fun ChatContent(
    contentPadding: PaddingValues,
    account: Account,
    peerUri: String,
    messages: List<Message>,
    onMessageDeleted: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(contentPadding),
        verticalArrangement = Arrangement.Bottom
    ) {
        Account(account)
        Spacer(modifier = Modifier.weight(1f))
        Messages(account, peerUri, messages, onMessageDeleted)
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
            color = if (isDark) Color(0xFF161C26) else Color(0xFFEFF3F8),
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = account.text(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
            }
        }
    }
}

@Composable
private fun Messages(
    account: Account,
    peerUri: String,
    messages: List<Message>,
    onMessageDeleted: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    val peerName = Utils.friendlyUri(
        uri = peerUri,
        account = account,
        anonymous = stringResource(R.string.anonymous),
        unknown = stringResource(R.string.unknown)
    )

    val shortMessageQuestion = stringResource(R.string.short_message_question)
    val deleteString = stringResource(R.string.delete)

    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }
    val secondButtonText = remember { mutableStateOf("") }
    val secondAction = remember { mutableStateOf({}) }
    val lastButtonText = remember { mutableStateOf("") }
    val lastAction = remember { mutableStateOf({}) }

    AlertDialog(
        showDialog = showDialog,
        title = stringResource(R.string.confirmation),
        message = dialogMessage.value,
        firstButtonText = stringResource(R.string.cancel),
        secondButtonText = secondButtonText.value,
        onSecondClicked = secondAction.value,
        lastButtonText = lastButtonText.value,
        onLastClicked = lastAction.value,
    )

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages) {
        if (messages.isNotEmpty())
            coroutineScope.launch { lazyListState.scrollToItem(0) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .verticalScrollbar(state = lazyListState),
        reverseLayout = true,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = messages, key = { message -> message.timeStamp }) { message ->
            val isIncoming = message.direction == MESSAGE_DOWN
            val cal = GregorianCalendar()
            cal.timeInMillis = message.timeStamp
            val fmt: DateFormat = if (isToday(message.timeStamp))
                DateFormat.getTimeInstance(DateFormat.SHORT)
            else
                DateFormat.getDateInstance(DateFormat.SHORT)
            var info = fmt.format(cal.time)
            if (info.length < 6) info = "${stringResource(R.string.today)} $info"
            if (message.direction == MESSAGE_UP_FAIL) {
                info = if (message.responseCode != 0)
                    "$info - ${stringResource(R.string.message_failed)}: ${message.responseCode} ${message.responseReason}"
                else
                    "$info - ${stringResource(R.string.sending_failed)}"
            }

            val bubbleShape = if (isIncoming)
                RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp)
            else
                RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomEnd = 18.dp, bottomStart = 18.dp)

            val bubbleColor = if (isIncoming) {
                if (isDark) Color(0xFF131C2E) else Color(0xFFEFF3F8)
            } else {
                if (isDark) Color(0xFF0284C7) else MaterialTheme.colorScheme.primary
            }

            val textColor = if (isIncoming) {
                if (isDark) Color.White else Color(0xFF0F172A)
            } else {
                Color.White
            }

            val metaColor = if (isIncoming) {
                if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)
            } else {
                Color.White.copy(alpha = 0.75f)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isIncoming) Arrangement.Start else Arrangement.End
            ) {
                Surface(
                    shape = bubbleShape,
                    color = bubbleColor,
                    border = if (isIncoming) BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)) else null,
                    modifier = Modifier
                        .widthIn(min = 70.dp, max = 310.dp)
                        .clickable {
                            dialogMessage.value = shortMessageQuestion
                            secondButtonText.value = ""
                            lastButtonText.value = deleteString
                            lastAction.value = {
                                Log.d(TAG, "ChatScreen: Deleting message at ${message.timeStamp}")
                                message.delete()
                                onMessageDeleted()
                            }
                            showDialog.value = true
                        }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        SelectionContainer {
                            Text(
                                text = message.message,
                                color = textColor,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                fontWeight = if (isIncoming && message.new) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.align(Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = info,
                                fontSize = 11.sp,
                                color = metaColor
                            )
                            if (!isIncoming) {
                                Spacer(modifier = Modifier.width(4.dp))
                                when (message.direction) {
                                    MESSAGE_UP_FAIL -> {
                                        Icon(
                                            imageVector = Icons.Filled.ErrorOutline,
                                            contentDescription = "Failed",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    MESSAGE_UP_WAIT -> {
                                        Icon(
                                            imageVector = Icons.Filled.HourglassEmpty,
                                            contentDescription = "Sending",
                                            tint = metaColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Filled.DoneAll,
                                            contentDescription = "Sent",
                                            tint = metaColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
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
            fontSize = 17.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NewMessage(
    viewModel: ViewModel,
    account: Account,
    peerUri: String,
    addMessage: (message: Message) -> Unit
) {
    val ctx = LocalContext.current
    val aor = account.aor
    val ua = UserAgent.ofAor(aor)!!
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    val newMessage = rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(viewModel.getAorPeerMessage(aor, peerUri)))
    }

    var textFieldLoaded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val showDialog = remember { mutableStateOf(false) }
    val dialogMessage = remember { mutableStateOf("") }

    val messageFailed = stringResource(R.string.message_failed)
    val noTelephonyProvider = stringResource(R.string.no_telephony_provider)

    AlertDialog(
        showDialog = showDialog,
        title = stringResource(R.string.notice),
        message = dialogMessage.value,
        lastButtonText = stringResource(R.string.ok),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
        border = BorderStroke(
            0.5.dp,
            if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
        ),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            OutlinedTextField(
                value = newMessage.value,
                placeholder = {
                    Text(
                        stringResource(R.string.new_message) + "...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                onValueChange = {
                    newMessage.value = it
                    viewModel.updateAorPeerMessage(aor, peerUri, it.text)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .verticalScroll(rememberScrollState())
                    .focusRequester(focusRequester)
                    .onGloballyPositioned {
                        if (!textFieldLoaded)
                            textFieldLoaded = true
                    },
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    focusedContainerColor = if (isDark) Color(0xFF161C26) else Color(0xFFF1F4F8),
                    unfocusedContainerColor = if (isDark) Color(0xFF141822) else Color(0xFFF6F8FA)
                ),
                singleLine = false,
                maxLines = 4,
                trailingIcon = {
                    if (newMessage.value.text.isNotEmpty()) {
                        IconButton(onClick = {
                            newMessage.value = TextFieldValue("")
                            viewModel.updateAorPeerMessage(aor, peerUri, "")
                        }) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    autoCorrectEnabled = true
                )
            )
            LaunchedEffect(Unit) {
                if (newMessage.value.text.isNotEmpty())
                    focusRequester.requestFocus()
            }
            IconButton(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (newMessage.value.text.isNotEmpty())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    ),
            onClick = {
                val msgText = newMessage.value.text
                if (msgText.isNotEmpty()) {
                    Log.d(TAG, "ChatScreen: Sending message to $peerUri (${msgText.length} chars)")
                    keyboardController?.hide()
                    val time = System.currentTimeMillis()
                    val msg = Message(
                        aor,
                        peerUri,
                        msgText,
                        time,
                        MESSAGE_UP_WAIT,
                        0,
                        "",
                        true
                    )
                    msg.add()
                    var msgUri = ""
                    addMessage(msg)
                    if (ua.account.isMobile) {
                        if (ua.status != circleGreen.getValue(colorblind)) {
                            dialogMessage.value = Utils.mobileStatusMessage(ctx, ua.status)
                            showDialog.value = true
                        }
                        else {
                            val destination = Utils.uriUserPart(peerUri)
                            if (Utils.sendSms(ctx, destination, msgText)) {
                                msg.direction = MESSAGE_UP
                                newMessage.value = TextFieldValue("")
                                viewModel.updateAorPeerMessage(aor, peerUri, "")
                                keyboardController?.hide()
                            }
                            else {
                                Toast.makeText(
                                    ctx, "$messageFailed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                msg.direction = MESSAGE_UP_FAIL
                                msg.responseReason = messageFailed
                            }
                        }
                    }
                    else {
                        if (Utils.isTelUri(peerUri)) {
                            if (ua.account.telProvider == "") {
                                dialogMessage.value = String.format(
                                    noTelephonyProvider,
                                    Utils.plainAor(aor)
                                )
                                showDialog.value = true
                            }
                            else
                                msgUri = Utils.telToSip(peerUri, ua.account)
                        }
                        else
                            msgUri = peerUri
                        if (msgUri != "") {
                            if (Api.message_send(ua.uap, msgUri, msgText, time.toString()) != 0) {
                                Toast.makeText(
                                    ctx, "$messageFailed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                msg.direction = MESSAGE_UP_FAIL
                                msg.responseReason = messageFailed
                            }
                            else {
                                newMessage.value = TextFieldValue("")
                                viewModel.updateAorPeerMessage(aor, peerUri, "")
                                keyboardController?.hide()
                            }
                        }
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = stringResource(R.string.add)
            )
        }
    }
}
}

private fun backAction(navController: NavController, account: Account, peerUri: String) {
    val aor = account.aor
    Message.updateMessagesFromPearRead(aor, peerUri)
    account.unreadMessages = Message.unreadMessages(aor)
    navController.navigateUp()
}

private fun loadPeerMessages(aor: String, peerUri: String): List<Message> {
    val res = mutableListOf<Message>()
    for (m in BaresipService.messages.reversed())
        if ((m.aor == aor) && (m.peerUri == peerUri)) res.add(m)
    return res
}
