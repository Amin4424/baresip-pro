package io.github.amin4424.baresip.promax

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlin.jvm.JvmName

data class MenuItem(
    val text: String,
    val icon: ImageVector? = null
)

object CustomElements {

    val selectItems = mutableStateOf(listOf<String>())
    val selectItemAction = mutableStateOf<(Int) -> Unit>({ _ -> run {} })
    val showSelectItemDialog = mutableStateOf(false)

    @Composable
    fun Button(
        onClick: () -> Unit,
        onLongClick: () -> Unit,
        modifier: Modifier = Modifier,
        shape: Shape,
        border: BorderStroke? = null,
        color: Color,
        content: @Composable RowScope.() -> Unit
    ) {
        Surface(
            shape = shape,
            color = color,
            border = border,
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { onLongClick() },
                    )
                }
                .then(modifier),
        ) {
            Row(
                modifier = Modifier.padding(ButtonDefaults.ContentPadding),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }

    @Composable
    @JvmName("DropdownMenuString")
    fun DropdownMenu(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        items: List<String>,
        onItemClick: (String) -> Unit
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            menuItems = items.map { MenuItem(it) },
            onItemClick = onItemClick
        )
    }

    @Composable
    fun DropdownMenu(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        menuItems: List<MenuItem>,
        onItemClick: (String) -> Unit
    ) {
        val hasAnyIcon = menuItems.any { it.icon != null }
        val isDark = MaterialTheme.colorScheme.background.red < 0.2f
        
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(18.dp))
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                containerColor = if (isDark) Color(0xFF131C2E) else Color(0xFFFFFFFF),
                shadowElevation = 12.dp,
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                ),
                modifier = Modifier.clip(RoundedCornerShape(18.dp))
            ) {
                val itemsIterator = menuItems.iterator()
                while (itemsIterator.hasNext()) {
                    val menuItem = itemsIterator.next()
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = menuItem.text,
                                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = if (hasAnyIcon) {
                            {
                                if (menuItem.icon != null)
                                    Icon(
                                        imageVector = menuItem.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                else
                                    Spacer(Modifier.size(20.dp))
                            }
                        } else null,
                        onClick = { onItemClick(menuItem.text) }
                    )
                    if (itemsIterator.hasNext())
                        HorizontalDivider(
                            color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                            thickness = 0.5.dp
                        )
                }
            }
        }
    }

    @Composable
    fun TextAvatar(name: String, color: Int) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(SolidColor(Color(color)))
            }
            val text = if (name == "") "" else name[0].toString()
            Text(text, color = Color.White, fontSize = 20.sp)
        }
    }

    @Composable
    fun ImageAvatar(bitmap: Bitmap) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
    }

    @Composable
    fun Modifier.verticalScrollbar(
        state: ScrollState,
        scrollbarWidth: Dp = 4.dp,
        alwaysShow: Boolean = true,
        color: Color = MaterialTheme.colorScheme.outlineVariant
    ): Modifier {
        val alpha by animateFloatAsState(
            targetValue = if(state.isScrollInProgress || alwaysShow) 1f else 0f,
            animationSpec = tween(400, delayMillis = if(state.isScrollInProgress) 0 else 700),
            label = "scrollbarAlpha"
        )
        return this then Modifier.drawWithContent {
            drawContent()

            val viewHeight = state.viewportSize.toFloat()
            if (viewHeight <= 0f) return@drawWithContent // Safety check for zero height

            val contentHeight = state.maxValue + viewHeight
            val minHeight = 10.dp.toPx()

            // Ensure the 'max' of coerceIn is at least as large as 'min'
            val scrollbarHeight = (viewHeight * (viewHeight / contentHeight))
                .coerceIn(minHeight.coerceAtMost(viewHeight) .. viewHeight)

            val variableZone = viewHeight - scrollbarHeight

            // Prevent division by zero if maxValue is 0 (no scrolling needed)
            val scrollbarOffsetY = if (state.maxValue > 0)
                (state.value.toFloat() / state.maxValue) * variableZone
            else
                0f

            drawRoundRect(
                cornerRadius = CornerRadius(scrollbarWidth.toPx() / 2, scrollbarWidth.toPx() / 2),
                color = color,
                topLeft = Offset(this.size.width - scrollbarWidth.toPx(), scrollbarOffsetY),
                size = Size(scrollbarWidth.toPx(), scrollbarHeight),
                alpha = alpha
            )
        }
    }

    @Composable
    fun Modifier.verticalScrollbar(
        state: LazyListState,
        width: Dp = 4.dp,
        alwaysShow: Boolean = true,
        color: Color = MaterialTheme.colorScheme.outlineVariant
    ): Modifier {
        val alpha by animateFloatAsState(
            targetValue = if (state.isScrollInProgress || alwaysShow) 1f else 0f,
            animationSpec = tween(durationMillis = if (state.isScrollInProgress) 150 else 500),
            label = "lazyScrollbarAlpha"
        )
        return this.drawWithContent {
            drawContent()

            val totalItems = state.layoutInfo.totalItemsCount
            val visibleItemsInfo = state.layoutInfo.visibleItemsInfo

            // Check if there are items and if they actually exceed the viewport
            if (totalItems > 0 && visibleItemsInfo.isNotEmpty()) {
                val firstVisibleElementIndex = visibleItemsInfo.first().index
                val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

                if (needDrawScrollbar) {
                    val elementHeight = this.size.height / totalItems
                    val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
                    val scrollbarHeight = visibleItemsInfo.size * elementHeight

                    // Only draw if the scrollbar is actually smaller than the track
                    if (scrollbarHeight < this.size.height)
                        drawRoundRect(
                            cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2),
                            color = color,
                            topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                            size = Size(width.toPx(), scrollbarHeight),
                            alpha = alpha
                        )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AlertDialog(
        showDialog: MutableState<Boolean>,
        title: String,
        message: String,
        firstButtonText: String = "",
        onFirstClicked: () -> Unit = {},
        secondButtonText: String = "",
        onSecondClicked: () -> Unit = {},
        thirdButtonText: String = "",
        onThirdClicked: () -> Unit = {},
        fourthButtonText: String = "",
        onFourthClicked: () -> Unit = {},
        lastButtonText: String = "",
        onLastClicked: () -> Unit = {}
    ) {
        if (showDialog.value) {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            BasicAlertDialog(
                onDismissRequest = { showDialog.value = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                content = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(if (isLandscape) 0.9f else 0.95f)
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = title,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .verticalScrollbar(scrollState)
                                    .verticalScroll(scrollState)
                            ) {
                                Text(
                                    text = message,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            val buttons = listOf(
                                firstButtonText, secondButtonText, thirdButtonText,
                                fourthButtonText, lastButtonText
                            )
                            val buttonCount = buttons.count { it.isNotEmpty() }

                            if (buttonCount > 0) {

                                if (buttonCount >= 3) {
                                    // Use a Column for 3-4 buttons, aligned to the end (right)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        if (firstButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onFirstClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = firstButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        if (secondButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onSecondClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = secondButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        if (thirdButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onThirdClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = thirdButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        if (fourthButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onFourthClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = fourthButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                )
                                            }
                                        if (lastButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onLastClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = lastButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                    }
                                } else {
                                    // Use the existing Row for 1 or 2 buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        if (firstButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onFirstClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = firstButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        if (secondButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onSecondClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = secondButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        if (thirdButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onThirdClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = thirdButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        if (fourthButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onFourthClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = fourthButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        if (lastButtonText.isNotEmpty())
                                            TextButton(onClick = {
                                                onLastClicked()
                                                showDialog.value = false
                                            }) {
                                                Text(
                                                    text = lastButtonText.uppercase(),
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SelectableAlertDialog(
        openDialog: MutableState<Boolean>,
        title: String,
        items: List<String>,
        onItemClicked: (Int) -> Unit,
        neutralButtonText: String = "",
        onNeutralClicked: () -> Unit = {}
    ) {
        if (openDialog.value) {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            BasicAlertDialog(
                onDismissRequest = {
                    openDialog.value = false
                },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                content = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(if (isLandscape) 0.9f else 0.95f)
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = title,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                itemsIndexed(items) { index, item ->
                                    TextButton(
                                        onClick = {
                                            onItemClicked(index)
                                            openDialog.value = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = stringResource(R.string.bullet_item, item),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                            if (neutralButtonText.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            onNeutralClicked()
                                            openDialog.value = false
                                        }
                                    ) {
                                        Text(
                                            text = neutralButtonText.uppercase(),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PasswordDialog(
        ctx: Context,
        showPasswordDialog: MutableState<Boolean>,
        password: MutableState<String>,
        emptyOk: Boolean = false,
        keyboardController: SoftwareKeyboardController?,
        title: String,
        message: String = "",
        okAction: () -> Unit,
        cancelAction: () -> Unit
    ) {
        val showPassword = remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }

        if (showPasswordDialog.value) {
            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            BasicAlertDialog(
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                ),
                onDismissRequest = {
                    keyboardController?.hide()
                    showPasswordDialog.value = false
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(if (isLandscape) 0.9f else 0.95f)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 0.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = title,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (message.isNotEmpty())
                            Text(
                                text = message,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        OutlinedTextField(
                            value = password.value,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = MaterialTheme.colorScheme.primary,
                            ),
                            onValueChange = {
                                password.value = it
                            },
                            visualTransformation = if (showPassword.value)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    showPassword.value = !showPassword.value
                                }) {
                                    Icon(
                                        imageVector = if (showPassword.value)
                                            Icons.Filled.Visibility
                                        else
                                            Icons.Filled.VisibilityOff,
                                        contentDescription = "Visibility",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 2.dp)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        LaunchedEffect(key1 = Unit) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    keyboardController?.hide()
                                    showPasswordDialog.value = false
                                    cancelAction()
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.cancel),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    keyboardController?.hide()
                                    showPasswordDialog.value = false
                                    password.value = password.value.trim()
                                    if (!(emptyOk && password.value.isEmpty()) && !Account.checkAuthPass(password.value)) {
                                        Toast.makeText(
                                            ctx,
                                            String.format(
                                                ctx.getString(R.string.invalid_authentication_password),
                                                password.value
                                            ),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        password.value = ""
                                    }
                                    okAction()
                                }
                            ) {
                                Text(
                                    text = stringResource(R.string.ok),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun EmptyStateBanner(
        icon: ImageVector,
        title: String,
        message: String,
        actionLabel: String? = null,
        onActionClick: (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                if (actionLabel != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Button(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = actionLabel, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    @Composable
    fun NoAccountView(
        title: String,
        message: String,
        onAddAccount: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
        val primaryCyan = Color(0xFF00B0FF)
        val accentBlue = Color(0xFF0080FF)

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDark) 16.dp else 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = primaryCyan.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131B2E).copy(alpha = 0.95f) else Color.White
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Glowing Icon Badge
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(12.dp, CircleShape, spotColor = primaryCyan.copy(alpha = 0.5f))
                            .background(
                                Brush.linearGradient(
                                    listOf(primaryCyan.copy(alpha = 0.25f), accentBlue.copy(alpha = 0.10f))
                                ),
                                CircleShape
                            )
                            .border(
                                1.5.dp,
                                Brush.verticalGradient(
                                    listOf(primaryCyan.copy(alpha = 0.6f), primaryCyan.copy(alpha = 0.15f))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = primaryCyan,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Modern Gradient Add Account Button
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = primaryCyan.copy(alpha = 0.5f))
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onAddAccount),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(primaryCyan, accentBlue)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add Account",
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
    fun topBarGradient(): Brush {
        val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
        return if (isDark) {
            Brush.verticalGradient(
                listOf(Color(0xFF0F172A), Color(0xFF1E293B))
            )
        } else {
            Brush.verticalGradient(
                listOf(Color(0xFF0284C7), Color(0xFF0369A1))
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ModernTopAppBar(
        title: String,
        subtitle: String? = null,
        badge: Int? = null,
        onBack: (() -> Unit)? = null,
        navigationIcon: (@Composable () -> Unit)? = null,
        actions: @Composable (RowScope.() -> Unit) = {}
    ) {
        val topBarGradient = topBarGradient()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(topBarGradient)
                .statusBarsPadding()
        ) {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            if (badge != null && badge > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = badge.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (!subtitle.isNullOrEmpty()) {
                            Text(
                                text = subtitle,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (navigationIcon != null) {
                        navigationIcon()
                    } else if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ModernTopAppBar(
        navigationIcon: (@Composable () -> Unit)? = null,
        onBack: (() -> Unit)? = null,
        actions: @Composable (RowScope.() -> Unit) = {},
        title: @Composable () -> Unit
    ) {
        val topBarGradient = topBarGradient()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(topBarGradient)
                .statusBarsPadding()
        ) {
            TopAppBar(
                title = title,
                navigationIcon = {
                    if (navigationIcon != null) {
                        navigationIcon()
                    } else if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    }
}
