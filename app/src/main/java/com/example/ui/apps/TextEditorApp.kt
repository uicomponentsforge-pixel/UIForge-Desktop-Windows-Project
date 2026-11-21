package com.example.ui.apps

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TextDocumentEntity
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LocalDesktopPalette
import com.example.ui.theme.PlusJakartaSansFamily

@Composable
fun TextEditorApp(
    documents: List<TextDocumentEntity>,
    requestedDocumentId: Long?,
    onConsumeRequestedDoc: () -> Unit,
    onSaveDocument: (id: Long, title: String, content: String, onSaved: (Long) -> Unit) -> Unit,
    onDeleteDocument: (id: Long, title: String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val palette = LocalDesktopPalette.current

    var activeDocId by remember { mutableLongStateOf(0L) }
    var docTitle by remember { mutableStateOf("Untitled.txt") }
    var docContent by remember { mutableStateOf("") }
    var isDirty by remember { mutableStateOf(false) }
    var useMonospace by remember { mutableStateOf(true) }

    // Load initial document or requested document from Workspace folder
    LaunchedEffect(requestedDocumentId, documents) {
        if (requestedDocumentId != null) {
            val found = documents.firstOrNull { it.id == requestedDocumentId }
            if (found != null) {
                activeDocId = found.id
                docTitle = found.title
                docContent = found.content
                isDirty = false
            }
            onConsumeRequestedDoc()
        } else if (activeDocId == 0L && !isDirty && docContent.isEmpty() && documents.isNotEmpty()) {
            val first = documents.first()
            activeDocId = first.id
            docTitle = first.title
            docContent = first.content
            isDirty = false
        }
    }

    val openTextLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: ""
                val name = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "Imported.txt" } ?: "Imported.txt"
                activeDocId = 0L
                docTitle = name
                docContent = text
                isDirty = true
                onShowMessage("Loaded \"$name\" into editor")
            } catch (e: Exception) {
                onShowMessage("Could not open file: ${e.localizedMessage ?: "Unsupported format"}")
            }
        }
    }

    val exportTextLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(docContent) }
                onShowMessage("Exported \"$docTitle\" to device storage")
            } catch (e: Exception) {
                onShowMessage("Export failed: ${e.localizedMessage ?: "Permission denied"}")
            }
        }
    }

    val wordsCount = remember(docContent) {
        val trimmed = docContent.trim()
        if (trimmed.isEmpty()) 0 else trimmed.split(Regex("\\s+")).size
    }
    val linesCount = remember(docContent) {
        if (docContent.isEmpty()) 1 else docContent.count { it == '\n' } + 1
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Sidebar: Local Documents
        Column(
            modifier = Modifier
                .width(175.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Documents", style = MaterialTheme.typography.labelLarge)
                IconButton(
                    onClick = {
                        activeDocId = 0L
                        docTitle = "Note_${documents.size + 1}.txt"
                        docContent = ""
                        isDirty = false
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "New Document", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(documents, key = { it.id }) { doc ->
                    val selected = doc.id == activeDocId
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selected) palette.accent.copy(alpha = 0.2f)
                                else Color.Transparent
                            )
                            .clickable {
                                activeDocId = doc.id
                                docTitle = doc.title
                                docContent = doc.content
                                isDirty = false
                            }
                            .padding(horizontal = 8.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = null,
                            tint = if (selected) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { openTextLauncher.launch(arrayOf("text/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.FileOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open File", style = MaterialTheme.typography.labelSmall)
            }
        }

        VerticalDivider(color = palette.borderSubtle.copy(alpha = 0.5f))

        // Right: Editor Surface
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(10.dp)
        ) {
            // Toolbar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = docTitle,
                    onValueChange = {
                        docTitle = it
                        isDirty = true
                    },
                    singleLine = true,
                    label = { Text(if (isDirty) "Filename • Unsaved" else "Filename") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("editor_title_input")
                )

                Button(
                    onClick = {
                        onSaveDocument(activeDocId, docTitle, docContent) { savedId ->
                            activeDocId = savedId
                            isDirty = false
                        }
                    },
                    modifier = Modifier.testTag("editor_save_button")
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }

                IconButton(
                    onClick = { useMonospace = !useMonospace },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = "Toggle Monospace Font",
                        tint = if (useMonospace) palette.accent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        exportTextLauncher.launch(docTitle.ifBlank { "document.txt" })
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = "Export Text File")
                }

                if (activeDocId != 0L) {
                    IconButton(
                        onClick = {
                            onDeleteDocument(activeDocId, docTitle)
                            activeDocId = 0L
                            docTitle = "Untitled.txt"
                            docContent = ""
                            isDirty = false
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete Document",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Main Text Area
            OutlinedTextField(
                value = docContent,
                onValueChange = {
                    docContent = it
                    isDirty = true
                },
                placeholder = { Text("Start typing your document here…") },
                textStyle = TextStyle(
                    fontFamily = if (useMonospace) JetBrainsMonoFamily else PlusJakartaSansFamily,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("editor_body_input")
            )

            // Status Bar
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (useMonospace) "Font: JetBrains Mono" else "Font: Plus Jakarta Sans",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$linesCount lines  •  $wordsCount words  •  ${docContent.length} chars",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
