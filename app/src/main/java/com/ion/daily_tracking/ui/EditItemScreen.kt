package com.ion.daily_tracking.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ion.daily_tracking.data.ScheduleItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    viewModel: ScheduleViewModel,
    itemId: Long,
    onDone: () -> Unit,
) {
    val isNew = itemId <= 0L
    val viewedDay by viewModel.selectedDay.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var repeating by remember { mutableStateOf(false) }
    var carryOver by remember { mutableStateOf(false) }
    var epochDay by remember { mutableLongStateOf(viewedDay) }
    var startMinute by remember { mutableStateOf<Int?>(null) }
    var endMinute by remember { mutableStateOf<Int?>(null) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(isNew) }

    // Which picker dialog (if any) is open.
    var picker by remember { mutableStateOf(Picker.NONE) }

    LaunchedEffect(itemId) {
        if (!isNew) {
            viewModel.load(itemId)?.let { item ->
                title = item.title
                notes = item.notes
                repeating = item.repeating
                carryOver = item.carryOver
                epochDay = item.epochDay ?: viewedDay
                startMinute = item.startMinute.takeIf { it in 0..1439 }
                endMinute = item.endMinute.takeIf { it in 0..1439 }
                reminderEnabled = item.reminderEnabled
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New activity" else "Edit activity") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Activity title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Type", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !repeating && !carryOver,
                    onClick = { repeating = false; carryOver = false },
                    shape = SegmentedButtonDefaults.itemShape(0, 3),
                ) { Text("One-off") }
                SegmentedButton(
                    selected = carryOver,
                    onClick = { carryOver = true; repeating = false },
                    shape = SegmentedButtonDefaults.itemShape(1, 3),
                ) { Text("Until done") }
                SegmentedButton(
                    selected = repeating,
                    onClick = { repeating = true; carryOver = false },
                    shape = SegmentedButtonDefaults.itemShape(2, 3),
                ) { Text("Daily") }
            }

            if (carryOver) {
                Text(
                    "Stays on your list every day from its start date until you tick it off.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!repeating) {
                FieldRow(
                    label = if (carryOver) "Start date" else "Date",
                    value = formatDayHeading(epochDay),
                ) {
                    picker = Picker.DATE
                }
            }

            FieldRow(
                label = "Start time",
                value = startMinute?.let { formatMinute(it) } ?: "Not set",
            ) { picker = Picker.START }

            FieldRow(
                label = "End time",
                value = endMinute?.let { formatMinute(it) } ?: "Not set",
            ) { picker = Picker.END }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reminder notification", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (startMinute == null) "Set a start time to enable"
                        else "Notify me at the start time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = reminderEnabled && startMinute != null,
                    onCheckedChange = { reminderEnabled = it },
                    enabled = startMinute != null,
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val item = ScheduleItem(
                        id = if (isNew) 0L else itemId,
                        title = title.trim(),
                        notes = notes.trim(),
                        startMinute = startMinute ?: -1,
                        endMinute = endMinute ?: -1,
                        repeating = repeating,
                        epochDay = if (repeating) null else epochDay,
                        reminderEnabled = reminderEnabled && startMinute != null,
                        carryOver = carryOver,
                    )
                    viewModel.save(item)
                    onDone()
                },
                enabled = loaded && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }

            if (!isNew) {
                OutlinedButton(
                    onClick = {
                        viewModel.delete(
                            ScheduleItem(id = itemId, title = title, repeating = repeating)
                        )
                        onDone()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        }
    }

    when (picker) {
        Picker.DATE -> DatePickerSheet(
            initialEpochDay = epochDay,
            onPick = { epochDay = it; picker = Picker.NONE },
            onDismiss = { picker = Picker.NONE },
        )
        Picker.START -> TimePickerSheet(
            initialMinute = startMinute,
            onPick = { startMinute = it; picker = Picker.NONE },
            onClear = { startMinute = null; reminderEnabled = false; picker = Picker.NONE },
            onDismiss = { picker = Picker.NONE },
        )
        Picker.END -> TimePickerSheet(
            initialMinute = endMinute,
            onPick = { endMinute = it; picker = Picker.NONE },
            onClear = { endMinute = null; picker = Picker.NONE },
            onDismiss = { picker = Picker.NONE },
        )
        Picker.NONE -> Unit
    }
}

private enum class Picker { NONE, DATE, START, END }

@Composable
private fun FieldRow(label: String, value: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Start)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    initialEpochDay: Long,
    onPick: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialEpochDay * MILLIS_PER_DAY,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onPick(it / MILLIS_PER_DAY) } ?: onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerSheet(
    initialMinute: Int?,
    onPick: (Int) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = (initialMinute ?: 9 * 60) / 60,
        initialMinute = (initialMinute ?: 0) % 60,
        is24Hour = true,
    )
    DatePickerDialogContainer(
        onDismiss = onDismiss,
        onConfirm = { onPick(state.hour * 60 + state.minute) },
        onClear = onClear,
    ) {
        TimePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogContainer(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onClear: () -> Unit,
    content: @Composable () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onClear) { Text("Clear") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

private const val MILLIS_PER_DAY = 86_400_000L
