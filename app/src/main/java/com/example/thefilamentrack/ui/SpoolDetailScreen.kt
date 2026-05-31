package com.example.thefilamentrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thefilamentrack.SpoolViewModel
import com.example.thefilamentrack.data.SpoolEntity
import com.example.thefilamentrack.nfc.NfcManager
import kotlinx.coroutines.flow.first

private enum class NfcWaitMode { None, Link, Write }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpoolDetailScreen(
    viewModel: SpoolViewModel,
    spoolId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit
) {
    val spools by viewModel.allSpools.collectAsStateWithLifecycle()
    val spool = spools.find { it.id == spoolId }

    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLinkNfcDialog by remember { mutableStateOf(false) }
    var showWriteNfcDialog by remember { mutableStateOf(false) }
    var showUsageDialog by remember { mutableStateOf(false) }
    var usageInput by remember { mutableStateOf("") }
    var nfcWaitMode by remember { mutableStateOf(NfcWaitMode.None) }

    if (spool == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Suspend waiting for the next tag scan; only active when nfcWaitMode != None
    LaunchedEffect(nfcWaitMode) {
        if (nfcWaitMode == NfcWaitMode.None) return@LaunchedEffect
        val tag = viewModel.nfcTag.first()
        val tagId = NfcManager.getTagId(tag)
        when (nfcWaitMode) {
            NfcWaitMode.Link -> {
                viewModel.linkNfcTag(spool.id, tagId)
                showLinkNfcDialog = false
                snackbarHostState.showSnackbar("Tag linked!", duration = SnackbarDuration.Short)
            }
            NfcWaitMode.Write -> {
                val success = NfcManager.writeSpoolId(tag, spool.id.toString())
                showWriteNfcDialog = false
                if (success) {
                    viewModel.linkNfcTag(spool.id, tagId)
                    snackbarHostState.showSnackbar("Tag written and linked!", duration = SnackbarDuration.Short)
                } else {
                    snackbarHostState.showSnackbar("Failed to write tag — tag may be read-only or incompatible.", duration = SnackbarDuration.Long)
                }
            }
            NfcWaitMode.None -> {}
        }
        nfcWaitMode = NfcWaitMode.None
    }

    val pct = if (spool.totalWeight > 0f) spool.remainingWeight / spool.totalWeight else 0f
    val dotColor = colorFromHex(spool.colorHex)
    val progressColor = progressColorFor(pct)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(spool.brand.ifEmpty { "Spool" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(spool.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Column {
                        Text(
                            spool.brand.ifEmpty { "Unknown brand" },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "${spool.material} · ${spool.colorName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        if (spool.nfcTagId != null) {
                            Text(
                                "✓ NFC Tag Linked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "ID: ${spool.nfcTagId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "No NFC tag linked",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Remaining", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${spool.remainingWeight.toInt()}g (${(pct * 100).toInt()}%)",
                            color = progressColor,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        "Total: ${spool.totalWeight.toInt()}g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (spool.notes.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(spool.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Button(
                onClick = { showUsageDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Usage")
            }

            Text(
                "NFC Tag",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        nfcWaitMode = NfcWaitMode.Link
                        showLinkNfcDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (spool.nfcTagId != null) "Re-link" else "Link Tag")
                }

                Button(
                    onClick = {
                        nfcWaitMode = NfcWaitMode.Write
                        showWriteNfcDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Write Tag")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete spool?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSpool(spool)
                    onBack()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showLinkNfcDialog) {
        AlertDialog(
            onDismissRequest = {
                showLinkNfcDialog = false
                nfcWaitMode = NfcWaitMode.None
            },
            icon = { Icon(Icons.Default.Nfc, contentDescription = null) },
            title = { Text("Link NFC Tag") },
            text = { Text("Hold your phone near the tag now...") },
            confirmButton = {
                TextButton(onClick = {
                    showLinkNfcDialog = false
                    nfcWaitMode = NfcWaitMode.None
                }) { Text("Cancel") }
            }
        )
    }

    if (showWriteNfcDialog) {
        AlertDialog(
            onDismissRequest = {
                showWriteNfcDialog = false
                nfcWaitMode = NfcWaitMode.None
            },
            icon = { Icon(Icons.Default.Nfc, contentDescription = null) },
            title = { Text("Write to NFC Tag") },
            text = { Text("Hold your phone near a writable NFC tag now...") },
            confirmButton = {
                TextButton(onClick = {
                    showWriteNfcDialog = false
                    nfcWaitMode = NfcWaitMode.None
                }) { Text("Cancel") }
            }
        )
    }

    if (showUsageDialog) {
        AlertDialog(
            onDismissRequest = { showUsageDialog = false; usageInput = "" },
            title = { Text("Log Usage") },
            text = {
                Column {
                    Text("How many grams did you use?")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = usageInput,
                        onValueChange = { usageInput = it },
                        label = { Text("Grams used") },
                        suffix = { Text("g") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val used = usageInput.toFloatOrNull() ?: 0f
                    if (used > 0f) {
                        viewModel.updateSpool(
                            spool.copy(
                                remainingWeight = (spool.remainingWeight - used).coerceAtLeast(0f)
                            )
                        )
                    }
                    showUsageDialog = false
                    usageInput = ""
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUsageDialog = false
                    usageInput = ""
                }) { Text("Cancel") }
            }
        )
    }
}
