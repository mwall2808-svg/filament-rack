package com.example.thefilamentrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thefilamentrack.SpoolViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogUsageScreen(
    viewModel: SpoolViewModel,
    spoolId: Int,
    onDone: () -> Unit
) {
    val spools by viewModel.allSpools.collectAsStateWithLifecycle()
    val spool = spools.find { it.id == spoolId }

    var usageInput by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    if (spool == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val pct = if (spool.totalWeight > 0f) spool.remainingWeight / spool.totalWeight else 0f
    val dotColor = colorFromHex(spool.colorHex)
    val progressColor = progressColorFor(pct)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Usage") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spool info card
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            spool.brand.ifEmpty { "Unknown brand" },
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${spool.material} · ${spool.colorName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { pct },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = progressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            "${spool.remainingWeight.toInt()}g of ${spool.totalWeight.toInt()}g remaining",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Usage input
            Text(
                "How much filament did you use?",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = usageInput,
                onValueChange = { usageInput = it },
                label = { Text("Grams used") },
                suffix = { Text("g") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = usageInput.isNotEmpty() && usageInput.toFloatOrNull() == null
            )

            // Quick amount buttons
            Text(
                "Quick amounts:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("10", "25", "50", "100").forEach { amount ->
                    FilterChip(
                        selected = usageInput == amount,
                        onClick = { usageInput = amount },
                        label = { Text("${amount}g") }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Success message
            if (saved) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "✓ Usage saved! ${spool.remainingWeight.toInt()}g remaining",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Save button
            Button(
                onClick = {
                    val used = usageInput.toFloatOrNull() ?: 0f
                    if (used > 0f) {
                        viewModel.updateSpool(
                            spool.copy(
                                remainingWeight = (spool.remainingWeight - used)
                                    .coerceAtLeast(0f)
                            )
                        )
                        saved = true
                        usageInput = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = usageInput.toFloatOrNull() != null && (usageInput.toFloatOrNull() ?: 0f) > 0f
            ) {
                Text("Save Usage")
            }

            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}