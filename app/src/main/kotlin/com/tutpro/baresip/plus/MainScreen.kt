package com.tutpro.baresip.plus

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.telecom.TelecomManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Call as CallIcon
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SpeakerPhone
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tutpro.baresip.plus.BaresipService.Companion.circleGreen
import com.tutpro.baresip.plus.BaresipService.Companion.colorblind
import com.tutpro.baresip.plus.BaresipService.Companion.contactNames
import com.tutpro.baresip.plus.BaresipService.Companion.uas
import com.tutpro.baresip.plus.BaresipService.Companion.uasStatus
import com.tutpro.baresip.plus.CustomElements.AlertDialog
import com.tutpro.baresip.plus.CustomElements.DropdownMenu
import com.tutpro.baresip.plus.CustomElements.PasswordDialog
import com.tutpro.baresip.plus.CustomElements.SelectableAlertDialog
import com.tutpro.baresip.plus.CustomElements.verticalScrollbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

private val dialpadButtonEnabled = mutableStateOf(true)
private var pullToRefreshEnabled = mutableStateOf(true)

private var downloadsInputUri: Uri? = null
private var downloadsOutputUri: Uri? = null

private val passwordTitle = mutableStateOf("")
private val showPasswordDialog = mutableStateOf(false)
private val showPasswordsDialog = mutableStateOf(false)

private var passwordAccounts = mutableListOf<String>()
private var password = mutableStateOf("")

private val selectItems = CustomElements.selectItems
private val selectItemAction = CustomElements.selectItemAction
private val showSelectItemDialog = CustomElements.showSelectItemDialog

private val showVideoLayout = mutableStateOf(false)

fun NavGraphBuilder.mainScreenRoute(
    navController: NavController,
    viewModel: ViewModel,
    onRequestPermissions: () -> Unit,
    onRestartApp: () -> Unit,
    onQuitApp: () -> Unit
) {
    composable("main") {
        MainScreen(
            navController = navController,
            viewModel = viewModel,
            onRequestPermissions = onRequestPermissions,
            onRestartClick = onRestartApp,
            onQuitClick = onQuitApp
        )
    }
}

@Composable
private fun MainScreen(
    navController: NavController,
    viewModel: ViewModel,
    onRequestPermissions: () -> Unit,
    onRestartClick: () -> Unit,
    onQuitClick: () -> Unit
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val selectedAor by viewModel.selectedAor.collectAsState()
    val ua = uas.value.find { it.account.aor == selectedAor }
    val call = ua?.currentCall()

    val showKeyboard by viewModel.showKeyboard.collectAsState()
    val hideKeyboard by viewModel.hideKeyboard.collectAsState()

    LaunchedEffect(showKeyboard) {
        if (showKeyboard > 0)
            keyboardController?.show()
    }

    LaunchedEffect(hideKeyboard) {
        if (hideKeyboard > 0)
            keyboardController?.hide()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "Resumed to MainScreen")
                    BaresipService.isMainVisible = true
                    viewModel.updateSpeakerPhoneStatus(BaresipService.speakerPhone)
                    viewModel.updateCalls(Call.calls().toList())
                    if (Call.inCall())
                        (ctx as? Activity)?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else
                        (ctx as? Activity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    (Call.call("incoming") ?: Call.calls().lastOrNull())?.let {
                        spinToAor(viewModel, it.ua.account.aor, it)
                    } ?: run {
                        if (uas.value.isNotEmpty() && viewModel.selectedAor.value == "")
                            spinToAor(viewModel, uas.value.first().account.aor)
                        else
                            viewModel.triggerAccountUpdate()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "Paused from MainScreen")
                    BaresipService.isMainVisible = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            Log.d(TAG, "onDispose for MainScreen")
            lifecycleOwner.lifecycle.removeObserver(observer)
            BaresipService.isMainVisible = false
        }
    }

    val encryptPasswordTitle = stringResource(R.string.encrypt_password)
    val backupRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.also { uri ->
                downloadsOutputUri = uri
                passwordTitle.value = encryptPasswordTitle
                showPasswordDialog.value = true
            }
        }
    }

    val noticeTitle = stringResource(R.string.notice)
    val noBackupMessage = stringResource(R.string.no_backup)
    fun launchBackupRequest() {
        if (VERSION.SDK_INT < 29) {
            if (!Utils.checkPermissions(ctx, arrayOf(WRITE_EXTERNAL_STORAGE))) {
                alertTitle.value = noticeTitle
                alertMessage.value = noBackupMessage
                showAlert.value = true
            }
            else {
                val path = Utils.downloadsPath("baresip.bs")
                downloadsOutputUri = File(path).toUri()
                passwordTitle.value = encryptPasswordTitle
                showPasswordDialog.value = true
            }
        }
        else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
                putExtra(
                    Intent.EXTRA_TITLE,
                    "baresip_" + SimpleDateFormat(
                        "yyyy_MM_dd_HH_mm_ss",
                        Locale.getDefault()
                    ).format(Date())
                )
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Downloads.EXTERNAL_CONTENT_URI)
            }
            backupRequestLauncher.launch(intent)
        }
    }

    val decryptPasswordTitle = stringResource(R.string.decrypt_password)
    val restoreRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.also { uri ->
                downloadsInputUri = uri
                passwordTitle.value = decryptPasswordTitle
                showPasswordDialog.value = true
            }
        }
    }

    val noRestoreMessage = stringResource(R.string.no_restore)
    fun launchRestoreRequest() {
        if (VERSION.SDK_INT < 29) {
            if (!Utils.checkPermissions(ctx, arrayOf(READ_EXTERNAL_STORAGE))) {
                alertTitle.value = noticeTitle
                alertMessage.value = noRestoreMessage
                showAlert.value = true
            }
            else {
                val path = Utils.downloadsPath("baresip.bs")
                downloadsInputUri = File(path).toUri()
                passwordTitle.value = decryptPasswordTitle
                showPasswordDialog.value = true
            }
        }
        else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Downloads.EXTERNAL_CONTENT_URI)
            }
            restoreRequestLauncher.launch(intent)
        }
    }

    val logcatRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK)
            result.data?.data?.also { uri ->
                try {
                    val out = ctx.contentResolver.openOutputStream(uri)
                    val process = Runtime.getRuntime().exec("logcat -d --pid=${Process.myPid()}")
                    val bufferedReader = process.inputStream.bufferedReader()
                    bufferedReader.forEachLine { line ->
                        out!!.write(line.toByteArray())
                        out.write('\n'.code.toByte().toInt())
                    }
                    out!!.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to write logcat to file: $e")
                }
            }
    }

    fun launchLogcatRequest() {
        if (VERSION.SDK_INT >= 29) {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TITLE,
                    "baresip_logcat_" + SimpleDateFormat(
                        "yyyy_MM_dd_HH_mm_ss",
                        Locale.getDefault()
                    ).format(Date())
                )
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, MediaStore.Downloads.EXTERNAL_CONTENT_URI)
            }
            logcatRequestLauncher.launch(intent)
        }
    }

    LaunchedEffect(key1 = call?.status, key2 = configuration.orientation) {
        val isConnected = call != null && call.status.value == "connected" && !call.held
        if (isConnected) {
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                call.focusDtmf.value = true
                delay(300.milliseconds)
                keyboardController?.show()
            }
            else
                keyboardController?.hide()
        }
    }

    if (showPasswordDialog.value)
        PasswordDialog(
            ctx = ctx,
            showPasswordDialog = showPasswordDialog,
            password = password,
            keyboardController = keyboardController,
            title = passwordTitle.value,
            okAction = {
                if (password.value != "") {
                    if (passwordTitle.value == encryptPasswordTitle)
                        backup(ctx, password.value)
                    else
                        restore(ctx, password.value, onRestartClick)
                    password.value = ""
                }
            },
            cancelAction = {
                if (downloadsOutputUri != null) {
                    Utils.deleteFile(ctx, downloadsOutputUri!!)
                }
            }
        )

    if (showPasswordsDialog.value) {
        if (passwordAccounts.isNotEmpty()) {
            val account = passwordAccounts.removeAt(0)
            val params = account.substringAfter(">")
            if (Utils.paramValue(params, "auth_user") != "" &&
                    Utils.paramValue(params, "auth_pass") == "") {
                val aor = account.substringAfter("<").substringBefore(">")
                PasswordDialog(
                    ctx = ctx,
                    showPasswordDialog = showPasswordsDialog,
                    password = password,
                    keyboardController = keyboardController,
                    title = stringResource(R.string.authentication_password),
                    message = stringResource(R.string.account) + " " + Utils.plainAor(aor),
                    okAction = {
                        if (password.value != "")
                            BaresipService.aorPasswords[aor] = password.value
                        showPasswordsDialog.value = true
                    },
                    cancelAction = {
                        showPasswordsDialog.value = true
                    }
                )
            }
            else {
                if (passwordAccounts.isEmpty()) {
                    showPasswordsDialog.value = false
                    onRequestPermissions()
                } else {
                    showPasswordsDialog.value = false
                    showPasswordsDialog.value = true
                }
            }
        }
        else {
            showPasswordsDialog.value = false
            onRequestPermissions()
        }
    }

    LaunchedEffect(Unit) {
        if (!BaresipService.isServiceRunning) {
            val path = ctx.filesDir.absolutePath + "/accounts"
            if (File(path).exists()) {
                val accounts = String(
                    Utils.getFileContents(path)!!,
                    Charsets.UTF_8
                ).lines()
                passwordAccounts = accounts.filter { account ->
                    val params = account.substringAfter(">")
                    Utils.paramValue(params, "auth_user").isNotEmpty() &&
                    Utils.paramValue(params, "auth_pass").isEmpty()
                }.toMutableList()

                if (passwordAccounts.isNotEmpty()) {
                    showPasswordsDialog.value = true
                } else {
                    showPasswordsDialog.value = false
                    onRequestPermissions()
                }
            }
            else {
                showPasswordsDialog.value = false
                onRequestPermissions()
            }
        }
    }

    if (showVideoLayout.value) {
        VideoLayout(ctx = ctx, viewModel = viewModel, onCloseVideo = { showVideoLayout.value = false })
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize().imePadding(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    viewModel = viewModel,
                    navController = navController,
                    onSettingsClick = { navController.navigate("settings") },
                    onAccountsClick = { navController.navigate("accounts") },
                    onBackupClick = { launchBackupRequest() },
                    onRestoreClick = { launchRestoreRequest() },
                    onLogcatClick = { launchLogcatRequest() },
                    onRestartClick = onRestartClick,
                    onQuitClick = onQuitClick
                )
            },
            content = { contentPadding ->
                MainContent(navController, viewModel, contentPadding)
            }
        )
    }
}

@Composable
private fun TopAppBar(
    viewModel: ViewModel,
    navController: NavController,
    onSettingsClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onLogcatClick: () -> Unit,
    onRestartClick: () -> Unit,
    onQuitClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val settings = stringResource(R.string.configuration)
    val accounts = stringResource(R.string.accounts)
    val backup = stringResource(R.string.backup)
    val restore = stringResource(R.string.restore)
    val logcat = stringResource(R.string.logcat)
    val restart = stringResource(R.string.restart)
    val quit = stringResource(R.string.quit)

    val menuItems = remember(settings, accounts, backup, restore, logcat, restart, quit) {
        val list = mutableListOf<MenuItem>(
            MenuItem(settings, Icons.Outlined.Settings),
            MenuItem(accounts, Icons.Outlined.AccountCircle),
            MenuItem(backup, Icons.Outlined.Backup),
            MenuItem(restore, Icons.Outlined.Restore)
        )
        if (VERSION.SDK_INT >= 29) {
            list.add(MenuItem(logcat, Icons.Outlined.Description))
        }
        list.add(MenuItem(restart, Icons.Outlined.RestartAlt))
        list.add(MenuItem(quit, Icons.Outlined.ExitToApp))
        list
    }

    CustomElements.ModernTopAppBar(
        actions = {
            IconButton(
                onClick = { menuExpanded = !menuExpanded }
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }
            CustomElements.DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                menuItems = menuItems,
                onItemClick = { selected ->
                    menuExpanded = false
                    when (selected) {
                        settings -> onSettingsClick()
                        accounts -> onAccountsClick()
                        backup -> onBackupClick()
                        restore -> onRestoreClick()
                        logcat -> onLogcatClick()
                        restart -> onRestartClick()
                        quit -> onQuitClick()
                    }
                }
            )
        },
        title = {
            AnimatedTypewriterTitle()
        }
    )
}

@Composable
fun AnimatedTypewriterTitle(modifier: Modifier = Modifier) {
    var displayedChars by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            // 1. Type in characters one by one
            for (i in 1..14) {
                displayedChars = i
                delay(90)
            }
            // 2. Pause when full
            delay(2400)
            // 3. Delete characters one by one
            for (i in 14 downTo 0) {
                displayedChars = i
                delay(40)
            }
            // 4. Pause before restarting
            delay(600)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    val baresipText = if (displayedChars <= 8) "Baresip ".take(displayedChars) else "Baresip "
    val proText = if (displayedChars > 8) "Pro".take(displayedChars - 8) else ""
    val maxText = if (displayedChars > 11) "max".take(displayedChars - 11) else ""

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (baresipText.isNotEmpty()) {
            Text(
                text = baresipText,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White
            )
        }
        if (proText.isNotEmpty()) {
            Text(
                text = proText,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color(0xFF38BDF8)
            )
        }
        if (maxText.isNotEmpty()) {
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = maxText,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Color(0xFFFF9100),
                modifier = Modifier.offset(y = (-6).dp)
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 2.dp)
                .width(2.dp)
                .height(18.dp)
                .background(Color(0xFFFF9100).copy(alpha = cursorAlpha), RoundedCornerShape(1.dp))
        )
    }
}

private val alertTitle = mutableStateOf("")
private val alertMessage = mutableStateOf("")
private val showAlert = mutableStateOf(false)

private val dialogTitle = mutableStateOf("")
private val dialogMessage = mutableStateOf("")
private val firstText = mutableStateOf("")
private val onFirstClicked = mutableStateOf({})

private val secondText = mutableStateOf("")
private val onSecondClicked = mutableStateOf({})
private val lastText = mutableStateOf("")
private val onLastClicked = mutableStateOf({})
private val showDialog = mutableStateOf(false)

@Composable
private fun NewDialerCard(ctx: Context, viewModel: ViewModel, dialerState: ViewModel.DialerState, navController: NavController) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val primaryCyan = Color(0xFF00B0FF)
    val emeraldNeon = Color(0xFF00E676)
    val emeraldDeep = Color(0xFF00C853)

    val currentUa = UserAgent.ofAor(viewModel.selectedAor.value)
    val defaultNumeric = currentUa?.account?.numericKeypad ?: true
    var isDialpadMode by rememberSaveable(viewModel.selectedAor.value) {
        mutableStateOf(defaultNumeric)
    }
    var requestFocusOnSwitch by remember { mutableStateOf(false) }

    LaunchedEffect(isDialpadMode) {
        if (requestFocusOnSwitch) {
            focusRequester.requestFocus()
            keyboardController?.show()
            requestFocusOnSwitch = false
        }
    }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = dialerState.callUri.value,
                selection = TextRange(dialerState.callUri.value.length)
            )
        )
    }

    val suggestions by remember { contactNames }
    var filteredSuggestions by remember { mutableStateOf<List<AnnotatedString>>(emptyList()) }
    val lazyListState = rememberLazyListState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .shadow(6.dp, RoundedCornerShape(26.dp)),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF131B2E) else Color(0xFFFFFFFF)
        ),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Modern Alphanumeric / Dialpad Text Field
            key(isDialpadMode) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        dialerState.callUri.value = newValue.text
                        if (newValue.text.length > 1) {
                            val normalizedInput = Utils.unaccent(newValue.text)
                            filteredSuggestions = suggestions
                                .filter { suggestion ->
                                    Utils.unaccent(suggestion).contains(normalizedInput, ignoreCase = true)
                                }
                                .map { suggestion ->
                                    Utils.buildAnnotatedStringWithHighlight(suggestion, newValue.text)
                                }
                            dialerState.showSuggestions.value = filteredSuggestions.isNotEmpty()
                        } else {
                            filteredSuggestions = emptyList()
                            dialerState.showSuggestions.value = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.enter_uri),
                            fontSize = 15.sp,
                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = {
                                requestFocusOnSwitch = true
                                isDialpadMode = !isDialpadMode
                            },
                            modifier = Modifier
                                .padding(start = 6.dp, end = 4.dp)
                                .size(38.dp)
                                .background(
                                    if (isDark) primaryCyan.copy(alpha = 0.15f) else primaryCyan.copy(alpha = 0.10f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isDialpadMode) Icons.Filled.Dialpad else Icons.Filled.Keyboard,
                                contentDescription = if (isDialpadMode) "Switch to keyboard" else "Switch to dialpad",
                                tint = primaryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailingIcon = {
                        if (textFieldValue.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    textFieldValue = TextFieldValue("")
                                    dialerState.callUri.value = ""
                                    dialerState.showSuggestions.value = false
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrBlank()) {
                                IconButton(
                                    onClick = {
                                        textFieldValue = TextFieldValue(clipText, TextRange(clipText.length))
                                        dialerState.callUri.value = clipText
                                    },
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = primaryCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        unfocusedContainerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                        focusedBorderColor = primaryCyan,
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0),
                        cursorColor = primaryCyan
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isDialpadMode) KeyboardType.Phone else KeyboardType.Email,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (dialerState.callUri.value.isNotEmpty()) {
                                callClick(ctx, viewModel, dialerState, false, navController)
                            }
                        }
                    )
                )
            }

            // Contact Suggestions Dropdown List (if matching contacts exist)
            if (dialerState.showSuggestions.value && filteredSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color(0xFFE2E8F0))
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = lazyListState
                    ) {
                        items(
                            items = filteredSuggestions,
                            key = { suggestion -> suggestion.toString() }
                        ) { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val selectedStr = suggestion.toString()
                                        textFieldValue = TextFieldValue(selectedStr, TextRange(selectedStr.length))
                                        dialerState.callUri.value = selectedStr
                                        dialerState.showSuggestions.value = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = primaryCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = suggestion,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color.White else Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Quick Redial hint when text field is empty
            if (textFieldValue.text.isEmpty()) {
                val currentUa = UserAgent.ofAor(viewModel.selectedAor.value)
                val latestPeerUri = currentUa?.let { CallHistoryNew.aorLatestPeerUri(it.account.aor) }
                if (latestPeerUri != null) {
                    val friendly = Utils.friendlyUri(ctx, latestPeerUri, currentUa.account)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                dialerState.redialUri = latestPeerUri
                                textFieldValue = TextFieldValue(friendly, TextRange(friendly.length))
                                dialerState.callUri.value = friendly
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recent: $friendly",
                                fontSize = 12.sp,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual Action Call Buttons (Voice Call & Video Call)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Call Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF00E676))
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            callClick(ctx, viewModel, dialerState, false, navController)
                        },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(emeraldNeon, emeraldDeep)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = stringResource(R.string.call),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.call),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Video Call Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .shadow(6.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFF00B0FF))
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            callClick(ctx, viewModel, dialerState, true, navController)
                        },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF00B0FF), Color(0xFF0080FF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = stringResource(R.string.video_call),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.video_call),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallCard(
    ctx: Context,
    viewModel: ViewModel,
    call: Call?,
    dialerState: ViewModel.DialerState?
) {
    Column {
        CallUriRow(ctx, viewModel, call, dialerState)
        CallRow(ctx, viewModel, call, dialerState)
        if (call != null && call.showOnHoldNotice.value)
            OnHoldNotice()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(navController: NavController, viewModel: ViewModel, contentPadding: PaddingValues) {

    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    var offset by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 200
    val ctx = LocalContext.current

    val calls by viewModel.calls.collectAsState()
    val selectedAor by viewModel.selectedAor.collectAsState()
    val isDialpadVisible by viewModel.isDialpadVisible.collectAsState()
    val ua = uas.value.find { it.account.aor == selectedAor }
    val aorCalls = calls.filter { it.ua.account.aor == selectedAor }
    val hasActiveCalls = aorCalls.any { !it.callOnHold.value }
    val conferenceCall = aorCalls.any { it.conferenceCall }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(1000.milliseconds)
            isRefreshing = false
        }
    }

    if (showAlert.value)
        AlertDialog(
            showDialog = showAlert,
            title = alertTitle.value,
            message = alertMessage.value,
            lastButtonText = stringResource(R.string.ok),
        )

    if (showDialog.value)
        AlertDialog(
            showDialog = showDialog,
            title = stringResource(R.string.confirmation),
            message = dialogMessage.value,
            firstButtonText = firstText.value,
            onFirstClicked = onFirstClicked.value,
            secondButtonText = secondText.value,
            onSecondClicked = onSecondClicked.value,
            lastButtonText = lastText.value,
            onLastClicked = onLastClicked.value,
        )

    SelectableAlertDialog(
        openDialog = showSelectItemDialog,
        title = stringResource(R.string.choose_destination_uri),
        items = selectItems.value,
        onItemClicked = selectItemAction.value,
        neutralButtonText = stringResource(R.string.cancel),
        onNeutralClicked = {}
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .padding(start = 12.dp, end = 12.dp, bottom = 90.dp)
            .fillMaxSize()
            .pullToRefresh(
                state = refreshState,
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    if (uas.value.isNotEmpty()) {
                        if (viewModel.selectedAor.value == "")
                            spinToAor(viewModel, uas.value.first().account.aor)
                        val currentUa = UserAgent.ofAor(viewModel.selectedAor.value)
                        if (currentUa?.account?.regint ?: 0 > 0)
                            Api.ua_register(currentUa!!.uap)
                    }
                },
                enabled = pullToRefreshEnabled.value,
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset = 0f },
                    onDragEnd = {
                        if (offset < -swipeThreshold) {
                            if (uas.value.isNotEmpty()) {
                                val curPos = UserAgent.findAorIndex(viewModel.selectedAor.value)
                                val newPos = if (curPos == null) 0 else (curPos + 1) % uas.value.size
                                if (curPos != newPos) {
                                    val newUa = uas.value[newPos]
                                    spinToAor(viewModel, newUa.account.aor)
                                    showCall(ctx, viewModel, newUa)
                                }
                            }
                        }
                        else if (offset > swipeThreshold) {
                            if (uas.value.isNotEmpty()) {
                                val curPos = UserAgent.findAorIndex(viewModel.selectedAor.value)
                                val newPos = when (curPos) {
                                    null -> 0
                                    0 -> uas.value.size - 1
                                    else -> curPos - 1
                                }
                                if (curPos != newPos) {
                                    val newUa = uas.value[newPos]
                                    spinToAor(viewModel, newUa.account.aor)
                                    showCall(ctx, viewModel, newUa)
                                }
                            }
                        }
                    }
                ) { _, dragAmount ->
                    offset += dragAmount
                }
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        AccountSpinner(ctx, viewModel, navController)

        // Active Calls Banners (if any ongoing / minimized calls exist)
        val activeCallsList = if (aorCalls.isNotEmpty()) aorCalls else calls
        if (activeCallsList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeCallsList.forEach { call ->
                    key(call.callp) {
                        ActiveCallBanner(ctx, call, navController, viewModel)
                    }
                }
            }
        }

        val showEmptyCard = if (ua?.account?.isMobile == true)
            aorCalls.isEmpty()
        else
            !hasActiveCalls || conferenceCall

        if (showEmptyCard && isDialpadVisible) {
            Spacer(modifier = Modifier.height(16.dp))
            NewDialerCard(ctx = ctx, viewModel = viewModel, dialerState = viewModel.dialerState, navController = navController)
        }

        Indicator(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            isRefreshing = isRefreshing,
            state = refreshState,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountSpinner(ctx: Context, viewModel: ViewModel, navController: NavController) {

    var expanded by rememberSaveable { mutableStateOf(false) }
    val selected: String by viewModel.selectedAor.collectAsState()
    val accountUpdate by viewModel.accountUpdate.collectAsState()
    val uasValue by uas

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    LaunchedEffect(uasValue, selected, accountUpdate) {
        val currentSelected = viewModel.selectedAor.value
        if (uas.value.isEmpty()) {
            if (currentSelected != "") viewModel.updateSelectedAor("")
        }
        else if (currentSelected == "" || UserAgent.ofAor(currentSelected) == null)
            viewModel.updateSelectedAor(uas.value.first().account.aor)
        val currentUa = UserAgent.ofAor(viewModel.selectedAor.value)
        if (currentUa != null)
            showCall(ctx, viewModel, currentUa, viewModel.focusedCall.value)
    }

    if (selected == "") {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .height(56.dp)
                .shadow(4.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .clickable { navController.navigate("accounts") },
            shape = RoundedCornerShape(22.dp),
            color = if (isDark) Color(0xFF131B2E) else Color(0xFFFFFFFF),
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = Color(0xFF00B0FF),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.accounts),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }
        }
    }
    else {
        val currentUa = UserAgent.ofAor(selected)
        val acc = currentUa?.account
        val displayName = acc?.nickName?.ifEmpty { null }
            ?: acc?.displayName?.ifEmpty { null }
            ?: acc?.text()
            ?: selected
        val serverHost = acc?.aor?.substringAfter("@")?.ifEmpty { null } ?: "SIP Account"

        val statusDrawable = uasStatus.value[selected] ?: R.drawable.circle_yellow
        val (statusColor, statusLabel) = when (statusDrawable) {
            R.drawable.circle_green -> Pair(Color(0xFF00E676), "Online")
            R.drawable.circle_red -> Pair(Color(0xFFFF3B30), "Offline")
            else -> Pair(Color(0xFFF9A825), "Connecting...")
        }

        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(22.dp))
                    .combinedClickable(
                        onClick = { expanded = true },
                        onLongClick = {
                            if (currentUa != null && acc != null && !acc.isMobile) {
                                if (Api.account_regint(acc.accp) > 0) {
                                    Api.account_set_regint(acc.accp, 0)
                                    Api.ua_unregister(currentUa.uap)
                                } else {
                                    Api.account_set_regint(acc.accp, acc.configuredRegInt)
                                    Api.ua_register(currentUa.uap)
                                }
                                acc.regint = Api.account_regint(acc.accp)
                                Account.saveAccounts()
                            }
                        }
                    ),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131B2E) else Color(0xFFFFFFFF)
                ),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glowing Status Badge (Clickable to open account settings)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(statusColor.copy(alpha = if (isDark) 0.18f else 0.12f), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate("account/${Uri.encode(selected)}/old")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(statusColor, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Account Name & Details
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
                            Text(
                                text = serverHost,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Text(
                                text = " • $statusLabel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Trailing Switcher Chevron Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Switch Account",
                            tint = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF475569),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Dropdown Menu for Account Switching
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 260.dp, max = 340.dp),
                shape = RoundedCornerShape(18.dp),
                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)
                ),
                shadowElevation = 6.dp
            ) {
                uas.value.forEachIndexed { index, currentItemUa ->
                    val itemAcc = currentItemUa.account
                    val isCurrent = itemAcc.aor == selected
                    val itemStatusDrawable = uasStatus.value[itemAcc.aor] ?: R.drawable.circle_yellow
                    val itemStatusColor = when (itemStatusDrawable) {
                        R.drawable.circle_green -> Color(0xFF00E676)
                        R.drawable.circle_red -> Color(0xFFFF3B30)
                        else -> Color(0xFFF9A825)
                    }
                    val itemName = itemAcc.nickName.ifEmpty { null }
                        ?: itemAcc.displayName.ifEmpty { null }
                        ?: itemAcc.text()

                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            spinToAor(viewModel, itemAcc.aor)
                        },
                        text = {
                            Column {
                                Text(
                                    text = itemName,
                                    fontSize = 15.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = itemAcc.aor.substringAfter("@"),
                                    fontSize = 12.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(itemStatusColor, CircleShape)
                            )
                        },
                        trailingIcon = {
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Active",
                                    tint = Color(0xFF00B0FF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                    if (index < uas.value.size - 1) {
                        HorizontalDivider(
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallUriRow(
    ctx: Context,
    viewModel: ViewModel,
    call: Call?,
    dialerState: ViewModel.DialerState?
) {

    val isDialer = dialerState != null

    val suggestions by remember { contactNames }
    var filteredSuggestions by remember { mutableStateOf<List<AnnotatedString>>(emptyList()) }
    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()
    val isDialpadVisible by viewModel.isDialpadVisible.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(
                value = if (isDialer) dialerState.callUri.value else call!!.callUri.value,
                readOnly = if (isDialer) !dialerState.callUriEnabled.value else !call!!.callUriEnabled.value,
                singleLine = true,
                onValueChange = {
                    if (isDialer)
                        if (it != dialerState.callUri.value) {
                            dialerState.callUri.value = it
                            dialerState.redialUri = ""
                            if (it == "") {
                                dialerState.showCallButton.value = true
                                dialerState.showCallConferenceButton.value = true
                                dialerState.showCallVideoButton.value = true
                            }
                            val normalizedInput = Utils.unaccent(it)
                            filteredSuggestions = suggestions
                                .filter { suggestion ->
                                    it.length > 1 &&
                                            Utils.unaccent(suggestion)
                                                .contains(normalizedInput, ignoreCase = true)
                                }
                                .map { suggestion ->
                                    Utils.buildAnnotatedStringWithHighlight(suggestion, it)
                                }
                            dialerState.showSuggestions.value = it.length > 1
                        }
                },
                trailingIcon = {
                    if (isDialer && dialerState.callUriEnabled.value && dialerState.callUri.value.isNotEmpty())
                        Icon(Icons.Outlined.Clear,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                if (dialerState.showSuggestions.value)
                                    dialerState.showSuggestions.value = false
                                dialerState.callUri.value = ""
                                dialerState.showCallButton.value = true
                                dialerState.showCallConferenceButton.value = true
                                dialerState.showCallVideoButton.value = true
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 2.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (isDialer) {
                            val account = Account.ofAor(viewModel.selectedAor.value)
                            if (account != null && account.numericKeypad)
                                if (!isDialpadVisible)
                                    viewModel.toggleDialpadVisibility()
                        }
                    },
                label = {
                    Text(
                        text = if (isDialer)
                            dialerState.callUriLabel.value
                        else
                            call!!.callUriLabel.value,
                        fontSize = 18.sp
                    )
                },
                textStyle = TextStyle(fontSize = 18.sp),
                keyboardOptions = if (isDialpadVisible)
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Default
                    )
                else
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Default
                    )
            )
            if (!isDialer && call!!.callUri2.value != "") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = call.callUri2.value,
                    readOnly = true,
                    singleLine = true,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                    label = {
                        Text(text = call.callUriLabel2.value, fontSize = 18.sp)
                    },
                    textStyle = TextStyle(fontSize = 18.sp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(8.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .animateContentSize()
            ) {
                if (isDialer && dialerState.showSuggestions.value && filteredSuggestions.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 150.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScrollbar(state = lazyListState, width = 6.dp),
                            horizontalAlignment = Alignment.Start,
                            state = lazyListState
                        ) {
                            items(
                                items = filteredSuggestions,
                                key = { suggestion -> suggestion.toString() }
                            ) { suggestion ->
                                Text(
                                    text = suggestion,
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            dialerState.callUri.value = suggestion.toString()
                                            dialerState.showSuggestions.value = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 12.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
fun CallTimer(initialDurationSeconds: Long, modifier: Modifier = Modifier) {
    var totalSeconds by remember { mutableStateOf(initialDurationSeconds) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            totalSeconds++
        }
    }

    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val timeString = if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    Text(
        text = timeString,
        fontSize = 18.sp,
        modifier = modifier
    )
}

@Composable
private fun CallRow(
    ctx: Context,
    viewModel: ViewModel,
    call: Call?,
    dialerState: ViewModel.DialerState?
) {
    val isDialer = dialerState != null
    ButtonsRow(ctx, viewModel, call, dialerState, isDialer)
}

@Composable
private fun ButtonsRow(
    ctx: Context,
    viewModel: ViewModel,
    call: Call?,
    dialerState: ViewModel.DialerState?,
    isDialer: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            if (isDialer || call?.showCancelButton?.value == true ||
                    call?.showAnswerRejectButtons?.value == true)
                Arrangement.Center
            else
                Arrangement.SpaceBetween
    ) {
        if (isDialer) {
            if (dialerState!!.showCallButton.value)
                IconButton(
                    modifier = Modifier.size(48.dp),
                    enabled = dialerState.callButtonsEnabled.value,
                    onClick = {
                        if (!dialerState.callButtonsEnabled.value) return@IconButton
                        dialerState.showCallConferenceButton.value = false
                        dialerState.showCallVideoButton.value = false
                        dialerState.showSuggestions.value = false
                        callClick(ctx, viewModel, dialerState, false)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallIcon,
                        modifier = Modifier.size(42.dp),
                        tint = colorResource(if (dialerState.callButtonsEnabled.value)
                            R.color.colorTrafficGreen
                        else
                            R.color.colorTrafficYellow),
                        contentDescription = null,
                    )
                }
            if (dialerState.showCallConferenceButton.value) {
                if (dialerState.showCallButton.value)
                    Spacer(modifier = Modifier.width(48.dp))
                IconButton(
                    modifier = Modifier.size(48.dp),
                    enabled = dialerState.callButtonsEnabled.value,
                    onClick = {
                        if (!dialerState.callButtonsEnabled.value) return@IconButton
                        dialerState.showCallButton.value = false
                        dialerState.showCallVideoButton.value = false
                        dialerState.showSuggestions.value = false
                        callClick(ctx, viewModel, dialerState, false)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddIcCall,
                        modifier = Modifier.size(42.dp),
                        tint = colorResource(
                            if (dialerState.callButtonsEnabled.value)
                                R.color.colorTrafficGreen
                            else
                                R.color.colorTrafficYellow
                        ),
                        contentDescription = null,
                    )
                }
            }
            if (dialerState.showCallVideoButton.value) {
                if (dialerState.showCallButton.value || dialerState.showCallConferenceButton.value)
                    Spacer(modifier = Modifier.width(48.dp))
                IconButton(
                    modifier = Modifier.size(48.dp),
                    enabled = dialerState.callButtonsEnabled.value,
                    onClick = {
                        if (!dialerState.callButtonsEnabled.value) return@IconButton
                        dialerState.showCallButton.value = false
                        dialerState.showCallConferenceButton.value = false
                        dialerState.showSuggestions.value = false
                        callClick(ctx, viewModel, dialerState, true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        modifier = Modifier.size(42.dp),
                        tint = colorResource(
                            if (dialerState.callButtonsEnabled.value)
                                R.color.colorTrafficGreen
                            else
                                R.color.colorTrafficYellow
                        ),
                        contentDescription = null,
                    )
                }
            }
        }
        else {
            if (call!!.showCancelButton.value) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    enabled = !call.terminated.value,
                    onClick = {
                        if (call.terminated.value) return@IconButton
                        call.terminated.value = true
                        Log.d(TAG, "AoR ${call.ua.account.aor} canceling call ${call.callp}")
                        call.hangup(487, "Request Terminated")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        modifier = Modifier.size(42.dp),
                        tint = colorResource(R.color.colorTrafficRed),
                        contentDescription = null,
                    )
                }
            }

            if (call.showHangupButton.value) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    enabled = !call.terminated.value,
                    onClick = {
                        if (call.terminated.value) return@IconButton
                        call.terminated.value = true
                        Log.d(TAG, "AoR ${call.ua.account.aor} hanging up call ${call.callp}")
                        call.hangup(487, "Request Terminated")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        modifier = Modifier.size(42.dp),
                        tint = colorResource(R.color.colorTrafficRed),
                        contentDescription = null,
                    )
                }

                if (call.videoIcon.value != Video.NONE)
                    IconButton(
                        modifier = Modifier.size(48.dp).padding(start = 4.dp),
                        onClick = {
                            call.videoIcon.value = Video.PENDING
                            videoClick(ctx, call)
                        }
                    ) {
                        Icon(
                            imageVector = if (call.videoIcon.value == Video.OFF)
                                Icons.Filled.VideocamOff
                            else
                                Icons.Filled.VideoCall,
                            modifier = Modifier.size(42.dp),
                            tint = if (call.videoIcon.value == Video.PENDING)
                                colorResource(R.color.colorTrafficYellow)
                            else
                                colorResource(R.color.colorTrafficGreen),
                            contentDescription = null,
                        )
                    }

                if (call.showCallTimer.value)
                    CallTimer(call.callDuration.toLong(), modifier = Modifier.padding(start = 4.dp))
            }

            if (call.showAnswerRejectButtons.value) {
                IconButton(
                    modifier = Modifier.size(48.dp),
                    onClick = {
                        answer(ctx, viewModel, call, false)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallIcon,
                        modifier = Modifier.size(42.dp),
                        tint = colorResource(R.color.colorTrafficGreen),
                        contentDescription = null,
                    )
                }

                if (call.hasVideo()) {
                    Spacer(Modifier.width(48.dp))
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = {
                            answer(ctx, viewModel, call, true)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            modifier = Modifier.size(42.dp),
                            tint = colorResource(R.color.colorTrafficGreen),
                            contentDescription = null,
                        )
                    }
                }

                Spacer(Modifier.width(48.dp))
                IconButton(
                    modifier = Modifier.size(48.dp),
                    onClick = {
                        reject(call)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        modifier = Modifier.size(42.dp),
                        tint = colorResource(R.color.colorTrafficRed),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnHoldNotice() {
    Text(
        text = stringResource(R.string.call_is_on_hold),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = colorResource(R.color.colorTrafficRed),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
    )
}

private fun spinToAor(viewModel: ViewModel, aor: String, call: Call? = null) {
    if (aor != "") {
        val aorChanged = aor != viewModel.selectedAor.value
        if (aorChanged) viewModel.updateSelectedAor(aor)
        viewModel.triggerAccountUpdate(call)
    }
}

enum class Video { NONE, ON, PENDING, OFF }

private fun videoClick(ctx: Context, call: Call) {
    if (call.videoIcon.value == Video.PENDING) {
        val newDir = if (call.hasVideo())
            Api.SDP_RECVONLY
        else
            Api.SDP_SENDRECV
        call.setVideoDirection(newDir)
        Handler(Looper.getMainLooper()).postDelayed({
            if (call.videoIcon.value == Video.PENDING) {
                call.videoIcon.value = if (!call.hasVideo())
                    Video.OFF
                else
                    Video.ON
            }
        }, 250)
        showVideoLayout.value = true
    }
}

@Composable
fun VideoLayout(ctx: Context, viewModel: ViewModel, onCloseVideo: () -> Unit) {
    val selectedAor by viewModel.selectedAor.collectAsState()
    val ua = uas.value.find { it.account.aor == selectedAor }
    val call = ua?.currentCall()
    if (call != null) {
        VideoCallScreen(ctx = ctx, viewModel = viewModel, call = call, onCloseVideo = onCloseVideo)
    } else {
        onCloseVideo()
    }
}

private fun callClick(
    ctx: Context,
    viewModel: ViewModel,
    dialerState: ViewModel.DialerState?,
    video: Boolean = false,
    navController: NavController? = null
) {
    if (BaresipService.uas.value.isEmpty() || viewModel.selectedAor.value == "" || UserAgent.ofAor(viewModel.selectedAor.value) == null) {
        Toast.makeText(ctx, R.string.no_account_available, Toast.LENGTH_SHORT).show()
        navController?.navigate("accounts")
        return
    }
    if (dialerState != null) {
        val uriText = dialerState.callUri.value.trim()
        if (uriText.isNotEmpty()) {
            if (Utils.checkPermissions(ctx, arrayOf(RECORD_AUDIO))) {
                val aor = viewModel.selectedAor.value
                val ua = UserAgent.ofAor(aor)
                val uriToCall = if (dialerState.redialUri != "" &&
                        uriText == Utils.friendlyUri(ctx, dialerState.redialUri, ua!!.account))
                    dialerState.redialUri
                else {
                    val uris = Contact.contactUris(uriText, ua?.account?.isMobile ?: false)
                    if (uris.isEmpty()) {
                        if (Contact.nameExists(uriText, BaresipService.contacts, true)) {
                            alertTitle.value = ctx.getString(R.string.notice)
                            alertMessage.value = if (ua?.account?.isMobile == true)
                                String.format(ctx.getString(R.string.contact_no_tel_uri), uriText)
                            else
                                String.format(ctx.getString(R.string.contact_no_sip_or_tel_uri), uriText)
                            showAlert.value = true
                            return
                        }
                        uriText
                    }
                    else if (uris.size == 1)
                        uris[0].uri
                    else {
                        selectItems.value = uris.map { it.label.ifEmpty { it.uri.substringAfter(":") } }
                        selectItemAction.value = { index ->
                            makeCall(ctx, viewModel, uris[index].uri, dialerState, video)
                        }
                        showSelectItemDialog.value = true
                        return
                    }
                }

                dialerState.redialUri = ""
                makeCall(ctx, viewModel, uriToCall, dialerState, video)
            }
            else
                Toast.makeText(ctx, R.string.no_calls, Toast.LENGTH_SHORT).show()
        }
        else {
            val ua = UserAgent.ofAor(viewModel.selectedAor.value)!!
            val latestPeerUri = CallHistoryNew.aorLatestPeerUri(ua.account.aor)
            if (latestPeerUri != null) {
                dialerState.redialUri = latestPeerUri
                dialerState.callUri.value = Utils.friendlyUri(ctx, latestPeerUri, ua.account)
            }
        }
    }
}

private fun makeCall(ctx: Context, viewModel: ViewModel, uriText: String,
                     dialerState: ViewModel.DialerState, videoCall: Boolean = false, onHoldCallp: Long = 0L) {
    val aor = viewModel.selectedAor.value
    val ua = UserAgent.ofAor(aor)!!
    val peerUri = if (Utils.isTelNumber(uriText))
        "tel:$uriText"
    else
        uriText
    val uri = if (Utils.isTelUri(peerUri)) {
        if (ua.account.isMobile)
            peerUri
        else if (ua.account.telProvider == "") {
            alertTitle.value = ctx.getString(R.string.notice)
            alertMessage.value = String.format(ctx.getString(R.string.no_telephony_provider), aor)
            showAlert.value = true
            return
        }
        else
            Utils.telToSip(peerUri, ua.account)
    }
    else
        Utils.uriComplete(peerUri, aor)
    if (!Utils.checkUri(uri)) {
        alertTitle.value = ctx.getString(R.string.notice)
        alertMessage.value = String.format(ctx.getString(R.string.invalid_sip_or_tel_uri), uri)
        showAlert.value = true
        return
    }
    else if (ua.account.isMobile && !Utils.isTelUri(uri)) {
        alertTitle.value = ctx.getString(R.string.notice)
        alertMessage.value = ctx.getString(R.string.no_telephone_number)
        showAlert.value = true
        return
    }
    else if (ua.account.isMobile && ua.status != circleGreen.getValue(colorblind)) {
        alertTitle.value = ctx.getString(R.string.notice)
        alertMessage.value = Utils.mobileStatusMessage(ctx, ua.status)
        showAlert.value = true
        return
    }
    else if (Utils.isAudioMode(ctx, AudioManager.MODE_IN_CALL) &&
            !Call.calls().any { it.ua.account.aor == ua.account.aor })
        Toast.makeText(ctx, R.string.call_already_active, Toast.LENGTH_SHORT).show()
    else if (ua.account.isMobile && Utils.isUssd(uri)) {
        viewModel.dialerState.callButtonsEnabled.value = false
        val baresipService = Intent(ctx, BaresipService::class.java)
        baresipService.action = "Start Call"
        baresipService.putExtra("uap", ua.uap)
        baresipService.putExtra("uri", uri)
        ContextCompat.startForegroundService(ctx, baresipService)
    }
    else {
        viewModel.dialerState.callButtonsEnabled.value = false
        var error = ""
        val tm = ctx.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (VERSION.SDK_INT >= 29 && ua.account.isMobile) {
            val phoneAccountHandle = Utils.pstnAccountHandle(ctx)
            if (phoneAccountHandle != null) {
                val extras = Bundle().apply {
                    putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
                }
                val callExtras = Bundle()
                callExtras.putBoolean("pstnCall", true)
                callExtras.putString("aor", aor)
                extras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, callExtras)
                try {
                    Log.i(TAG, "Placing Telecom PSTN call to $uri with uap=${ua.uap}")
                    val pstnExtras = Bundle().apply {
                        putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
                            BaresipService.getPhoneAccountHandle(ctx, BaresipService.PSTN_ACCOUNT_ID))
                    }
                    val pstnCallExtras = Bundle()
                    pstnCallExtras.putBoolean("pstnCall", true)
                    pstnCallExtras.putString("aor", aor)
                    pstnExtras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, pstnCallExtras)
                    val telecomUri = if (uri.startsWith("tel:"))
                        Uri.fromParts("tel", uri.substring(4), null)
                    else
                        uri.toUri()
                    tm.placeCall(telecomUri, pstnExtras)
                } catch (e: SecurityException) {
                    error = "placeCall failed: ${e.message}"
                }
            }
            else
                error = "no phone account"
        }
        else {
            val extras = Bundle()
            extras.putParcelable(
                TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
                BaresipService.getPhoneAccountHandle(ctx, BaresipService.SIP_ACCOUNT_ID)
            )
            val callExtras = Bundle()
            callExtras.putBoolean("conferenceCall", dialerState.showCallConferenceButton.value)
            callExtras.putBoolean("videoCall", videoCall)
            callExtras.putLong("uap", ua.uap)
            if (onHoldCallp != 0L)
                callExtras.putLong("onHoldCallp", onHoldCallp)
            extras.putBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, callExtras)
            try {
                Log.d(TAG, "Placing Telecom SIP call to $uri with uap=${ua.uap}")
                tm.placeCall(uri.toUri(), extras)
            } catch (e: SecurityException) {
                error = "placeCall failed: ${e.message}"
            }
        }
        if (error != "") {
            Log.e(TAG, error)
            viewModel.dialerState.callButtonsEnabled.value = true
        }
        else
            viewModel.viewModelScope.launch {
                delay(5000.milliseconds)
                if (Call.calls().isEmpty()) {
                    Log.d(TAG, "Re-enabling dialer buttons after timeout (no calls)")
                    viewModel.dialerState.callButtonsEnabled.value = true
                }
            }
    }
}

fun answer(ctx: Context, viewModel: ViewModel, call: Call, video: Boolean = false) {
    if (Utils.checkPermissions(ctx, arrayOf(RECORD_AUDIO))) {
        call.answer(video)
        showCall(ctx, viewModel, call.ua)
    }
}

fun reject(call: Call) {
    call.hangup(486, "Busy Here")
}

private fun transfer(ctx: Context, viewModel: ViewModel, ua: UserAgent, uriText: String, attended: Boolean) {
    val call = ua.currentCall() ?: return
    val cleanUri = uriText.trim()
    if (cleanUri.isEmpty()) return

    val uri = if (Utils.isTelNumber(cleanUri))
        Utils.telToSip("tel:$cleanUri", ua.account)
    else
        Utils.uriComplete(cleanUri, ua.account.aor)

    if (!Utils.checkUri(uri)) {
        alertTitle.value = ctx.getString(R.string.notice)
        alertMessage.value = String.format(ctx.getString(R.string.invalid_sip_or_tel_uri), uri)
        showAlert.value = true
        return
    }

    if (attended) {
        call.transfer(uri)
    } else {
        call.transfer(uri)
    }
}

private fun showCall(ctx: Context, viewModel: ViewModel, ua: UserAgent?, showCall: Call? = null) {
    if (ua == null) return
    val call = showCall ?: ua.currentCall()
    val aor = ua.account.aor
    val callp = call?.callp ?: 0L
    val status = call?.status?.value ?: "idle"
    val security = call?.security ?: -1

    if (viewModel.isUIRedundant(aor, callp, status, security)) return

    if (call == null) {
        pullToRefreshEnabled.value = !ua.account.isMobile
        viewModel.dialerState.callUri.value = ua.account.resumeUri
        viewModel.dialerState.callUriLabel.value = ctx.getString(R.string.outgoing_call_to_dots)
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.dialerState.callUriEnabled.value = true
        }, 100)
        viewModel.dialerState.showCallButton.value = true
        viewModel.dialerState.showCallConferenceButton.value = !ua.account.isMobile
        viewModel.dialerState.showCallVideoButton.value = true
        viewModel.dialerState.callButtonsEnabled.value = true
        viewModel.dialerState.showSuggestions.value = false
        dialpadButtonEnabled.value = true
        if (BaresipService.isMicMuted) {
            BaresipService.isMicMuted = false
            viewModel.updateMicIcon(Icons.Filled.Mic)
        }
    }
    else {
        viewModel.dialerState.callUri.value = ""
        pullToRefreshEnabled.value = false
        call.callUriEnabled.value = false
        val isLandscape = ctx.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (isLandscape || call.held || call.status.value != "connected") {
            call.focusDtmf.value = false
            call.dtmfEnabled.value = !call.held
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.requestHideKeyboard()
            }, 25)
        }
        else {
            call.dtmfEnabled.value = true
            call.focusDtmf.value = true
            viewModel.requestShowKeyboard()
        }
        Log.d(TAG, "Showing call $callp from $aor with status $status")
        when (status) {
            "outgoing", "transferring", "answered" -> {
                call.callUriLabel.value = if (status == "answered")
                    ctx.getString(R.string.incoming_call_from_dots)
                else
                    ctx.getString(R.string.outgoing_call_to_dots)
                call.callUri.value = Utils.friendlyUri(ctx, call.peerUri, ua.account)
                call.callUri2.value = ""
                call.showCallTimer.value = false
                call.securityIconTint.value = -1
                call.showCallButton.value = false
                call.showCancelButton.value = status == "outgoing"
                call.showHangupButton.value = !call.showCancelButton.value
                call.showAnswerRejectButtons.value = false
                call.showOnHoldNotice.value = false
                dialpadButtonEnabled.value = false
            }
            "incoming" -> {
                call.showCallTimer.value = false
                call.securityIconTint.value = -1
                call.callUriLabel.value = ctx.getString(R.string.incoming_call_from_dots)
                call.callUri.value = Utils.friendlyUri(ctx, call.peerUri, ua.account)
                val uri = call.diverterUri()
                if (uri != "") {
                    call.callUriLabel2.value = ctx.getString(R.string.diverted_by_dots)
                    call.callUri2.value = Utils.friendlyUri(ctx, uri, ua.account)
                }
                else
                    call.callUri2.value = ""
                call.showCallButton.value = false
                call.showCancelButton.value = false
                call.showHangupButton.value = false
                call.showAnswerRejectButtons.value = true
                call.showOnHoldNotice.value = false
                dialpadButtonEnabled.value = false
            }
            "connected" -> {
                if (call.referTo != "") {
                    call.callUriLabel.value = ctx.getString(R.string.outgoing_call_to_dots)
                    call.callUri.value = Utils.friendlyUri(ctx, call.referTo, ua.account)
                    call.transferButtonEnabled.value = false
                } else {
                    if (call.dir == "out") {
                        call.callUriLabel.value = ctx.getString(R.string.outgoing_call_to_dots)
                        call.callUri.value = Utils.friendlyUri(ctx, call.peerUri, ua.account)
                    } else {
                        call.callUriLabel.value = ctx.getString(R.string.incoming_call_from_dots)
                        call.callUri.value = Utils.friendlyUri(ctx, call.peerUri, ua.account)
                    }
                    call.transferButtonEnabled.value = !ua.account.isMobile
                }
                call.callUri2.value = ""
                call.callTransfer.value = call.onHoldCall != null
                call.callDuration = call.duration()
                call.showCallTimer.value = true
                if (ua.account.mediaEnc == "")
                    call.securityIconTint.value = -1
                else
                    call.securityIconTint.value = call.security
                call.showCallButton.value = false
                call.showCancelButton.value = false
                call.showHangupButton.value = true
                call.showAnswerRejectButtons.value = false
                call.callOnHold.value = call.onhold
                Handler(Looper.getMainLooper()).postDelayed({
                    call.showOnHoldNotice.value = call.held
                }, 100)
            }
        }
    }
    viewModel.markUIRendered(aor, callp, status, security)
}

fun handleServiceEvent(ctx: Context, viewModel: ViewModel, event: String, params: ArrayList<Any>) {

    fun handleNextEvent(logMessage: String? = null) {
        if (logMessage != null)
            Log.w(TAG, logMessage)
        synchronized(BaresipService.serviceEvents) {
            if (BaresipService.serviceEvents.isNotEmpty()) {
                val first = BaresipService.serviceEvents.removeAt(0)
                handleServiceEvent(ctx, viewModel, first.event, first.params)
            }
        }
    }

    if (event == "started") {
        val uriString = params[0] as String
        Log.d(TAG, "Handling service event 'started' with URI '$uriString'")
        if (uriString != "")
            callAction(ctx, viewModel, uriString.toUri(), "dial")
        else if (viewModel.selectedAor.value == "" && uas.value.isNotEmpty())
            viewModel.updateSelectedAor(uas.value.first().account.aor)
        if (Preferences(ctx).displayTheme != AppCompatDelegate.getDefaultNightMode())
            AppCompatDelegate.setDefaultNightMode(Preferences(ctx).displayTheme)
        handleNextEvent()
        return
    }

    val ev = event.split(",")
    val uap = params[0] as Long

    when (ev[0]) {
        "mic muted" -> {
            val muted = ev[1].toBoolean()
            if (muted)
                viewModel.updateMicIcon(Icons.Filled.MicOff)
            else
                viewModel.updateMicIcon(Icons.Filled.Mic)
            handleNextEvent()
            return
        }
        "speaker update" -> {
            viewModel.updateSpeakerPhoneStatus(ev[1].toBoolean())
            handleNextEvent()
            return
        }
    }

    val ua = UserAgent.ofUap(uap)
    if (ua == null) {
        handleNextEvent("handleServiceEvent '$event' did not find ua $uap")
        return
    }

    Log.d(TAG, "Handling service event '${ev[0]}' for $uap")
    val acc = ua.account
    val aor = ua.account.aor

    when (ev[0]) {
        "call rejected" -> {}
        "call outgoing" -> {
            val callp = params[1] as Long
            val call = Call.ofCallp(callp)
            spinToAor(viewModel, aor, call)
            viewModel.navigateToCall()
        }
        "call incoming" -> {
            val callp = params[1] as Long
            val call = Call.ofCallp(callp)
            spinToAor(viewModel, aor, call)
            viewModel.navigateToCall()
        }
        "call answered" -> {
            val callp = params[1] as Long
            val call = Call.ofCallp(callp)
            spinToAor(viewModel, aor, call)
            viewModel.navigateToCall()
        }
        "call redirect" -> {
            val redirectUri = ev[1]
            val target = Utils.friendlyUri(ctx, redirectUri, acc)
            if (acc.autoRedirect) {
                redirect(ctx, viewModel, ua, redirectUri)
                Toast.makeText(ctx,
                    String.format(ctx.getString(R.string.redirect_notice), target),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                dialogTitle.value = ctx.getString(R.string.redirect_request)
                dialogMessage.value = String.format(ctx.getString(R.string.redirect_request_query), target)
                firstText.value = ""
                secondText.value = ctx.getString(R.string.no)
                onSecondClicked.value = { }
                lastText.value = ctx.getString(R.string.yes)
                onLastClicked.value = { redirect(ctx, viewModel, ua, redirectUri) }
                showDialog.value = true
            }
        }
        "call established" -> {
            (ctx as? Activity)?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            viewModel.navigateToCall()
            if (aor == viewModel.selectedAor.value) {
                viewModel.dialerState.callButtonsEnabled.value = true
                val callp = params[1] as Long
                val call = Call.ofCallp(callp)
                if (call != null)
                    call.dtmfText.value = ""
            }
        }
        "call update" -> {}
        "call verify" -> {
            val callp = params[1] as Long
            val call = Call.ofCallp(callp)
            if (call == null) {
                handleNextEvent("Call $callp to be verified is not found")
                return
            }
            dialogTitle.value = ctx.getString(R.string.verify)
            dialogMessage.value = String.format(ctx.getString(R.string.verify_sas), ev[1])
            firstText.value = ""
            secondText.value = ctx.getString(R.string.no)
            onSecondClicked.value = {
                call.security = R.color.colorTrafficYellow
                call.zid = ev[2]
                if (aor == viewModel.selectedAor.value)
                    call.securityIconTint.value = R.color.colorTrafficYellow
                secondText.value = ""
            }
            lastText.value = ctx.getString(R.string.yes)
            onLastClicked.value = {
                call.security = if (Api.cmd_exec("zrtp_verify ${ev[2]}") != 0) {
                    Log.e(TAG, "Command 'zrtp_verify ${ev[2]}' failed")
                    R.color.colorTrafficYellow
                }
                else
                    R.color.colorTrafficGreen
                call.zid = ev[2]
                if (aor == viewModel.selectedAor.value)
                    call.securityIconTint.value = call.security
            }
            showDialog.value = true
        }
        "call verified", "call secure" -> {
            val callp = params[1] as Long
            val call = Call.ofCallp(callp)
            if (call == null) {
                handleNextEvent("Call $callp that is verified is not found")
                return
            }
            if (aor == viewModel.selectedAor.value)
                call.securityIconTint.value = call.security
        }
        "call transfer", "transfer show" -> {
            if (!BaresipService.isMainVisible)
                viewModel.navigateToHome()
            val callp = params[1] as Long
            val call = Call.ofCallp(callp)
            val target = Utils.friendlyUri(ctx, ev[1], acc)
            dialogTitle.value = if (call != null)
                ctx.getString(R.string.transfer_request)
            else
                ctx.getString(R.string.call_request)
            dialogMessage.value = if (call != null)
                String.format(ctx.getString(R.string.transfer_request_query), target)
            else
                String.format(ctx.getString(R.string.call_request_query), target)
            firstText.value = ""
            secondText.value = ctx.getString(R.string.no)
            onSecondClicked.value = {
                if (call in Call.calls())
                    call!!.notifySipfrag(603, "Decline")
            }
            lastText.value = ctx.getString(R.string.yes)
            onLastClicked.value = {
                if (call in Call.calls())
                    acceptTransfer(ctx, viewModel, ua, call!!, ev[1])
                else
                    makeCall(ctx, viewModel, ev[1], viewModel.dialerState)
            }
            showDialog.value = true
        }
        "transfer accept" -> {
            val callp = params[1] as Long
            val call = Call.ofCallp(callp)
            if (call in Call.calls())
                call!!.hangup(487, "Request Terminated")
            makeCall(ctx, viewModel, ev[1], viewModel.dialerState)
            showCall(ctx, viewModel, ua)
        }
        "transfer failed" -> {
            showCall(ctx, viewModel, ua)
        }
        "call closed" -> {
            val calls = Call.calls()
            if (calls.isEmpty())
                (ctx as? Activity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            viewModel.updateCalls(calls.toList())
            val activity = ctx as? Activity
            if (activity != null) {
                val kgm = activity.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (kgm.isKeyguardLocked) {
                    activity.moveTaskToBack(true)
                    return
                }
            }
            if (aor == viewModel.selectedAor.value) {
                viewModel.dialerState.callButtonsEnabled.value = true
                ua.account.resumeUri = ""
                viewModel.triggerAccountUpdate()
            }
        }
        "message", "message show", "message reply" -> {
            Log.d(TAG, "handleServiceEvent: Message event '${ev[0]}' for $aor from ${params[1]}. Updating state without auto-jumping to chat.")
            BaresipService.messageUpdate.postValue(System.currentTimeMillis())
            viewModel.triggerAccountUpdate()
        }
        "mwi notify" -> {
            val lines = ev[1].split("\n")
            for (line in lines)
                if (line.startsWith("Voice-Message:")) {
                    val counts = (line.split(" ")[1]).split("/")
                    acc.vmNew = counts[0].toInt()
                    acc.vmOld = counts[1].toInt()
                    break
                }
        }
        else -> Log.e(TAG, "Unknown event '${ev[0]}'")
    }

    viewModel.updateCalls(Call.calls().toList())
    viewModel.triggerAccountUpdate()
    handleNextEvent()
}

fun handleIntent(ctx: Context, viewModel: ViewModel, intent: Intent, action: String) {
    Log.d(TAG, "Handling intent '$action'")
    val ev = action.split(",")
    when (ev[0]) {
        "call", "dial" -> {
            if (Call.inCall()) {
                Toast.makeText(ctx, ctx.getString(R.string.call_already_active),
                    Toast.LENGTH_SHORT).show()
                return
            }
            val uap = intent.getLongExtra("uap", 0L)
            val ua = UserAgent.ofUap(uap)
            if (ua == null) {
                Log.w(TAG, "handleIntent 'call' did not find ua $uap")
                return
            }
            if (ev[0] == "dial") {
                viewModel.navigateToHome()
            }
            viewModel.dialerState.callUri.value = intent.getStringExtra("peer")!!
            spinToAor(viewModel, ua.account.aor)
            if (ev[0] == "call") {
                viewModel.dialerState.showCallConferenceButton.value = false
                viewModel.dialerState.showCallVideoButton.value = false
                callClick(ctx, viewModel, viewModel.dialerState)
            }
        }
        "call show", "call answer" -> {
            val callp = intent.getLongExtra("callp", 0L)
            val call = Call.ofCallp(callp)
            if (call == null) {
                Log.w(TAG, "handleIntent '$action' did not find call $callp")
                return
            }
            viewModel.navigateToCall()
            val ua = call.ua
            spinToAor(viewModel, ua.account.aor, call)
            if (ev[0] == "call answer")
                answer(ctx, viewModel, call)
            else
                BaresipService.postServiceEvent(
                    ServiceEvent("call incoming", arrayListOf(call.ua.uap, callp), System.nanoTime())
                )
        }
        "call missed" -> {
            val uap = intent.getLongExtra("uap", 0L)
            val ua = UserAgent.ofUap(uap)
            if (ua == null) {
                Log.w(TAG, "handleIntent did not find ua $uap")
                return
            }
            spinToAor(viewModel, ua.account.aor)
            viewModel.navigateToCalls(ua.account.aor)
        }
        "call transfer", "transfer show", "transfer accept" -> {
            val callp = intent.getLongExtra("callp", 0L)
            val call = Call.ofCallp(callp)
            if (call == null) {
                Log.w(TAG, "handleIntent '$action' did not find call $callp")
                return
            }
            val uri = if (ev[0] == "call transfer")
                ev[1]
            else
                intent.getStringExtra("uri")!!
            BaresipService.postServiceEvent(
                ServiceEvent(ev[0] + "," + uri, arrayListOf(call.ua.uap, callp), System.nanoTime())
            )
        }
        "message", "message show", "message reply" -> {
            val uap = intent.getLongExtra("uap", 0L)
            val ua = UserAgent.ofUap(uap)
            if (ua == null) {
                Log.w(TAG, "handleIntent did not find ua $uap")
                return
            }
            spinToAor(viewModel, ua.account.aor)
            val peer = intent.getStringExtra("peer") ?: ""
            Log.d(TAG, "handleIntent: message intent action=${ev[0]} for aor=${ua.account.aor} peer=$peer - navigating to chat")
            if (peer.isNotEmpty()) {
                viewModel.navigateToChat(ua.account.aor, peer)
            } else {
                viewModel.navigateToChats()
            }
        }
    }
}

fun handleDialog(ctx: Context, title: String, message: String, action: () -> Unit = {}) {
    dialogTitle.value = title
    dialogMessage.value = message
    firstText.value = ""
    secondText.value = ""
    lastText.value = ctx.getString(R.string.ok)
    onLastClicked.value = { action() }
    showDialog.value = true
}

fun callAction(ctx: Context, viewModel: ViewModel, uri: Uri?, action: String) {
    if (Call.inCall() || uas.value.isEmpty()) return
    Log.d(TAG, "Action $action to $uri")
    if (uri != null) {
        var uriStr: String
        var uap: Long
        when (uri.scheme) {
            "sip" -> {
                uriStr = Utils.uriUnescape(uri.toString())
                var ua = UserAgent.ofDomain(Utils.uriHostPart(uriStr))
                if (ua == null && uas.value.isNotEmpty())
                    ua = uas.value[0]
                if (ua == null) {
                    Log.w(TAG, "No accounts for '$uriStr'")
                    return
                }
                uap = ua.uap
            }
            "tel" -> {
                uriStr = uri.toString().replace("%2B", "+").replace("%20", "")
                    .filterNot { setOf('-', ' ', '(', ')').contains(it) }
                var account: Account? = null
                for (a in Account.accounts())
                    if (a.telProvider != "") {
                        account = a
                        break
                    }
                if (account == null) {
                    Log.w(TAG, "No telephony providers for '$uriStr'")
                    return
                }
                uap = UserAgent.ofAor(account.aor)!!.uap
            }
            else -> {
                Log.w(TAG, "Unsupported URI scheme ${uri.scheme}")
                return
            }
        }
        val intent = Intent(ctx, MainActivity::class.java)
        intent.putExtra("uap", uap)
        intent.putExtra("peer", uriStr)
        handleIntent(ctx, viewModel, intent, action)
    }
}

private fun redirect(ctx: Context, viewModel: ViewModel, ua: UserAgent, redirectUri: String) {
    if (ua.account.aor != viewModel.selectedAor.value)
        spinToAor(viewModel, ua.account.aor)
    viewModel.dialerState.callUri.value = redirectUri
    callClick(ctx, viewModel, viewModel.dialerState)
}

private fun acceptTransfer(ctx: Context, viewModel: ViewModel, ua: UserAgent, call: Call, uri: String) {
    val newCallp = ua.callAlloc(call.callp, Api.VIDMODE_OFF)
    if (newCallp != 0L) {
        Log.d(TAG, "Adding outgoing call ${ua.uap}/$newCallp/$uri")
        val newCall = Call(newCallp, ua, uri, "out", "transferring")
        newCall.add()
        if (newCall.connect(uri)) {
            if (ua.account.aor != viewModel.selectedAor.value)
                spinToAor(viewModel, ua.account.aor)
            showCall(ctx, viewModel, ua)
        }
        else {
            Log.w(TAG, "call_connect $newCallp failed")
            call.notifySipfrag(500, "Call Error")
        }
    }
    else {
        Log.w(TAG, "callAlloc for ua ${ua.uap} call ${call.callp} transfer failed")
        call.notifySipfrag(500, "Call Error")
    }
}

private fun backup(ctx: Context, password: String) {
    val files = arrayListOf("accounts", "config", "contacts", "call_history",
        "messages", "uuid", "gzrtp.zid", "cert.pem", "ca_cert", "ca_certs.crt", "blocked.json")
    File(BaresipService.filesPath).walk().forEach {
        if (it.name.endsWith(".png"))
            files.add(it.name)
    }
    val zipFile = ctx.getString(R.string.app_name) + ".zip"
    val zipFilePath = BaresipService.filesPath + "/$zipFile"
    if (!Utils.zip(files, zipFile)) {
        Log.w(TAG, "Failed to write zip file '$zipFile'")
        alertTitle.value = ctx.getString(R.string.error)
        alertMessage.value = String.format(
            ctx.getString(R.string.backup_failed),
            Utils.fileNameOfUri(ctx, downloadsOutputUri!!)
        )
        showAlert.value = true
        downloadsOutputUri = null
        return
    }
    val content = Utils.getFileContents(zipFilePath)
    if (content == null) {
        Log.w(TAG, "Failed to read zip file '$zipFile'")
        alertTitle.value = ctx.getString(R.string.error)
        alertMessage.value = String.format(
            ctx.getString(R.string.backup_failed),
            Utils.fileNameOfUri(ctx, downloadsOutputUri!!)
        )
        showAlert.value = true
        downloadsOutputUri = null
        return
    }
    if (!Utils.encryptToUri(ctx, downloadsOutputUri!!, content, password)) {
        alertTitle.value = ctx.getString(R.string.error)
        alertMessage.value = String.format(
            ctx.getString(R.string.backup_failed),
            Utils.fileNameOfUri(ctx, downloadsOutputUri!!)
        )
        showAlert.value = true
        downloadsOutputUri = null
        return
    }
    alertTitle.value = ctx.getString(R.string.info)
    alertMessage.value = String.format(
        ctx.getString(R.string.backed_up),
        Utils.fileNameOfUri(ctx, downloadsOutputUri!!)
    )
    showAlert.value = true
    Utils.deleteFile(File(zipFilePath))
    downloadsInputUri = null
}

private fun restore(ctx: Context, password: String, onRestartApp: () -> Unit) {
    val zipFile = ctx.getString(R.string.app_name) + ".zip"
    val zipFilePath = BaresipService.filesPath + "/$zipFile"
    val zipData = Utils.decryptFromUri(ctx, downloadsInputUri!!, password)
    if (zipData == null) {
        alertTitle.value = ctx.getString(R.string.error)
        alertMessage.value = String.format(
            ctx.getString(R.string.restore_failed),
            Utils.fileNameOfUri(ctx, downloadsInputUri!!)
        )
        showAlert.value = true
        downloadsInputUri = null
        return
    }
    if (!Utils.putFileContents(zipFilePath, zipData)) {
        Log.w(TAG, "Failed to write zip file '$zipFile'")
        alertTitle.value = ctx.getString(R.string.error)
        alertMessage.value = String.format(
            ctx.getString(R.string.restore_failed),
            Utils.fileNameOfUri(ctx, downloadsInputUri!!)
        )
        showAlert.value = true
        downloadsInputUri = null
        return
    }
    if (!Utils.unZip(zipFilePath)) {
        Log.w(TAG, "Failed to unzip file '$zipFile'")
        alertTitle.value = ctx.getString(R.string.error)
        alertMessage.value = String.format(
            ctx.getString(R.string.restore_unzip_failed),
            "baresip",
            BuildConfig.VERSION_NAME
        )
        showAlert.value = true
        downloadsInputUri = null
        return
    }
    Utils.deleteFile(File(zipFilePath))

    File("${BaresipService.filesPath}/recordings").walk().forEach {
        if (it.name.startsWith("dump"))
            Utils.deleteFile(it)
    }

    Utils.createEmptyFile(BaresipService.filesPath + "/restored")

    dialogTitle.value = ctx.getString(R.string.info)
    dialogMessage.value = ctx.getString(R.string.restored)
    firstText.value = ctx.getString(R.string.cancel)
    onFirstClicked.value = { showDialog.value = false }
    secondText.value = ""
    lastText.value = ctx.getString(R.string.restart)
    onLastClicked.value = {
        onRestartApp()
        showDialog.value = false
    }
    showDialog.value = true

    downloadsInputUri = null
}
