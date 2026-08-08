package com.pandeyganesha.kaamsutra.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.data.Task
import com.pandeyganesha.kaamsutra.data.TaskLog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp

@Composable
fun TasksScreen(activeTasks: List<Task>,
                allTaskLogsForToday: List<TaskLog>,
                onCheckedChange: (Boolean, Task) -> Unit,
                onEditClicked: (Task) -> Unit,
                onDeleteClicked: (Task) -> Unit,
                modifier: Modifier
                ) {
    Column(modifier = modifier.fillMaxSize()) {
//        Text(
//            text = "Tasks",
//            style = MaterialTheme.typography.headlineSmall,
//            modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 15.dp)
//        )
        activeTasks.forEach { task ->
            TaskRow(
                taskName = task.name,
                worthDelta = task.worthDelta,
                isChecked = allTaskLogsForToday.any { it.taskId == task.id && it.pointsAwarded > 0 },
                onCheckedChange = { checked -> onCheckedChange(checked, task) },
                onEditClick = { onEditClicked(task) },
                onDeleteClick = { onDeleteClicked(task) }
            )
        }
    }
}