package com.tutpro.baresip.plus

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.tutpro.baresip.plus.CustomElements.AlertDialog
import com.tutpro.baresip.plus.CustomElements.verticalScrollbar

fun NavGraphBuilder.accountsScreenRoute(navController: NavController) {
    composable("accounts") { AccountsScreen(navController) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomElements.ModernTopAppBar(
                title = stringResource(R.string.accounts),
                badge = BaresipService.uas.value.size,
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = { NewAccount(navController) },
        content = { contentPadding -> AccountsContent(contentPadding, navController) },
    )
}

@Composable
fun AccountsContent(contentPadding: PaddingValues, navController: NavController) {

    val showDialog = remember { mutableStateOf(false) }
    val message = remember { mutableStateOf("") }
    val lastAction = remember { mutableStateOf({}) }

    AlertDialog(
        showDialog = showDialog,
        title = stringResource(R.string.confirmation),
        message = message.value,
        firstButtonText = stringResource(R.string.cancel),
        lastButtonText = stringResource(R.string.delete),
        onLastClicked = lastAction.value,
    )

    val showAccounts = remember { mutableStateOf(true) }

    if (showAccounts.value && BaresipService.uas.value.isNotEmpty()) {

        val lazyListState = rememberLazyListState()

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .padding(top = 10.dp, bottom = 8.dp)
                .verticalScrollbar(state = lazyListState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(BaresipService.uas.value) { ua ->
                val account = ua.account
                val aor = account.aor
                val text = account.text()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("account/$aor/old") }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ManageAccounts,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = text,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = aor.substringAfter(":"),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val deleteAccountMessage = stringResource(R.string.delete_account)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f))
                                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), CircleShape)
                                .clickable {
                                    message.value = String.format(deleteAccountMessage, text)
                                    lastAction.value = {
                                        CallHistoryNew.clear(aor)
                                        Message.clearMessagesOfAor(aor)
                                        Blocked.clear(aor)
                                        BlockRule.clear(aor)
                                        ua.remove()
                                        Api.ua_destroy(ua.uap)
                                        Account.saveAccounts()
                                        showAccounts.value = false
                                        showAccounts.value = true
                                    }
                                    showDialog.value = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            CustomElements.EmptyStateBanner(
                icon = Icons.Outlined.ManageAccounts,
                title = stringResource(R.string.no_account_found),
                message = "Add a new SIP account below to start making and receiving calls."
            )
        }
    }
}

@Composable
fun NewAccount(navController: NavController) {

    val alertTitle = remember { mutableStateOf("") }
    val alertMessage = remember { mutableStateOf("") }
    val showAlert = remember { mutableStateOf(false) }

    if (showAlert.value)
        AlertDialog(
            showDialog = showAlert,
            title = alertTitle.value,
            message = alertMessage.value,
            lastButtonText = stringResource(R.string.ok),
        )

    fun createNew(ctx: Context, newAor: String): Account? {

        val aor = if (newAor.startsWith("sip:"))
            newAor
        else
            "sip:$newAor"

        if (!Utils.checkAor(aor)) {
            alertTitle.value = ctx.getString(R.string.notice)
            alertMessage.value =
                String.format(ctx.getString(R.string.invalid_aor), aor.split(":")[1])
            showAlert.value = true
            return null
        }

        if (Account.ofAor(aor) != null) {
            alertTitle.value = ctx.getString(R.string.notice)
            alertMessage.value =
                String.format(ctx.getString(R.string.account_exists), aor.split(":")[1])
            showAlert.value = true
            return null
        }

        val uap = UserAgent.uaAlloc(
            "<$aor>;stunserver=\"stun:stun.l.google.com:19302\";regq=0.5;pubint=0;regint=$REGISTRATION_INTERVAL;check_origin=no;mwi=no"
        )
        if (uap == 0L) {
            alertTitle.value = ctx.getString(R.string.notice)
            alertMessage.value = ctx.getString(R.string.account_allocation_failure)
            showAlert.value = true
            return null
        }

        val ua = UserAgent.ofUap(uap)
        if (ua == null) {
            alertTitle.value = ctx.getString(R.string.notice)
            alertMessage.value = ctx.getString(R.string.account_allocation_failure)
            showAlert.value = true
            return null
        }

        val acc = ua.account
        acc.checkOrigin = true
        Log.d(TAG, "Allocated UA $uap with SIP URI ${acc.luri}")
        Account.saveAccounts()
        return acc
    } // createNew

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    var newAor by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF),
        border = BorderStroke(
            0.5.dp,
            if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f)
        ),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 12.dp, top = 10.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val ctx = LocalContext.current
            val newAccountTitle = stringResource(R.string.new_account)
            val accountsHelp = stringResource(R.string.accounts_help)
            OutlinedTextField(
                value = newAor,
                placeholder = { Text(text = "user@domain.com") },
                onValueChange = { newAor = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                singleLine = true,
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                    focusedContainerColor = if (isDark) Color(0xFF161C26) else Color(0xFFF1F4F8),
                    unfocusedContainerColor = if (isDark) Color(0xFF141822) else Color(0xFFF6F8FA)
                ),
                trailingIcon = {
                    if (newAor.isNotEmpty())
                        Icon(
                            Icons.Outlined.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.clickable { newAor = "" },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                },
                label = { Text(stringResource(R.string.new_account)) },
                textStyle = TextStyle(fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (newAor.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    )
                    .clickable {
                        val account = createNew(ctx, newAor.trim())
                        if (account != null) {
                            navController.navigate("account/${account.aor}/new")
                            newAor = ""
                            focusManager.clearFocus()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    modifier = Modifier.size(26.dp),
                    tint = Color.White,
                    contentDescription = stringResource(R.string.add)
                )
            }
        }
    }
}

