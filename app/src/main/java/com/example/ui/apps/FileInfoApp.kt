package com.example.ui.apps

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.TextDocumentEntity
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalDesktopPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class InspectedFileMetadata(
    val name: String,
    val mimeType: String,
    val sizeBytes: Long?,
    val sourceUri: String,
    val previewSnippet: String?
)

@Composable
fun FileInfoApp(documents: List<TextDocumentEntity>) {
    val context = LocalContext.current
    val palette = LocalDesktopPalette.current
    var metadata by remember { mutableStateOf<InspectedFileMetadata?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            errorText = null
            var displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Unknown File"
            var sizeBytes: Long? = null

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) {
                        displayName = cursor.getString(nameIdx) ?: displayName
                    }
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) {
                        sizeBytes = cursor.getLong(sizeIdx)
                    }
                }
            }

            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
            val snippet = if (mimeType.startsWith("text/") || displayName.endsWith(".txt") || displayName.endsWith(".json") || displayName.endsWith(".md")) {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                    reader.readText().take(600)
                }
            } else {
                null
            }

            metadata = InspectedFileMetadata(
                name = displayName,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                sourceUri = uri.toString(),
                previewSnippet = snippet
            )
        } catch (e: Exception) {
            errorText = "Unable to inspect file: ${e.localizedMessage ?: "Access restricted"}"
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text("File Inspector", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Select any file via Android Storage Access Framework",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { pickerLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.testTag("pick_file_button")
            ) {
                Icon(Icons.Rounded.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select File")
            }
        }

        if (errorText != null) {
            Text(
                text = errorText!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (metadata != null) {
            val info = metadata!!
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(14.dp)
            ) {
                MetadataRow("File Name", info.name)
                MetadataRow("MIME / Type", info.mimeType)
                MetadataRow("File Size", info.sizeBytes?.let { formatBytes(it) } ?: "Unknown")
                MetadataRow("Source URI", info.sourceUri)

                if (!info.previewSnippet.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Text Preview", style = MaterialTheme.typography.labelMedium, color = palette.accent)
                    Text(
                        text = info.previewSnippet,
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(10.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.InsertDriveFile,
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose a device file above or click a local workspace file below",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (documents.isNotEmpty()) {
            Text(
                text = "Quick Inspect: Local Workspace Documents",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            documents.take(4).forEach { doc ->
                val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable {
                            metadata = InspectedFileMetadata(
                                name = doc.title,
                                mimeType = "text/plain (UIForge Local Store)",
                                sizeBytes = doc.content.toByteArray().size.toLong(),
                                sourceUri = "uiforge://documents/${doc.id} (Updated ${fmt.format(Date(doc.updatedAtMillis))})",
                                previewSnippet = doc.content.take(600)
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Text(doc.title, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = formatBytes(doc.content.toByteArray().size.toLong()),
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.accent
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontFamily = JetBrainsMonoFamily,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB (%d B)", kb, bytes)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.2f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.US, "%.2f GB", gb)
}
