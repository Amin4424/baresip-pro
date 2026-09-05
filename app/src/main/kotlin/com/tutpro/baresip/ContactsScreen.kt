package com.tutpro.baresip

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.graphics.scale
import android.provider.ContactsContract
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.AsyncImage
import com.tutpro.baresip.BaresipService.Companion.uas
import com.tutpro.baresip.CustomElements.AlertDialog
import com.tutpro.baresip.CustomElements.TextAvatar
import com.tutpro.baresip.CustomElements.verticalScrollbar
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val avatarSize: Int = 96

fun NavGraphBuilder.contactsScreenRoute(navController: NavController, viewModel: ViewModel) {
    composable("contacts") { _ ->
        ContactsScreen(navController = navController, viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsScreen(navController: NavController, viewModel: ViewModel) {

    val ctx = LocalContext.current
    val activity = LocalActivity.current!!
    var searchContactName by remember { mutableStateOf("") }

    val consentRequest = stringResource(R.string.consent_request)
    val contactsConsent = stringResource(R.string.contacts_consent)
    val deny = stringResource(R.string.deny)
    val accept = stringResource(R.string.accept)
    val notice = stringResource(R.string.notice)
    val noAndroidContacts = stringResource(R.string.no_android_contacts)
    val ok = stringResource(R.string.ok)

    var expanded by remember { mutableStateOf(false) }
    val showModeDialog = remember { mutableStateOf(false) }
    val both = stringResource(R.string.both)
    val import = stringResource(R.string.import_contacts)
    val export = stringResource(R.string.export_contacts)
    val delete = stringResource(R.string.delete)
    val confirmation = stringResource(R.string.confirmation)
    val cancel = stringResource(R.string.cancel)
    val contactsDeleteQuestion = stringResource(R.string.contacts_delete_question)
    val contactDeleteQuestion = stringResource(R.string.contact_delete_question)

    val vcfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/vcard")
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                ctx.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = outputStream.bufferedWriter()
                    for (contact in BaresipService.baresipContacts.value) {
                        writer.write("BEGIN:VCARD\n")
                        writer.write("VERSION:3.0\n")
                        writer.write("FN:${contact.name}\n")
                        val nameParts = contact.name.split(" ", limit = 2)
                        if (nameParts.size == 2)
                            writer.write("N:${nameParts[1]};${nameParts[0]};;;\n")
                        else
                            writer.write("N:;${contact.name};;;\n")
                        if (contact.email.isNotEmpty())
                            writer.write("EMAIL:${contact.email}\n")
                        if (contact.avatarImage != null) {
                            val outputStreamPhoto = ByteArrayOutputStream()
                            contact.avatarImage!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStreamPhoto)
                            val base64Image = Base64.encodeToString(outputStreamPhoto.toByteArray(), Base64.NO_WRAP)
                            writer.write("PHOTO;ENCODING=BASE64;JPEG:$base64Image\n")
                        }
                        for (u in contact.uris) {
                            if (u.uri.startsWith("tel:")) {
                                if (u.label.isNotEmpty())
                                    writer.write("TEL;X-${u.label}:${u.uri.substring(4)}\n")
                                else
                                    writer.write("TEL:${u.uri.substring(4)}\n")
                            } else if (u.uri.startsWith("sip:")) {
                                if (u.label.isNotEmpty())
                                    writer.write("X-SIP;X-${u.label}:${u.uri.substring(4)}\n")
                                else
                                    writer.write("X-SIP:${u.uri.substring(4)}\n")
                            }
                        }
                        writer.write("END:VCARD\n")
                    }
                    writer.flush()
                }
                Toast.makeText(ctx, R.string.contact_export_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to export VCF: ${e.message}")
                Toast.makeText(ctx, R.string.contact_export_failure, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val vcfImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                ctx.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = inputStream.bufferedReader()
                    val lines = mutableListOf<String>()
                    reader.forEachLine { line ->
                        if (line.startsWith(" ") || line.startsWith("\t")) {
                            if (lines.isNotEmpty()) {
                                lines[lines.size - 1] = lines.last() + line.trim()
                            }
                        } else {
                            lines.add(line)
                        }
                    }

                    var name = ""
                    var email = ""
                    val uris = mutableListOf<Contact.ContactUri>()
                    var photoBase64 = ""
                    var contactNo = 0
                    val newBaresipContacts = BaresipService.baresipContacts.value.toMutableList()

                    for (line in lines) {
                        when {
                            line.startsWith("BEGIN:VCARD", ignoreCase = true) -> {
                                name = ""
                                email = ""
                                uris.clear()
                                photoBase64 = ""
                            }
                            line.startsWith("FN:", ignoreCase = true) -> {
                                name = line.substring(3).trim()
                            }
                            line.startsWith("N:", ignoreCase = true) && name.isEmpty() -> {
                                name = line.substring(2).trim().replace(";", " ").trim()
                            }
                            line.startsWith("EMAIL", ignoreCase = true) -> {
                                if (email.isEmpty())
                                    email = line.substringAfter(":").trim()
                            }
                            line.startsWith("TEL", ignoreCase = true) -> {
                                val label = if (line.contains("X-")) line.substringAfter("X-").substringBefore(":") else ""
                                val value = line.substringAfter(":").trim()
                                val cleanValue = value.filterNot { setOf('-', ' ', '(', ')').contains(it) }
                                if (cleanValue.isNotEmpty()) {
                                    val telUri = if (cleanValue.startsWith("tel:")) cleanValue else "tel:$cleanValue"
                                    if (uris.none { it.uri == telUri }) uris.add(Contact.ContactUri(telUri, label))
                                }
                            }
                            line.startsWith("X-SIP", ignoreCase = true) -> {
                                val label = if (line.contains("X-")) line.substringAfter("X-").substringBefore(":") else ""
                                val sipUri = line.substringAfter(":").trim()
                                if (sipUri.isNotEmpty()) {
                                    val fullSipUri = if (sipUri.startsWith("sip:")) sipUri else "sip:$sipUri"
                                    if (uris.none { it.uri == fullSipUri }) uris.add(Contact.ContactUri(fullSipUri, label))
                                }
                            }
                            line.startsWith("PHOTO", ignoreCase = true) && line.contains("BASE64", ignoreCase = true) -> {
                                photoBase64 = line.substringAfter(":").trim()
                            }
                            line.startsWith("END:VCARD", ignoreCase = true) -> {
                                if (name.isNotEmpty()) {
                                    val existingContact = newBaresipContacts.find { it.name == name }
                                    val contactId = existingContact?.id ?: (System.currentTimeMillis() + contactNo++)
                                    if (photoBase64.isNotEmpty()) {
                                        try {
                                            val decodedString = Base64.decode(photoBase64, Base64.DEFAULT)
                                            val decodedByte = Utils.decodeSampledBitmapFromByteArray(decodedString, 192, 192)
                                            if (decodedByte != null) {
                                                val scaledByte = decodedByte.scale(192, 192)
                                                val avatarFile = File(BaresipService.filesPath, "$contactId.png")
                                                try {
                                                    val out = FileOutputStream(avatarFile)
                                                    scaledByte.compress(Bitmap.CompressFormat.PNG, 100, out)
                                                    out.flush()
                                                    out.close()
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Failed to save photo for $name: ${e.message}")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Failed to decode photo for $name: ${e.message}")
                                        }
                                    }
                                    if (existingContact != null) {
                                        for (u in uris) {
                                            if (existingContact.uris.none { it.uri == u.uri }) {
                                                existingContact.uris.add(u)
                                            }
                                        }
                                        if (existingContact.email.isEmpty() && email.isNotEmpty()) {
                                            existingContact.email = email
                                        }
                                    } else {
                                        newBaresipContacts.add(
                                            Contact.BaresipContact(
                                                name,
                                                ArrayList(uris),
                                                email,
                                                Utils.randomColor(),
                                                contactId,
                                                false
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    BaresipService.baresipContacts.value = newBaresipContacts.toList()
                    Contact.saveBaresipContacts()
                    Contact.restoreBaresipContacts()
                    Contact.contactsUpdate()
                    Toast.makeText(
                        ctx,
                        R.string.contact_import_success,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to import VCF: ${e.message}")
                Toast.makeText(
                    ctx,
                    R.string.contact_import_failure,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val call = stringResource(R.string.call)
    val showCall = stringResource(R.string.show_call)

    val contactActionName = remember(BaresipService.contactAction) {
        listOf(if (BaresipService.contactAction == "call") call else showCall)
    }

    val contactModeNames = remember(both) { listOf("baresip", "Android", both) }
    val contactModeValues = listOf("baresip", "android", "both")
    val currentContactModeName = contactModeNames[contactModeValues.indexOf(BaresipService.contactsMode)]

    val showDialog = remember { mutableStateOf(false) }
    val showNoticeDialog = remember { mutableStateOf(false) }
    val title = remember { mutableStateOf("") }
    val message = remember { mutableStateOf("") }
    val firstButtonText = remember { mutableStateOf("") }
    val onFirstClicked = remember { mutableStateOf({}) }
    val lastButtonText = remember { mutableStateOf("") }
    val onLastClicked = remember { mutableStateOf({}) }
    var pendingMode by remember { mutableStateOf("") }

    AlertDialog(
        showDialog = showDialog,
        title = title.value,
        message = message.value,
        firstButtonText = firstButtonText.value,
        onFirstClicked = onFirstClicked.value,
        lastButtonText = lastButtonText.value,
        onLastClicked = onLastClicked.value,
    )

    fun setContactsMode(mode: String) {
        if (Config.variable("contacts_mode").lowercase() != mode) {
            Config.replaceVariable("contacts_mode", mode)
            BaresipService.contactsMode = mode
            val baresipService = Intent(ctx, BaresipService::class.java)
            when (mode) {
                "baresip" -> {
                    BaresipService.androidContacts.value = listOf()
                    Contact.restoreBaresipContacts()
                    baresipService.action = "Stop Content Observer"
                }
                "android" -> {
                    BaresipService.baresipContacts.value = mutableListOf()
                    Contact.loadAndroidContacts(ctx)
                    baresipService.action = "Start Content Observer"
                }
                "both" -> {
                    Contact.restoreBaresipContacts()
                    Contact.loadAndroidContacts(ctx)
                    baresipService.action = "Start Content Observer"
                }
            }
            Contact.contactsUpdate()
            Config.save()
            ContextCompat.startForegroundService(ctx, baresipService)
        }
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_CONTACTS] == true &&
            permissions[Manifest.permission.WRITE_CONTACTS] == true) {
            if (pendingMode.isNotEmpty()) {
                setContactsMode(pendingMode)
            }
        }
        pendingMode = ""
    }

    AlertDialog(
        showDialog = showNoticeDialog,
        title = notice,
        message = noAndroidContacts,
        lastButtonText = ok,
        onLastClicked = {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
                )
            )
        }
    )

    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value

    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
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
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.contacts),
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
                                    text = "${BaresipService.contacts.size}",
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
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.navigateUp() }
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
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        val contactMenuItems = listOf(
                            MenuItem(currentContactModeName, Icons.Outlined.People),
                            MenuItem(contactActionName.first(), Icons.Outlined.Call),
                            MenuItem(import, Icons.Outlined.FileDownload),
                            MenuItem(export, Icons.Outlined.FileUpload),
                            MenuItem(delete, Icons.Outlined.Delete)
                        )
                        CustomElements.DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            menuItems = contactMenuItems,
                            onItemClick = { name ->
                                expanded = false
                                if (name == currentContactModeName) {
                                    showModeDialog.value = true
                                    return@DropdownMenu
                                }
                                if (name == call || name == showCall) {
                                    val newAction = if (BaresipService.contactAction == "call") "dial" else "call"
                                    BaresipService.contactAction = newAction
                                    Config.replaceVariable("contact_action", newAction)
                                    Config.save()
                                    return@DropdownMenu
                                }
                                if (name == import) {
                                    vcfImportLauncher.launch(arrayOf("text/vcard", "text/x-vcard"))
                                    return@DropdownMenu
                                }
                                if (name == export) {
                                    val fileName = "contacts_" +
                                            SimpleDateFormat(
                                                "yyyy_MM_dd_HH_mm",
                                                Locale.getDefault()
                                            ).format(Date()) + ".vcf"
                                    vcfExportLauncher.launch(fileName)
                                    return@DropdownMenu
                                }
                                if (name == delete) {
                                    title.value = confirmation
                                    message.value = contactsDeleteQuestion
                                    firstButtonText.value = cancel
                                    onFirstClicked.value = { }
                                    lastButtonText.value = delete
                                    onLastClicked.value = {
                                        for (contact in BaresipService.baresipContacts.value) {
                                            val avatarFile = File(BaresipService.filesPath, "${contact.id}.png")
                                            if (avatarFile.exists()) avatarFile.delete()
                                        }
                                        BaresipService.baresipContacts.value = mutableListOf()
                                        Contact.saveBaresipContacts()
                                        Contact.contactsUpdate()
                                    }
                                    showDialog.value = true
                                    return@DropdownMenu
                                }
                            }
                        )
                        CustomElements.SelectableAlertDialog(
                            openDialog = showModeDialog,
                            title = stringResource(R.string.contacts),
                            items = contactModeNames,
                            onItemClicked = { index ->
                                val mode = contactModeValues[index]
                                val contactsPermissions = arrayOf(
                                    Manifest.permission.READ_CONTACTS,
                                    Manifest.permission.WRITE_CONTACTS
                                )
                                if (mode != "baresip" && !Utils.checkPermissions(ctx, contactsPermissions)) {
                                    title.value = consentRequest
                                    message.value = contactsConsent
                                    firstButtonText.value = deny
                                    onFirstClicked.value = { }
                                    lastButtonText.value = accept
                                    onLastClicked.value = {
                                        showDialog.value = false
                                        if (ContextCompat.checkSelfPermission(
                                                ctx,
                                                Manifest.permission.READ_CONTACTS
                                            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                                ctx,
                                                Manifest.permission.WRITE_CONTACTS
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            Log.d(TAG, "Contacts permissions already granted")
                                            setContactsMode(mode)
                                        } else {
                                            if (shouldShowRequestPermissionRationale(
                                                    activity, Manifest.permission.READ_CONTACTS
                                                ) ||
                                                shouldShowRequestPermissionRationale(
                                                    activity, Manifest.permission.WRITE_CONTACTS
                                                )
                                            ) {
                                                pendingMode = mode
                                                showNoticeDialog.value = true
                                            } else {
                                                pendingMode = mode
                                                requestPermissionsLauncher.launch(contactsPermissions)
                                            }
                                        }
                                    }
                                    showDialog.value = true
                                } else {
                                    setContactsMode(mode)
                                }
                            }
                        )
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                )
            }
        },
        content = { contentPadding ->
            ContactsContent(
                ctx,
                navController,
                viewModel,
                contentPadding,
                searchContactName,
                { searchContactName = it },
                showDialog,
                title,
                message,
                firstButtonText,
                onFirstClicked,
                lastButtonText,
                onLastClicked,
                confirmation,
                cancel,
                delete,
                contactDeleteQuestion
            )
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContactsContent(
    ctx: Context,
    navController: NavController,
    viewModel: ViewModel,
    contentPadding: PaddingValues,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showDialog: androidx.compose.runtime.MutableState<Boolean>,
    title: androidx.compose.runtime.MutableState<String>,
    message: androidx.compose.runtime.MutableState<String>,
    firstButtonText: androidx.compose.runtime.MutableState<String>,
    onFirstClicked: androidx.compose.runtime.MutableState<() -> Unit>,
    lastButtonText: androidx.compose.runtime.MutableState<String>,
    onLastClicked: androidx.compose.runtime.MutableState<() -> Unit>,
    confirmationText: String,
    cancelText: String,
    deleteText: String,
    contactDeleteQuestion: String
) {
    val isDark = isSystemInDarkTheme() || BaresipService.darkTheme.value
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }

    val scrollToContact = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("scrollToContact")
        ?.observeAsState()

    var lastSearchQuery by remember { mutableStateOf("") }
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank() && lastSearchQuery.isNotBlank())
            lazyListState.scrollToItem(0)
        lastSearchQuery = searchQuery
    }

    val filteredContacts = remember(BaresipService.contacts, searchQuery) {
        if (searchQuery.isBlank())
            BaresipService.contacts.map { contact ->
                Triple(contact, buildAnnotatedString { append(contact.name()) }, null)
            }
        else {
            val normalizedQuery = Utils.unaccent(searchQuery)
            val numericQuery = searchQuery.filter { it.isDigit() || it == '+' }
            BaresipService.contacts.mapNotNull { contact ->
                val nameMatch = Utils.unaccent(contact.name()).contains(normalizedQuery, ignoreCase = true)
                var matchingUri: Contact.ContactUri? = null
                if (numericQuery.isNotEmpty()) {
                    matchingUri = contact.uris().find {
                        it.uri.startsWith("tel:") && it.uri.substring(4).contains(numericQuery)
                    }
                }
                if (nameMatch || matchingUri != null) {
                    val annotatedName = if (nameMatch)
                        Utils.buildAnnotatedStringWithHighlight(contact.name(), searchQuery)
                    else
                        AnnotatedString(contact.name())
                    Triple(contact, annotatedName, matchingUri)
                } else {
                    null
                }
            }
        }
    }

    LaunchedEffect(filteredContacts.size, searchQuery) {
        Log.d(TAG, "ContactsScreen: rendering ${filteredContacts.size} contacts, query='$searchQuery'")
    }

    val groupedContacts = remember(filteredContacts) {
        filteredContacts.groupBy { (contact, _, _) ->
            val firstChar = contact.name().trim().firstOrNull()?.uppercaseChar()
            if (firstChar != null && firstChar.isLetter()) firstChar.toString() else "#"
        }.toSortedMap()
    }

    LaunchedEffect(scrollToContact?.value, filteredContacts) {
        scrollToContact?.value?.let { name ->
            val index = filteredContacts.indexOfFirst { it.first.name() == name }
            if (index != -1)
                lazyListState.scrollToItem(index)
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scrollToContact")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = contentPadding.calculateTopPadding())
    ) {
        // Modern Pill Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    onSearchQueryChange(it)
                    Log.d(TAG, "ContactsScreen: Search query updated to '$it'")
                    if (it.isBlank())
                        keyboardController?.hide()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .shadow(4.dp, RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            onSearchQueryChange("")
                            keyboardController?.hide()
                        }) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        stringResource(R.string.search) + "...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(4.dp, CircleShape)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                onClick = { navController.navigate("contact//new") }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (filteredContacts.isEmpty()) {
            CustomElements.EmptyStateBanner(
                icon = Icons.Filled.Person,
                title = stringResource(R.string.no_android_contacts),
                message = if (searchQuery.isNotEmpty()) "No contacts match \"$searchQuery\"" else "Add a contact using the + button below or import contacts via the menu.",
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScrollbar(state = lazyListState),
                state = lazyListState,
                contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 80.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                groupedContacts.forEach { (letter, contactsInGroup) ->
                    stickyHeader(key = "header_$letter") {
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
                                    text = letter,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    itemsIndexed(
                        contactsInGroup,
                        key = { index, (contact, _, _) -> "${contact.id()}_$index" }
                    ) { _, (contact, annotatedName, matchingUri) ->
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
                                            Log.d(TAG, "ContactsScreen: contact row click for '${contact.name()}'")
                                            navController.navigate("contact/${contact.name()}/old")
                                        },
                                        onLongClick = {
                                            Log.d(TAG, "ContactsScreen: Contact long-pressed: ${contact.name()}")
                                            title.value = confirmationText
                                            message.value = String.format(contactDeleteQuestion, contact.name())
                                            firstButtonText.value = cancelText
                                            onFirstClicked.value = { }
                                            lastButtonText.value = deleteText
                                            onLastClicked.value = {
                                                Log.d(TAG, "ContactsScreen: Deleting contact ${contact.name()}")
                                                when (contact) {
                                                    is Contact.BaresipContact -> {
                                                        val id = contact.id
                                                        val avatarFile = File(BaresipService.filesPath, "$id.png")
                                                        if (avatarFile.exists())
                                                            try {
                                                                avatarFile.delete()
                                                            } catch (e: IOException) {
                                                                Log.e(TAG, "Could not delete file $id.png: ${e.message}")
                                                            }
                                                        Contact.removeBaresipContact(contact)
                                                    }
                                                    is Contact.AndroidContact -> {
                                                        ctx.contentResolver.delete(
                                                            ContactsContract.RawContacts.CONTENT_URI,
                                                            ContactsContract.Contacts.DISPLAY_NAME + "='" + contact.name() + "'",
                                                            null
                                                        )
                                                    }
                                                }
                                            }
                                            showDialog.value = true
                                        }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar (48dp with subtle border & shadow)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .shadow(2.dp, CircleShape)
                                ) {
                                    when (contact) {
                                        is Contact.BaresipContact -> {
                                            val avatarImage = contact.avatarImage
                                            if (avatarImage != null)
                                                Image(
                                                    bitmap = avatarImage.asImageBitmap(),
                                                    contentDescription = "Avatar",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                                )
                                            else
                                                ModernContactAvatar(contact.name(), contact.color)
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
                                                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                                )
                                            else
                                                ModernContactAvatar(contact.name(), contact.color)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Contact info column
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = annotatedName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else Color(0xFF111827),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontStyle = if (contact.favorite()) FontStyle.Italic else FontStyle.Normal
                                        )
                                        if (contact.favorite()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Favorite",
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    val displayUri = matchingUri ?: contact.uris().firstOrNull()
                                    if (displayUri != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isDark) Color.White.copy(alpha = 0.06f) else Color(0xFFF1F5F9),
                                            border = BorderStroke(0.5.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                if (displayUri.label.isNotEmpty()) {
                                                    Text(
                                                        text = displayUri.label.uppercase(),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    )
                                                }
                                                val uriClean = displayUri.uri.substringAfter(":")
                                                val annotatedTel = if (matchingUri != null) {
                                                    Utils.buildAnnotatedStringWithHighlight(
                                                        uriClean,
                                                        searchQuery.filter { it.isDigit() || it == '+' }
                                                    )
                                                } else {
                                                    buildAnnotatedString { append(uriClean) }
                                                }
                                                Text(
                                                    text = annotatedTel,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }

                                // Quick Call and Chat actions
                                val primaryUri = contact.uris().firstOrNull()?.uri
                                if (!primaryUri.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Quick Call
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
                                                    Log.d(TAG, "ContactsScreen: Quick Call initiated for ${contact.name()} -> $primaryUri")
                                                    val ua = uas.value.find { it.account.aor == viewModel.selectedAor.value }
                                                        ?: uas.value.firstOrNull()
                                                    if (ua != null) {
                                                        val intent = Intent(ctx, MainActivity::class.java).apply {
                                                            putExtra("uap", ua.uap)
                                                            putExtra("peer", primaryUri)
                                                        }
                                                        handleIntent(ctx, viewModel, intent, BaresipService.contactAction)
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

                                        // Quick Chat
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.18f else 0.12f)
                                                )
                                                .border(
                                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.35f else 0.25f)),
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    Log.d(TAG, "ContactsScreen: Quick Chat initiated for ${contact.name()} -> $primaryUri")
                                                    val aor = viewModel.selectedAor.value.ifEmpty {
                                                        uas.value.firstOrNull()?.account?.aor ?: ""
                                                    }
                                                    if (aor.isNotEmpty()) {
                                                        navController.navigate("chat/$aor/$primaryUri")
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                                contentDescription = "Chat",
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
        }
    }
}

@Composable
private fun ModernContactAvatar(name: String, color: Int) {
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
