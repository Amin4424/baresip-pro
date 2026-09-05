package io.github.amin4424.baresip.promax

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.amin4424.baresip.promax.CustomElements.verticalScrollbar

fun NavGraphBuilder.codecsScreenRoute(navController: NavController) {
    composable(
        route = "codecs/{aor}/{media}",
        arguments = listOf(
            navArgument("aor") { type = NavType.StringType },
            navArgument("media") { type = NavType.StringType })
    ) { backStackEntry ->
        val aor = backStackEntry.arguments?.getString("aor")!!
        val media = backStackEntry.arguments?.getString("media")!!
        val account = UserAgent.ofAor(aor)?.account!!
        CodecsScreen(
            onBack = { navController.navigateUp() },
            checkOnClick = { updatedCodecs ->
                val enabledCodecNames = updatedCodecs.filter { it.enabled.value }.map { it.name }
                val codecList = Utils.implode(enabledCodecNames, ",")
                Log.d(TAG, "Saving codecs for ${account.aor} (${media}): $codecList")
                val success = if (media == "audio")
                    Api.account_set_audio_codecs(account.accp, codecList)
                else
                    Api.account_set_video_codecs(account.accp, codecList)
                if (success == 0) {
                    if (media == "audio")
                        account.audioCodec = ArrayList(enabledCodecNames)
                    else
                        account.videoCodec = ArrayList(enabledCodecNames)
                    Account.saveAccounts()
                    Log.d("CodecsSave", "Codecs saved successfully.")
                }
                else
                    Log.e(TAG, "Failed to set $aor codecs.")
                navController.navigateUp()
            },
            aor = aor,
            media = media
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodecsScreen(
    onBack: () -> Unit,
    checkOnClick: (List<Codec>) -> Unit,
    aor: String,
    media: String
) {
    val ua = UserAgent.ofAor(aor)!!
    val acc = ua.account
    val codecs = remember { mutableStateListOf<Codec>() }

    LaunchedEffect(acc, media) {
        val allCodecs: List<String> = if (media == "audio")
            Api.audio_codecs().split(",")
        else
            Api.video_codecs().split(",").distinct()
        val accCodecs: List<String> = if (media == "audio")
            acc.audioCodec
        else
            acc.videoCodec
        val currentCodecs = mutableListOf<Codec>()
        for (codec in accCodecs)
            currentCodecs.add(Codec(codec, mutableStateOf(true)))
        for (codec in allCodecs)
            if (codec !in accCodecs)
                currentCodecs.add(Codec(codec, mutableStateOf(false)))
        codecs.clear()
        codecs.addAll(currentCodecs)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomElements.ModernTopAppBar(
                title = if (media == "audio")
                    stringResource(R.string.audio_codecs)
                else
                    stringResource(R.string.video_codecs),
                onBack = onBack,
                actions = {
                    IconButton(
                        onClick = { checkOnClick(codecs) }
                    ) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = "Check", tint = Color.White)
                    }
                }
            )
        },
        content = { contentPadding ->
            CodecsContent(contentPadding, codecs)
        },
    )
}

@Composable
private fun CodecsContent(
    contentPadding: PaddingValues,
    codecs: SnapshotStateList<Codec>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(bottom = 16.dp),
    ) {
        Codecs(codecs = codecs)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Codecs(codecs: SnapshotStateList<Codec>) {

    val draggableState = rememberDraggableListState(
        onMove = { fromIndex, toIndex ->
            codecs.add(toIndex, codecs.removeAt(fromIndex))
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .verticalScrollbar(state = draggableState.listState),
        state = draggableState.listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        draggableItems(
            state = draggableState,
            items = codecs,
            key = { item -> item.name }
        ) { item, isDragging ->
            val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDragging)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else if (isDark)
                        Color(0xFF131C2E)
                    else
                        Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 1.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.dragHandle(state = draggableState, key = item.name),
                        imageVector = Icons.Filled.Reorder,
                        contentDescription = "Reorder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.name,
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (item.enabled.value) 1.0f else 0.5f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = item.enabled.value,
                        onCheckedChange = { isChecked ->
                            item.enabled.value = isChecked
                            val index = codecs.indexOf(item)
                            codecs.removeAt(index)
                            if (isChecked) codecs.add(0, item) else codecs.add(item)
                        }
                    )
                }
            }
        }
    }
}
