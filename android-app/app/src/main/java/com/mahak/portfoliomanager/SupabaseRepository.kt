package com.mahak.portfoliomanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

data class ContactMessageReply(
    val id: String,
    val body: String,
    val createdAt: String,
    val emailSentAt: String?
)

data class ContactMessage(
    val id: String,
    val fullName: String,
    val email: String,
    val subject: String,
    val message: String,
    val createdAt: String,
    val isRead: Boolean,
    val replies: List<ContactMessageReply>
)

private data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long
)

class SupabaseRepository(
    private val projectUrl: String,
    private val anonKey: String,
    private val ownerId: String
) {
    private var session: AuthSession? = null

    val isConfigured: Boolean
        get() = projectUrl.startsWith("https://")
            && anonKey.isNotBlank()
            && ownerId.isNotBlank()

    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        requireConfigured()
        require(email.isNotBlank() && password.isNotBlank()) { "Enter your email and password." }

        val response = request(
            method = "POST",
            path = "/auth/v1/token?grant_type=password",
            body = JSONObject().put("email", email).put("password", password)
        )
        val result = JSONObject(response)
        val accessToken = result.getString("access_token")
        val refreshToken = result.getString("refresh_token")
        val signedInUserId = result.getJSONObject("user").getString("id")
        if (signedInUserId != ownerId) {
            try {
                request("POST", "/auth/v1/logout?scope=local", token = accessToken)
            } catch (error: Exception) {
                throw IllegalStateException(
                    "This account does not have portfolio editor access. The temporary session could not be revoked: ${error.message}",
                    error
                )
            }
            throw IllegalStateException("This account does not have portfolio editor access.")
        }
        session = AuthSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAtMillis = System.currentTimeMillis() + result.optLong("expires_in", 3600L) * 1000
        )
    }

    suspend fun loadContent(): PortfolioContent = withContext(Dispatchers.IO) {
        val response = request(
            method = "GET",
            path = "/rest/v1/portfolio_content?id=eq.1&select=content",
            token = currentAccessToken()
        )
        val rows = JSONArray(response)
        if (rows.length() == 0) PortfolioContent.defaults()
        else contentFromJson(rows.getJSONObject(0).getJSONObject("content"))
    }

    suspend fun saveContent(content: PortfolioContent) = withContext(Dispatchers.IO) {
        requireConfigured()
        val row = JSONObject()
            .put("id", 1)
            .put("owner_id", ownerId)
            .put("content", contentToJson(content))
            .put("updated_at", Instant.now().toString())
        val response = request(
            method = "POST",
            path = "/rest/v1/portfolio_content?on_conflict=id",
            token = currentAccessToken(),
            body = row,
            prefer = "resolution=merge-duplicates,return=representation"
        )
        val savedRows = JSONArray(response)
        if (savedRows.length() != 1) {
            throw IllegalStateException("Supabase did not confirm the portfolio save. Check the table permissions and try again.")
        }
        val savedContent = contentFromJson(savedRows.getJSONObject(0).getJSONObject("content"))
        if (savedContent != content) {
            throw IllegalStateException("Supabase returned different content than was submitted. Please reload before saving again.")
        }
    }

    suspend fun loadContactMessages(): List<ContactMessage> = withContext(Dispatchers.IO) {
        val response = request(
            method = "GET",
            path = "/rest/v1/contact_messages?select=id,full_name,email,subject,message,created_at,is_read,contact_message_replies(id,body,created_at,email_sent_at)&order=created_at.desc",
            token = currentAccessToken()
        )
        val rows = JSONArray(response)
        List(rows.length()) { index ->
            rows.getJSONObject(index).let { message ->
                val replies = message.optJSONArray("contact_message_replies") ?: JSONArray()
                ContactMessage(
                    id = message.getString("id"),
                    fullName = message.getString("full_name"),
                    email = message.getString("email"),
                    subject = message.optString("subject"),
                    message = message.getString("message"),
                    createdAt = message.getString("created_at"),
                    isRead = message.optBoolean("is_read"),
                    replies = List(replies.length()) { replyIndex ->
                        replies.getJSONObject(replyIndex).let { reply ->
                            ContactMessageReply(
                                id = reply.getString("id"),
                                body = reply.getString("body"),
                                createdAt = reply.getString("created_at"),
                                emailSentAt = if (reply.isNull("email_sent_at")) null else reply.getString("email_sent_at")
                            )
                        }
                    }
                )
            }
        }
    }

    suspend fun markContactMessageRead(messageId: String) = withContext(Dispatchers.IO) {
        request(
            method = "POST",
            path = "/rest/v1/rpc/mark_contact_message_read",
            token = currentAccessToken(),
            body = JSONObject().put("p_message_id", messageId)
        )
    }

    suspend fun saveContactReply(messageId: String, body: String): ContactMessageReply = withContext(Dispatchers.IO) {
        require(body.isNotBlank()) { "Write a reply before sending." }
        val response = request(
            method = "POST",
            path = "/rest/v1/contact_message_replies",
            token = currentAccessToken(),
            body = JSONObject().put("message_id", messageId).put("body", body.trim()),
            prefer = "return=representation"
        )
        JSONArray(response).getJSONObject(0).let { reply ->
            ContactMessageReply(
                id = reply.getString("id"),
                body = reply.getString("body"),
                createdAt = reply.getString("created_at"),
                emailSentAt = if (reply.isNull("email_sent_at")) null else reply.getString("email_sent_at")
            )
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        val activeSession = session
        try {
            if (activeSession != null) {
                request("POST", "/auth/v1/logout?scope=local", token = activeSession.accessToken)
            }
        } finally {
            session = null
        }
    }

    private fun currentAccessToken(): String {
        val current = checkNotNull(session) { "Sign in with the portfolio owner account before continuing." }
        if (current.expiresAtMillis > System.currentTimeMillis() + 60_000) return current.accessToken

        val response = request(
            method = "POST",
            path = "/auth/v1/token?grant_type=refresh_token",
            body = JSONObject().put("refresh_token", current.refreshToken)
        )
        val refreshed = JSONObject(response)
        if (refreshed.getJSONObject("user").getString("id") != ownerId) {
            session = null
            throw IllegalStateException("The refreshed account does not have portfolio editor access. Sign in again.")
        }
        return AuthSession(
            accessToken = refreshed.getString("access_token"),
            refreshToken = refreshed.getString("refresh_token"),
            expiresAtMillis = System.currentTimeMillis() + refreshed.optLong("expires_in", 3600L) * 1000
        ).also { session = it }.accessToken
    }

    private fun requireConfigured() {
        check(isConfigured) {
            "Configure SUPABASE_URL, ANON_KEY, and ADMIN_USER_ID in android-app/local.properties."
        }
    }

    private fun request(
        method: String,
        path: String,
        token: String? = null,
        body: JSONObject? = null,
        prefer: String? = null
    ): String {
        val connection = URL(projectUrl.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 20_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("apikey", anonKey)
            connection.setRequestProperty("Authorization", "Bearer ${token ?: anonKey}")
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
            }
            if (prefer != null) connection.setRequestProperty("Prefer", prefer)

            if (body != null) {
                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }
            }

            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val response = responseStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (statusCode !in 200..299) {
                val detail = try {
                    JSONObject(response).optString("message").ifBlank { response }
                } catch (_: Exception) {
                    response
                }
                throw IOException(detail.ifBlank { "Supabase request failed with HTTP $statusCode." })
            }
            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun contentToJson(content: PortfolioContent) = JSONObject()
        .put("projects", JSONArray().apply {
            content.projects.forEach { project ->
                put(
                    JSONObject()
                        .put("id", project.id)
                        .put("title", project.title)
                        .put("description", project.description)
                        .put("role", project.role)
                        .put("technologies", JSONArray(project.technologies))
                        .put("teamSize", project.teamSize)
                )
            }
        })
        .put("experiences", JSONArray().apply {
            content.experiences.forEach { experience ->
                put(
                    JSONObject()
                        .put("id", experience.id)
                        .put("position", experience.position)
                        .put("company", experience.company)
                        .put("location", experience.location)
                        .put("description", experience.description)
                )
            }
        })
        .put("services", JSONArray().apply {
            content.services.forEach { service ->
                put(
                    JSONObject()
                        .put("id", service.id)
                        .put("name", service.name)
                        .put("description", service.description)
                        .put("icon", service.icon)
                )
            }
        })
        .put("skills", JSONArray(content.skills))

    private fun contentFromJson(json: JSONObject) = PortfolioContent(
        projects = json.getJSONArray("projects").let { array ->
            List(array.length()) { index ->
                array.getJSONObject(index).let { project ->
                    Project(
                        id = project.getInt("id"),
                        title = project.optString("title"),
                        description = project.optString("description"),
                        role = project.optString("role"),
                        technologies = project.getJSONArray("technologies").let { technologies ->
                            List(technologies.length()) { technologyIndex -> technologies.getString(technologyIndex) }
                        },
                        teamSize = project.optString("teamSize")
                    )
                }
            }
        },
        experiences = json.getJSONArray("experiences").let { array ->
            List(array.length()) { index ->
                array.getJSONObject(index).let { experience ->
                    Experience(
                        id = experience.getInt("id"),
                        position = experience.optString("position"),
                        company = experience.optString("company"),
                        location = experience.optString("location"),
                        description = experience.optString("description")
                    )
                }
            }
        },
        services = json.getJSONArray("services").let { array ->
            List(array.length()) { index ->
                array.getJSONObject(index).let { service ->
                    Service(
                        id = service.getInt("id"),
                        name = service.optString("name"),
                        description = service.optString("description"),
                        icon = service.optString("icon")
                    )
                }
            }
        },
        skills = json.getJSONArray("skills").let { skills ->
            List(skills.length()) { index -> skills.getString(index) }
        }
    )
}
