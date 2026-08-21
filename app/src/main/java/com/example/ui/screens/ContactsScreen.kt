package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.EmergencyContact
import com.example.ui.theme.BeaconCyan
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SignalRed
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(viewModel: MainViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val stealthDarkMode by viewModel.stealthDarkModeActive.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContact?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SignalRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TRUSTED CONTACTS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Emergency Dispatch & Escalation Ladder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No trusted contacts added yet",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap + to add your family or close friends",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Group by Escalation Tier
                    val tier1 = contacts.filter { it.escalationTier == 1 }
                    val tier2 = contacts.filter { it.escalationTier == 2 }
                    val tier3 = contacts.filter { it.escalationTier == 3 }

                    if (tier1.isNotEmpty()) {
                        item {
                            TierHeader(
                                title = "Tier 1: Instant Silent SMS Dispatch",
                                badgeColor = SignalRed
                            )
                        }
                        items(tier1) { contact ->
                            ContactCard(
                                contact = contact,
                                stealthDarkMode = stealthDarkMode,
                                onEdit = {
                                    editingContact = contact
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteContact(contact) }
                            )
                        }
                    }

                    if (tier2.isNotEmpty()) {
                        item {
                            TierHeader(
                                title = "Tier 2: Automatic Call Escalation",
                                badgeColor = WarningAmber
                            )
                        }
                        items(tier2) { contact ->
                            ContactCard(
                                contact = contact,
                                stealthDarkMode = stealthDarkMode,
                                onEdit = {
                                    editingContact = contact
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteContact(contact) }
                            )
                        }
                    }

                    if (tier3.isNotEmpty()) {
                        item {
                            TierHeader(
                                title = "Tier 3: Broad Panic Broadcast",
                                badgeColor = BeaconCyan
                            )
                        }
                        items(tier3) { contact ->
                            ContactCard(
                                contact = contact,
                                stealthDarkMode = stealthDarkMode,
                                onEdit = {
                                    editingContact = contact
                                    showDialog = true
                                },
                                onDelete = { viewModel.deleteContact(contact) }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                editingContact = null
                showDialog = true
            },
            containerColor = SignalRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Contact")
        }
    }

    if (showDialog) {
        AddEditContactDialog(
            existingContact = editingContact,
            onDismiss = { showDialog = false },
            onSave = { contact ->
                if (editingContact == null) {
                    viewModel.addContact(contact)
                } else {
                    viewModel.updateContact(contact)
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun TierHeader(title: String, badgeColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(badgeColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ContactCard(
    contact: EmergencyContact,
    stealthDarkMode: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val displayName = if (stealthDarkMode) "${contact.name.take(1)}•••••" else contact.name
    val displayPhone = if (stealthDarkMode) "•••• •••• ${contact.phoneNumber.takeLast(4)}" else contact.phoneNumber

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (contact.isPrimary) SignalRed.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (contact.escalationTier == 2) Icons.Default.Call else Icons.Default.Sms,
                    contentDescription = null,
                    tint = if (contact.isPrimary) SignalRed else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (contact.isPrimary) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Primary",
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = "${contact.relationship} • $displayPhone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SignalRed.copy(alpha = 0.8f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditContactDialog(
    existingContact: EmergencyContact?,
    onDismiss: () -> Unit,
    onSave: (EmergencyContact) -> Unit
) {
    var name by remember { mutableStateOf(existingContact?.name ?: "") }
    var phone by remember { mutableStateOf(existingContact?.phoneNumber ?: "") }
    var relation by remember { mutableStateOf(existingContact?.relationship ?: "Family") }
    var isPrimary by remember { mutableStateOf(existingContact?.isPrimary ?: false) }
    var tier by remember { mutableIntStateOf(existingContact?.escalationTier ?: 1) }

    var expandedTierDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingContact == null) "Add Trusted Contact" else "Edit Contact",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (+Country Code)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Relationship (e.g., Mom, Friend, Partner)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Tier Selection
                ExposedDropdownMenuBox(
                    expanded = expandedTierDropdown,
                    onExpandedChange = { expandedTierDropdown = !expandedTierDropdown }
                ) {
                    OutlinedTextField(
                        value = "Tier $tier: " + when (tier) {
                            1 -> "Primary Silent SMS"
                            2 -> "Call Escalation"
                            else -> "Broadcast Panic"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Escalation Tier") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTierDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedTierDropdown,
                        onDismissRequest = { expandedTierDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tier 1: Instant Silent SMS") },
                            onClick = { tier = 1; expandedTierDropdown = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Tier 2: Automatic Phone Call") },
                            onClick = { tier = 2; expandedTierDropdown = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Tier 3: Broadcast Emergency") },
                            onClick = { tier = 3; expandedTierDropdown = false }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Mark as Primary Guardian",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isPrimary,
                        onCheckedChange = { isPrimary = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(
                            EmergencyContact(
                                id = existingContact?.id ?: 0,
                                name = name,
                                phoneNumber = phone,
                                relationship = relation,
                                isPrimary = isPrimary,
                                escalationTier = tier
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SignalRed)
            ) {
                Text("Save Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
