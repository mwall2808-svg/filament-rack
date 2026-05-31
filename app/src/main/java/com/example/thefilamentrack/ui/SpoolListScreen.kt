package com.example.thefilamentrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thefilamentrack.SpoolViewModel
import com.example.thefilamentrack.data.SpoolEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpoolListScreen(
    viewModel: SpoolViewModel,
    onAddSpool: () -> Unit,
    onSpoolClick: (Int) -> Unit
) {
    val spools by viewModel.allSpools.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filament Tracker") },
                actions = {
                    Text(
                        "Tap tag to log usage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSpool) {
                Icon(Icons.Default.Add, contentDescription = "Add Spool")
            }
        }
    ) { padding ->
        if (spools.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("No spools yet.", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tap + to add your first spool",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(spools, key = { it.id }) { spool ->
                    SpoolCard(spool = spool, onClick = { onSpoolClick(spool.id) })
                }
            }
        }
    }
}

@Composable
fun SpoolCard(spool: SpoolEntity, onClick: () -> Unit) {
    val pct = if (spool.totalWeight > 0f) spool.remainingWeight / spool.totalWeight else 0f
    val dotColor = colorFromHex(spool.colorHex)
    val progressColor = progressColorFor(pct)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(dotColor))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        spool.brand.ifEmpty { "Unknown brand" },
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${(pct * 100).toInt()}%",
                        fontWeight = FontWeight.Medium,
                        color = progressColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    "${spool.material} · ${spool.colorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (spool.nfcTagId != null) {
                    Text(
                        "✓ NFC linked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    "${spool.remainingWeight.toInt()}g of ${spool.totalWeight.toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}