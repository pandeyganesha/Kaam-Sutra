package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column



@Composable
fun AddTaskDialog(
    taskName: String = "",
    worthDelta: String = "",
    existingTaskNames: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (taskName: String, worthDelta: Int) -> Unit,
) {
    var taskNameText by remember { mutableStateOf(taskName) }
    var worthDeltaText by remember { mutableStateOf(worthDelta) }
    val isDuplicate = taskNameText in existingTaskNames

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task")},
        text = {
            Column {
                OutlinedTextField(
                    value = taskNameText,
                    onValueChange = { taskNameText = it },
                    label = {Text("Task Name")}
                )
                if (isDuplicate) {
                    Text(
                        text = "Task name already exists",
                        color = Color.Red
                    )
                }
                OutlinedTextField(
                    value = worthDeltaText,
                    onValueChange = { worthDeltaText = it },
                    label = { Text("Worth") }
                )
            }
        },
        confirmButton = {

            TextButton(onClick = {
                onConfirm(taskNameText, worthDeltaText.toIntOrNull() ?: 0)
            },
                enabled = !isDuplicate
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}