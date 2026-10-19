package com.vozbarrial.core

import android.content.Context
import android.util.Patterns
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray

object RememberedEmailCache {
    private const val PREFERENCES_NAME = "email_history"
    private const val EMAILS_KEY = "emails"

    fun remember(context: Context, value: String) {
        val email = value.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return

        val emails = load(context).filterNot { it.equals(email, ignoreCase = true) }
        val updated = listOf(email) + emails
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(EMAILS_KEY, JSONArray(updated).toString())
            .apply()
    }

    fun suggestions(context: Context, query: String): List<String> {
        val part = query.trim()
        if (part.isEmpty()) return emptyList()
        return load(context)
            .filter { it.contains(part, ignoreCase = true) && !it.equals(part, ignoreCase = true) }
            .take(5)
    }

    private fun load(context: Context): List<String> {
        val stored = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(EMAILS_KEY, null) ?: return emptyList()
        val json = runCatching { JSONArray(stored) }.getOrNull() ?: return emptyList()
        return (0 until json.length())
            .map { json.optString(it).trim() }
            .filter { it.isNotEmpty() }
    }
}

@Composable
fun RememberedEmailSuggestions(
    query: String,
    onSelect: (String) -> Unit,
) {
    val context = LocalContext.current
    val matches = remember(context, query) {
        RememberedEmailCache.suggestions(context, query)
    }
    if (matches.isNotEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(9.dp),
            shadowElevation = 2.dp,
        ) {
            Column {
                Text(
                    "Usados anteriormente",
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                matches.forEach { email ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(email) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                    ) {
                        Text(email, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}