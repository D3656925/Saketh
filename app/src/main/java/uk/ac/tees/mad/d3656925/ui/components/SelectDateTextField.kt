package uk.ac.tees.mad.d3656925.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTimeTextField(time: (Long) -> Unit) {
    var shouldDisplayDialog by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("") }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()

    val calendar = Calendar.getInstance()
    val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
    val initialMinute = calendar.get(Calendar.MINUTE)
    val datePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    LaunchedEffect(datePickerState.hour, datePickerState.minute) {
        val selectedCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, datePickerState.hour)
            set(Calendar.MINUTE, datePickerState.minute)
        }
        selectedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedCalendar.time)
        time(selectedCalendar.timeInMillis)
    }
    if (isPressed) {
        shouldDisplayDialog = true
    }
    if (shouldDisplayDialog) {
        TimePickerDialog(
            state = datePickerState,
            onDismissRequest = { shouldDisplayDialog = false }
        )
    }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        value = selectedTime,
        onValueChange = {},
        trailingIcon = {
            IconButton(onClick = { shouldDisplayDialog = true }) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Select Time"
                )
            }
        },
        label = { Text("Select Time") },
        interactionSource = interactionSource
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(state: TimePickerState, onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(
                state = state
            )
        }
    )
}