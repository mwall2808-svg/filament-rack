package com.example.thefilamentrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thefilamentrack.SpoolViewModel
import com.example.thefilamentrack.data.SpoolEntity
import com.example.thefilamentrack.nfc.NfcManager
import kotlinx.coroutines.flow.first

private val MATERIALS = listOf("PLA", "PLA+", "PETG", "ABS", "ASA", "TPU", "Nylon", "Other")

private val PRESET_COLORS = listOf(
    "White" to "#F0F0F0", "Black" to "#1C1C1C", "Gray" to "#909090",
    "Red" to "#E03E3E", "Orange" to "#E8832A", "Yellow" to "#D4A017",
    "Green" to "#2E9E5E", "Teal" to "#1A8A85", "Blue" to "#2D6FBD",
    "Purple" to "#7B4BC4", "Pink" to "#D45A8A", "Silver" to "#B0B8C4"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSpoolScreen(
    viewModel: SpoolViewModel,
    spoolId: Int?,
    onBack: () -> Unit
) {
    val spools by viewModel.allSpools.collectAsStateWithLifecycle()
    val existing = spoolId?.let { id -> spools.find { it.id == id } }

    var brand by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("PLA") }
    var colorName by remember { mutableStateOf("White") }
    var colorHex by remember { mutableStateOf("#F0F0F0") }
    var totalWeight by remember { mutableStateOf("1000") }
    var remainingWeight by remember { mutableStateOf("1000") }
    var notes by remember { mutableStateOf("") }
    var linkedTagId by remember { mutableStateOf<String?>(null) }
    var fieldsLoaded by remember { mutableStateOf(false) }

    // Populate fields once the spool loads from the DB (allSpools starts as emptyList)
    LaunchedEffect(existing) {
        if (existing != null && !fieldsLoaded) {
            brand = existing.brand
            material = existing.material
            colorName = existing.colorName
            colorHex = existing.colorHex
            totalWeight = existing.totalWeight.toString()
            remainingWeight = existing.remainingWeight.toString()
            notes = existing.notes
            linkedTagId = existing.nfcTagId
            fieldsLoaded = true
        }
    }

    var materialExpanded by remember { mutableStateOf(false) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var awaitingNfc by remember { mutableStateOf(false) }

    // Suspend waiting for the next tag scan when the user requests NFC link
    LaunchedEffect(awaitingNfc) {
        if (!awaitingNfc) return@LaunchedEffect
        val tag = viewModel.nfcTag.first()
        linkedTagId = NfcManager.getTagId(tag)
        awaitingNfc = false
        showLinkDialog = false
    }

    val totalG = totalWeight.toFloatOrNull()
    val remainingG = remainingWeight.toFloatOrNull()
    val remainingError = totalG != null && remainingG != null && remainingG > totalG
    val canSave = totalG != null && remainingG != null && !remainingError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (spoolId == null) "Add Spool" else "Edit Spool") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Brand") },
                placeholder = { Text("e.g. Prusament, Bambu Lab") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = materialExpanded,
                onExpandedChange = { materialExpanded = it }
            ) {
                OutlinedTextField(
                    value = material,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Material") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(materialExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = materialExpanded,
                    onDismissRequest = { materialExpanded = false }
                ) {
                    MATERIALS.forEach { mat ->
                        DropdownMenuItem(
                            text = { Text(mat) },
                            onClick = { material = mat; materialExpanded = false }
                        )
                    }
                }
            }

            Text("Color", style = MaterialTheme.typography.labelLarge)
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(96.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PRESET_COLORS) { (name, hex) ->
                    val c = colorFromHex(hex)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(
                                if (colorHex == hex)
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                else Modifier
                            )
                            .clickable { colorName = name; colorHex = hex }
                    )
                }
            }
            OutlinedTextField(
                value = colorName,
                onValueChange = { colorName = it },
                label = { Text("Color name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = totalWeight,
                    onValueChange = { totalWeight = it },
                    label = { Text("Total weight") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = totalWeight.isNotEmpty() && totalG == null
                )
                OutlinedTextField(
                    value = remainingWeight,
                    onValueChange = { remainingWeight = it },
                    label = { Text("Remaining") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = remainingError || (remainingWeight.isNotEmpty() && remainingG == null)
                )
            }
            if (remainingError) {
                Text(
                    "Remaining cannot exceed total weight",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider()
            Text("NFC Tag", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (linkedTagId != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Nfc,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                "✓ Tag Linked",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "ID: $linkedTagId",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    awaitingNfc = true
                    showLinkDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Nfc, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (linkedTagId != null) "Re-link NFC Tag" else "Link NFC Tag")
            }

            Button(
                onClick = {
                    val entity = SpoolEntity(
                        id = existing?.id ?: 0,
                        brand = brand.trim(),
                        material = material,
                        colorName = colorName.trim(),
                        colorHex = colorHex,
                        totalWeight = totalG ?: 1000f,
                        remainingWeight = remainingG ?: 1000f,
                        notes = notes.trim(),
                        nfcTagId = linkedTagId
                    )
                    if (existing == null) viewModel.addSpool(entity)
                    else viewModel.updateSpool(entity)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave
            ) {
                Text(if (spoolId == null) "Add Spool" else "Save Changes")
            }
        }
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = {
                showLinkDialog = false
                awaitingNfc = false
            },
            icon = { Icon(Icons.Default.Nfc, contentDescription = null) },
            title = { Text("Link NFC Tag") },
            text = { Text("Hold your phone near the tag now...") },
            confirmButton = {
                TextButton(onClick = {
                    showLinkDialog = false
                    awaitingNfc = false
                }) { Text("Cancel") }
            }
        )
    }
}
