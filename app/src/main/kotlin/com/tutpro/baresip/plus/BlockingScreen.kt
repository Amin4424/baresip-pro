package com.tutpro.baresip.plus

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tutpro.baresip.plus.CustomElements.AlertDialog
import com.tutpro.baresip.plus.CustomElements.verticalScrollbar

fun NavGraphBuilder.blockingScreenRoute(navController: NavController) {
    composable(
        route = "blocking/{aor}",
        arguments = listOf(navArgument("aor") { type = NavType.StringType })
    ) { backStackEntry ->
        val aor = backStackEntry.arguments?.getString("aor")!!
        val viewModel = viewModel<AccountViewModel>()
        val ua = UserAgent.ofAor(aor)!!
        BlockingScreen(navController, viewModel, ua)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockingScreen(navController: NavController, viewModel: AccountViewModel, ua: UserAgent) {
    val acc = ua.account
    var rules by remember {
        mutableStateOf(BaresipService.blockRules.filter { it.aor == acc.aor || it.aor == "" })
    }

    remember {
        viewModel.loadAccount(acc)
        true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomElements.ModernTopAppBar(
                title = stringResource(R.string.blocking),
                badge = rules.size,
                onBack = { navController.navigateUp() }
            )
        },
        bottomBar = { NewRule(acc.aor, onRuleAdded = { rules = BaresipService.blockRules.filter { it.aor == acc.aor || it.aor == "" } }) },
        content = { contentPadding ->
            BlockingContent(
                contentPadding,
                viewModel,
                rules,
                acc,
                onRuleDeleted = { rules = BaresipService.blockRules.filter { it.aor == acc.aor || it.aor == "" } }
            )
        },
    )
}

@Composable
fun BlockingContent(
    contentPadding: PaddingValues,
    viewModel: AccountViewModel,
    rules: List<BlockRule>,
    acc: Account,
    onRuleDeleted: () -> Unit
) {
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

    @Composable
    fun BlockUnknown() {
        val blockUnknownTitle = stringResource(R.string.block_unknown)
        val blockUnknownHelp = stringResource(R.string.block_unknown_help)
        val block by viewModel.blockUnknown.collectAsState()
        Row(
            Modifier.fillMaxWidth().padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = blockUnknownTitle,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        alertTitle.value = blockUnknownTitle
                        alertMessage.value = blockUnknownHelp
                        showAlert.value = true
                    },
                fontSize = 18.sp
            )
            Switch(
                checked = block,
                onCheckedChange = { 
                    viewModel.blockUnknown.value = it
                    acc.blockUnknown = it
                    Account.saveAccounts()
                }
            )
        }
    }

    @Composable
    fun BlockHidden(acc: Account) {
        val blockHiddenTitle = stringResource(R.string.block_hidden)
        val blockHiddenHelp = stringResource(R.string.block_hidden_help)
        val block by viewModel.blockHidden.collectAsState()
        Row(
            Modifier.fillMaxWidth().padding(end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = blockHiddenTitle,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        alertTitle.value = blockHiddenTitle
                        alertMessage.value = blockHiddenHelp
                        showAlert.value = true
                    },
                fontSize = 18.sp
            )
            Switch(
                checked = block,
                onCheckedChange = { 
                    viewModel.blockHidden.value = it 
                    acc.blockHidden = it
                    Account.saveAccounts()
                }
            )
        }
    }

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BlockUnknown()
                BlockHidden(acc)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.blocking_rules),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.primary
        )

        if (rules.isEmpty()) {
            CustomElements.EmptyStateBanner(
                icon = Icons.Filled.Security,
                title = "No Custom Rules",
                message = "Add wildcard patterns below (e.g. +1800*) to automatically block callers."
            )
        } else {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp)
                    .verticalScrollbar(state = lazyListState),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(rules) { rule ->
                    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF131C2E) else Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FilterAlt,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = rule.pattern,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            val deleteRuleMessage = stringResource(R.string.blocking_delete_alert)
                            IconButton(
                                onClick = {
                                    message.value = String.format(
                                        deleteRuleMessage,
                                        rule.pattern
                                    )
                                    lastAction.value = {
                                        BaresipService.blockRules.remove(rule)
                                        BlockRule.save()
                                        onRuleDeleted()
                                    }
                                    showDialog.value = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
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
fun NewRule(aor: String, onRuleAdded: () -> Unit) {
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

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    var pattern by remember { mutableStateOf("") }
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
            OutlinedTextField(
                value = pattern,
                placeholder = { Text(text = stringResource(R.string.new_blocking_rule)) },
                onValueChange = { pattern = it },
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
                    if (pattern.isNotEmpty()) {
                        Icon(
                            Icons.Outlined.Clear,
                            contentDescription = "Clear",
                            modifier = Modifier.clickable { pattern = "" },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                label = { Text(stringResource(R.string.new_blocking_rule)) },
                textStyle = TextStyle(fontSize = 16.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (pattern.trim().isNotEmpty())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    )
                    .clickable {
                        if (pattern.trim().isNotEmpty()) {
                            if (!BlockRule.exists(aor, pattern.trim())) {
                                BaresipService.blockRules.add(BlockRule(aor, pattern.trim()))
                                BlockRule.save()
                                onRuleAdded()
                            }
                            pattern = ""
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
