package com.mahak.portfoliomanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val InboxPanel = Color(0xFF1B2326)
private val InboxPaper = Color(0xFFF1EEE8)
private val InboxMuted = Color(0xFFA6ACAE)
private val InboxMint = Color(0xFF99F6CF)
private val InboxCoral = Color(0xFFFF8066)

@Composable
fun ContactInboxScreen(
    messages: List<ContactMessage>,
    isBusy: Boolean,
    replyingToMessageId: String?,
    replyDraft: String,
    onRefresh: () -> Unit,
    onMarkRead: (ContactMessage) -> Unit,
    onReply: (ContactMessage) -> Unit,
    onRetryEmail: (ContactMessageReply) -> Unit,
    onReplyDraftChange: (String) -> Unit,
    onStartReply: (ContactMessage) -> Unit,
    onCancelReply: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Visitor inbox", style = MaterialTheme.typography.titleLarge, color = InboxPaper)
                    Text("Messages update from your portfolio contact form.", color = InboxMuted)
                }
                TextButton(onClick = onRefresh, enabled = !isBusy) {
                    if (isBusy) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    else Text("Refresh")
                }
            }
        }

        if (messages.isEmpty()) {
            item {
                Text("No visitor messages yet.", color = InboxMuted, modifier = Modifier.padding(vertical = 12.dp))
            }
        }

        items(messages, key = ContactMessage::id) { message ->
            Card(
                colors = CardDefaults.cardColors(containerColor = InboxPanel),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            message.subject.ifBlank { "New portfolio inquiry" },
                            style = MaterialTheme.typography.titleMedium,
                            color = InboxMint,
                            modifier = Modifier.weight(1f)
                        )
                        if (!message.isRead) {
                            Text("NEW", color = InboxCoral, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.height(5.dp))
                    Text("${message.fullName} · ${message.email}", color = InboxMuted)
                    Text(message.createdAt, color = InboxMuted, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(12.dp))
                    Text(message.message, color = InboxPaper)

                    message.replies.forEach { reply ->
                        Spacer(Modifier.height(14.dp))
                        Text("Your reply · ${reply.createdAt}", color = InboxMint, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(4.dp))
                        Text(reply.body, color = InboxPaper)
                        if (reply.emailSentAt == null) {
                            Text("Email not sent", color = InboxCoral, style = MaterialTheme.typography.labelSmall)
                            TextButton(
                                onClick = { onRetryEmail(reply) },
                                enabled = !isBusy
                            ) { Text("Retry sending email") }
                        } else {
                            Text("Email sent", color = InboxMuted, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!message.isRead) {
                            TextButton(onClick = { onMarkRead(message) }, enabled = !isBusy) {
                                Text("Mark read")
                            }
                        }
                        TextButton(onClick = { onStartReply(message) }, enabled = !isBusy) {
                            Text("Reply")
                        }
                    }

                    if (replyingToMessageId == message.id) {
                        OutlinedTextField(
                            value = replyDraft,
                            onValueChange = onReplyDraftChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email reply") },
                            minLines = 3,
                            maxLines = 7,
                            supportingText = { Text("The reply is saved in this conversation and emailed to the visitor.") }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onCancelReply, enabled = !isBusy) { Text("Cancel") }
                            Button(
                                onClick = { onReply(message) },
                                enabled = !isBusy && replyDraft.isNotBlank()
                            ) {
                                if (isBusy) CircularProgressIndicator(
                                    modifier = Modifier.height(18.dp),
                                    strokeWidth = 2.dp
                                ) else Text("Send email")
                            }
                        }
                    }
                }
            }
        }
    }
}
