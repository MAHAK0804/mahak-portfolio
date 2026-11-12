package com.mahak.portfoliomanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val Ink = Color(0xFF101418)
private val Panel = Color(0xFF1B2326)
private val Paper = Color(0xFFF1EEE8)
private val Muted = Color(0xFFA6ACAE)
private val Mint = Color(0xFF99F6CF)
private val Coral = Color(0xFFFF8066)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(16, 20, 24)),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.rgb(16, 20, 24))
        )
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Mint,
                    onPrimary = Ink,
                    secondary = Coral,
                    background = Ink,
                    surface = Panel,
                    onBackground = Paper,
                    onSurface = Paper,
                    error = Coral
                )
            ) {
                PortfolioManagerApp(
                    SupabaseRepository(
                        projectUrl = BuildConfig.SUPABASE_URL,
                        anonKey = BuildConfig.ANON_KEY,
                        ownerId = BuildConfig.ADMIN_USER_ID
                    )
                )
            }
        }
    }
}

private data class EditorField(
    val label: String,
    val value: String,
    val multiline: Boolean = false
)

@Composable
private fun PortfolioManagerApp(repository: SupabaseRepository) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isAuthenticated by remember { mutableStateOf(false) }
    var content by remember { mutableStateOf(PortfolioContent.defaults()) }
    var messages by remember { mutableStateOf<List<ContactMessage>>(emptyList()) }
    var isBusy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableIntStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var editorFields by remember { mutableStateOf<List<EditorField>?>(null) }
    var editorTitle by remember { mutableStateOf("") }
    var editorSave by remember { mutableStateOf<((List<String>) -> Unit)?>(null) }
    var pendingDelete by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingDeleteTitle by remember { mutableStateOf("") }
    var replyingToMessageId by remember { mutableStateOf<String?>(null) }
    var replyDraft by remember { mutableStateOf("") }

    fun launchSave(updatedContent: PortfolioContent, successMessage: String = "Changes saved and published.") {
        if (!isAuthenticated) return
        scope.launch {
            isBusy = true
            errorMessage = null
            try {
                repository.saveContent(updatedContent)
                content = updatedContent
                snackbarHostState.showSnackbar(successMessage)
            } catch (error: Exception) {
                errorMessage = error.message ?: "The portfolio could not be saved."
            } finally {
                isBusy = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!repository.isConfigured) {
            errorMessage = "Configure Supabase in android-app/local.properties before signing in."
        }
    }

    if (!isAuthenticated) {
        LoginScreen(
            isConfigured = repository.isConfigured,
            isBusy = isBusy,
            errorMessage = errorMessage,
            onSignIn = { email, password ->
                scope.launch {
                    isBusy = true
                    errorMessage = null
                    try {
                        repository.signIn(email, password)
                        val loadedContent = repository.loadContent()
                        val loadedMessages = repository.loadContactMessages()
                        isAuthenticated = true
                        content = loadedContent
                        messages = loadedMessages
                    } catch (error: Exception) {
                        errorMessage = error.message ?: "Sign in failed. Please try again."
                    } finally {
                        isBusy = false
                    }
                }
            }
        )
    } else {
        ManagerScreen(
            content = content,
            activeTab = activeTab,
            isBusy = isBusy,
            errorMessage = errorMessage,
            messages = messages,
            snackbarHostState = snackbarHostState,
            showMenu = showMenu,
            onMenuChange = { showMenu = it },
            onTabChange = { activeTab = it },
            onContentChange = { content = it },
            onSave = { launchSave(content) },
            onSignOut = {
                scope.launch {
                    isBusy = true
                    errorMessage = null
                    try {
                        repository.signOut()
                    } catch (error: Exception) {
                        errorMessage = error.message ?: "The remote session could not be ended."
                    } finally {
                        isAuthenticated = false
                        isBusy = false
                    }
                }
            },
            onRefreshMessages = {
                scope.launch {
                    isBusy = true
                    errorMessage = null
                    try {
                        messages = repository.loadContactMessages()
                    } catch (error: Exception) {
                        errorMessage = error.message ?: "Could not refresh the message inbox."
                    } finally {
                        isBusy = false
                    }
                }
            },
            onMarkMessageRead = { message ->
                scope.launch {
                    try {
                        repository.markContactMessageRead(message.id)
                        messages = messages.map { if (it.id == message.id) it.copy(isRead = true) else it }
                    } catch (error: Exception) {
                        errorMessage = error.message ?: "Could not mark the message as read."
                    }
                }
            },
            onSendReply = { message ->
                val body = replyDraft.trim()
                if (body.isNotEmpty() && !isBusy) {
                    scope.launch {
                        isBusy = true
                        errorMessage = null
                        try {
                            repository.sendContactReply(message.id, body)
                            replyDraft = ""
                            replyingToMessageId = null
                            messages = repository.loadContactMessages()
                            snackbarHostState.showSnackbar("Reply emailed to ${message.email}.")
                        } catch (error: Exception) {
                            val failureMessage = error.message ?: "Could not send the reply."
                            errorMessage = failureMessage
                            if (error is ContactReplyEmailException) {
                                replyDraft = ""
                                replyingToMessageId = null
                            }
                            try {
                                messages = repository.loadContactMessages()
                            } catch (refreshError: Exception) {
                                errorMessage = "${errorMessage}; inbox refresh failed: ${refreshError.message ?: "unknown error"}"
                            }
                            snackbarHostState.showSnackbar(
                                if (error is ContactReplyEmailException) {
                                    "Reply saved, but its email could not be sent. Use Retry sending email in the inbox."
                                } else {
                                    failureMessage
                                }
                            )
                        } finally {
                            isBusy = false
                        }
                    }
                }
            },
            onRetryReplyEmail = { reply ->
                scope.launch {
                    isBusy = true
                    errorMessage = null
                    try {
                        repository.retryContactReplyEmail(reply.id)
                        messages = repository.loadContactMessages()
                        snackbarHostState.showSnackbar("Reply email sent.")
                    } catch (error: Exception) {
                        errorMessage = error.message ?: "Could not send the reply email."
                        snackbarHostState.showSnackbar(errorMessage ?: "Could not send the reply email.")
                    } finally {
                        isBusy = false
                    }
                }
            },
            replyingToMessageId = replyingToMessageId,
            replyDraft = replyDraft,
            onReplyDraftChange = { replyDraft = it },
            onStartReply = { message ->
                replyingToMessageId = message.id
                replyDraft = ""
                if (!message.isRead) {
                    scope.launch {
                        try {
                            repository.markContactMessageRead(message.id)
                            messages = messages.map {
                                if (it.id == message.id) it.copy(isRead = true) else it
                            }
                        } catch (error: Exception) {
                            errorMessage = error.message ?: "Could not mark the message as read."
                        }
                    }
                }
            },
            onCancelReply = {
                replyingToMessageId = null
                replyDraft = ""
            },
            onReset = { showResetDialog = true },
            onAdd = {
                when (activeTab) {
                    0 -> {
                        editorTitle = "Add project"
                        editorFields = projectFields(Project(0, "", "", "", emptyList(), ""))
                        editorSave = { values ->
                            val item = projectFromFields(0, values)
                            content = content.copy(projects = content.projects + item.copy(id = nextProjectId(content.projects)))
                        }
                    }
                    1 -> {
                        editorTitle = "Add experience"
                        editorFields = experienceFields(Experience(0, "", "", "", ""))
                        editorSave = { values ->
                            val item = experienceFromFields(0, values)
                            content = content.copy(experiences = content.experiences + item.copy(id = nextExperienceId(content.experiences)))
                        }
                    }
                    2 -> {
                        editorTitle = "Add service"
                        editorFields = serviceFields(Service(0, "", "", "◆"))
                        editorSave = { values ->
                            val item = serviceFromFields(0, values)
                            content = content.copy(services = content.services + item.copy(id = nextServiceId(content.services)))
                        }
                    }
                    else -> {
                        editorTitle = "Edit technologies"
                        editorFields = listOf(EditorField("Technologies (separate with commas)", content.skills.joinToString(", "), true))
                        editorSave = { values ->
                            content = content.copy(skills = values.first().split(",").map(String::trim).filter(String::isNotEmpty))
                        }
                    }
                }
            },
            onEdit = { title, fields, save ->
                editorTitle = title
                editorFields = fields
                editorSave = save
            },
            onDelete = { title, delete ->
                pendingDeleteTitle = title
                pendingDelete = delete
            }
        )
    }

    if (editorFields != null) {
        EditorDialog(
            title = editorTitle,
            fields = editorFields.orEmpty(),
            onDismiss = { editorFields = null },
            onSave = { values ->
                editorSave?.invoke(values)
                editorFields = null
            }
        )
    }

    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove $pendingDeleteTitle?") },
            text = { Text("This change will be included the next time you save.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete?.invoke()
                    pendingDelete = null
                }) { Text("Remove", color = Coral) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restore default content?") },
            text = { Text("This replaces the shared portfolio content with the original portfolio details.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    launchSave(PortfolioContent.defaults(), "Default content restored and published.")
                }) { Text("Restore", color = Coral) }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun LoginScreen(
    isConfigured: Boolean,
    isBusy: Boolean,
    errorMessage: String?,
    onSignIn: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("PRIVATE WORKSPACE", color = Coral, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(12.dp))
            Text("Portfolio manager", style = MaterialTheme.typography.headlineLarge, color = Paper)
            Spacer(Modifier.height(8.dp))
            Text("Sign in with the portfolio owner account to edit shared content.", color = Muted)
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                singleLine = true,
                enabled = isConfigured && !isBusy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                enabled = isConfigured && !isBusy,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )
            if (errorMessage != null) {
                Spacer(Modifier.height(16.dp))
                Text(errorMessage, color = Coral)
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSignIn(email.trim(), password) },
                enabled = isConfigured && !isBusy && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isBusy) CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                else Text("Sign in")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManagerScreen(
    content: PortfolioContent,
    activeTab: Int,
    isBusy: Boolean,
    errorMessage: String?,
    messages: List<ContactMessage>,
    replyingToMessageId: String?,
    replyDraft: String,
    snackbarHostState: SnackbarHostState,
    showMenu: Boolean,
    onMenuChange: (Boolean) -> Unit,
    onTabChange: (Int) -> Unit,
    onContentChange: (PortfolioContent) -> Unit,
    onSave: () -> Unit,
    onSignOut: () -> Unit,
    onRefreshMessages: () -> Unit,
    onMarkMessageRead: (ContactMessage) -> Unit,
    onSendReply: (ContactMessage) -> Unit,
    onRetryReplyEmail: (ContactMessageReply) -> Unit,
    onReplyDraftChange: (String) -> Unit,
    onStartReply: (ContactMessage) -> Unit,
    onCancelReply: () -> Unit,
    onReset: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String, List<EditorField>, (List<String>) -> Unit) -> Unit,
    onDelete: (String, () -> Unit) -> Unit
) {
    val tabs = listOf(
        "Projects" to content.projects.size,
        "Experience" to content.experiences.size,
        "Services" to content.services.size,
        "Technologies" to content.skills.size,
        "Inbox" to messages.count { !it.isRead }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Portfolio manager", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    TextButton(onClick = onSave, enabled = !isBusy) {
                        if (isBusy) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                        else Text("Save")
                    }
                    androidx.compose.material3.IconButton(onClick = { onMenuChange(true) }) {
                        Text("⋮", style = MaterialTheme.typography.titleLarge)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { onMenuChange(false) }) {
                        DropdownMenuItem(
                            text = { Text("Restore defaults") },
                            onClick = { onMenuChange(false); onReset() }
                        )
                        DropdownMenuItem(
                            text = { Text("Sign out") },
                            onClick = { onMenuChange(false); onSignOut() }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (activeTab < 3) FloatingActionButton(onClick = onAdd, containerColor = Mint, contentColor = Ink) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        },
        containerColor = Ink
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PrimaryScrollableTabRow(selectedTabIndex = activeTab, containerColor = Ink, contentColor = Mint) {
                tabs.forEachIndexed { index, (title, count) ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { onTabChange(index) },
                        text = { Text("$title  $count", maxLines = 1) }
                    )
                }
            }
            if (errorMessage != null) {
                Text(
                    errorMessage,
                    color = Coral,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
            when (activeTab) {
                0 -> ProjectsList(content, onEdit, onDelete, onContentChange)
                1 -> ExperiencesList(content, onEdit, onDelete, onContentChange)
                2 -> ServicesList(content, onEdit, onDelete, onContentChange)
                3 -> SkillsList(content, onEdit, onContentChange)
                else -> ContactInboxScreen(
                    messages = messages,
                    isBusy = isBusy,
                    replyingToMessageId = replyingToMessageId,
                    replyDraft = replyDraft,
                    onRefresh = onRefreshMessages,
                    onMarkRead = onMarkMessageRead,
                    onReply = onSendReply,
                    onRetryEmail = onRetryReplyEmail,
                    onReplyDraftChange = onReplyDraftChange,
                    onStartReply = onStartReply,
                    onCancelReply = onCancelReply
                )
            }
        }
    }
}

@Composable
private fun ProjectsList(
    content: PortfolioContent,
    onEdit: (String, List<EditorField>, (List<String>) -> Unit) -> Unit,
    onDelete: (String, () -> Unit) -> Unit,
    onContentChange: (PortfolioContent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(content.projects, key = { _, project -> project.id }) { _, project ->
            RecordCard(
                title = project.title.ifBlank { "Untitled project" },
                subtitle = project.role,
                onEdit = {
                    onEdit("Edit project", projectFields(project)) { values ->
                        val updated = projectFromFields(project.id, values)
                        onContentChange(content.copy(projects = content.projects.map {
                            if (it.id == project.id) updated else it
                        }))
                    }
                },
                onDelete = {
                    onDelete(project.title.ifBlank { "project" }) {
                        onContentChange(content.copy(projects = content.projects.filterNot { it.id == project.id }))
                    }
                }
            )
        }
    }
}

@Composable
private fun ExperiencesList(
    content: PortfolioContent,
    onEdit: (String, List<EditorField>, (List<String>) -> Unit) -> Unit,
    onDelete: (String, () -> Unit) -> Unit,
    onContentChange: (PortfolioContent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(content.experiences, key = { _, experience -> experience.id }) { _, experience ->
            RecordCard(
                title = experience.position.ifBlank { "Untitled position" },
                subtitle = experience.company,
                onEdit = {
                    onEdit("Edit experience", experienceFields(experience)) { values ->
                        val updated = experienceFromFields(experience.id, values)
                        onContentChange(content.copy(experiences = content.experiences.map {
                            if (it.id == experience.id) updated else it
                        }))
                    }
                },
                onDelete = {
                    onDelete(experience.position.ifBlank { "experience" }) {
                        onContentChange(content.copy(experiences = content.experiences.filterNot { it.id == experience.id }))
                    }
                }
            )
        }
    }
}

@Composable
private fun ServicesList(
    content: PortfolioContent,
    onEdit: (String, List<EditorField>, (List<String>) -> Unit) -> Unit,
    onDelete: (String, () -> Unit) -> Unit,
    onContentChange: (PortfolioContent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(content.services, key = { _, service -> service.id }) { _, service ->
            RecordCard(
                title = service.name.ifBlank { "Untitled service" },
                subtitle = service.description,
                onEdit = {
                    onEdit("Edit service", serviceFields(service)) { values ->
                        val updated = serviceFromFields(service.id, values)
                        onContentChange(content.copy(services = content.services.map {
                            if (it.id == service.id) updated else it
                        }))
                    }
                },
                onDelete = {
                    onDelete(service.name.ifBlank { "service" }) {
                        onContentChange(content.copy(services = content.services.filterNot { it.id == service.id }))
                    }
                }
            )
        }
    }
}

@Composable
private fun SkillsList(
    content: PortfolioContent,
    onEdit: (String, List<EditorField>, (List<String>) -> Unit) -> Unit,
    onContentChange: (PortfolioContent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Technologies", style = MaterialTheme.typography.titleLarge)
        Text("Edit the comma-separated technology list shown on the portfolio.", color = Muted)
        RecordCard(
            title = "${content.skills.size} technologies",
            subtitle = content.skills.joinToString(", "),
            onEdit = {
                onEdit(
                    "Edit technologies",
                    listOf(EditorField("Technologies (separate with commas)", content.skills.joinToString(", "), true))
                ) { values ->
                    onContentChange(content.copy(
                        skills = values.first().split(",").map(String::trim).filter(String::isNotEmpty)
                    ))
                }
            },
            onDelete = null
        )
    }
}

@Composable
private fun RecordCard(title: String, subtitle: String, onEdit: () -> Unit, onDelete: (() -> Unit)?) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Mint)
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Muted, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEdit) { Text("Edit") }
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Remove", color = Coral) }
                }
            }
        }
    }
}

@Composable
private fun EditorDialog(title: String, fields: List<EditorField>, onDismiss: () -> Unit, onSave: (List<String>) -> Unit) {
    var values by remember(title, fields) { mutableStateOf(fields.map(EditorField::value)) }
    var validationMessage by remember(title, fields) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                fields.forEachIndexed { index, field ->
                    OutlinedTextField(
                        value = values[index],
                        onValueChange = { value ->
                            values = values.toMutableList().also { it[index] = value }
                            validationMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(field.label) },
                        minLines = if (field.multiline) 3 else 1,
                        maxLines = if (field.multiline) 6 else 1
                    )
                }
                if (validationMessage != null) {
                    Text(validationMessage.orEmpty(), color = Coral)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.startsWith("Add ") && values.firstOrNull().isNullOrBlank()) {
                    validationMessage = when (title) {
                        "Add experience" -> "Enter a position before adding the experience."
                        "Add service" -> "Enter a service name before adding it."
                        else -> "Enter a project title before adding it."
                    }
                } else {
                    onSave(values)
                }
            }) { Text("Done") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun projectFields(project: Project) = listOf(
    EditorField("Title", project.title),
    EditorField("Role", project.role),
    EditorField("Team size", project.teamSize),
    EditorField("Technologies (separate with commas)", project.technologies.joinToString(", ")),
    EditorField("Description", project.description, true)
)

private fun experienceFields(experience: Experience) = listOf(
    EditorField("Position", experience.position),
    EditorField("Company", experience.company),
    EditorField("Location", experience.location),
    EditorField("Description", experience.description, true)
)

private fun serviceFields(service: Service) = listOf(
    EditorField("Name", service.name),
    EditorField("Description", service.description, true),
    EditorField("Icon", service.icon)
)

private fun projectFromFields(id: Int, values: List<String>) = Project(
    id = id,
    title = values[0].trim(),
    role = values[1].trim(),
    teamSize = values[2].trim(),
    technologies = values[3].split(",").map(String::trim).filter(String::isNotEmpty),
    description = values[4].trim()
)

private fun experienceFromFields(id: Int, values: List<String>) = Experience(
    id = id,
    position = values[0].trim(),
    company = values[1].trim(),
    location = values[2].trim(),
    description = values[3].trim()
)

private fun serviceFromFields(id: Int, values: List<String>) = Service(
    id = id,
    name = values[0].trim(),
    description = values[1].trim(),
    icon = values[2].trim()
)

private fun nextProjectId(items: List<Project>) = (items.maxOfOrNull(Project::id) ?: 0) + 1
private fun nextExperienceId(items: List<Experience>) = (items.maxOfOrNull(Experience::id) ?: 0) + 1
private fun nextServiceId(items: List<Service>) = (items.maxOfOrNull(Service::id) ?: 0) + 1
