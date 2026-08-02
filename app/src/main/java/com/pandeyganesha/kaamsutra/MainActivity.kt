package com.pandeyganesha.kaamsutra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.pandeyganesha.kaamsutra.ui.theme.KaamSutraTheme
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.collections.emptyList
import androidx.compose.runtime.collectAsState
import com.pandeyganesha.kaamsutra.data.DatabaseProvider
import com.pandeyganesha.kaamsutra.data.Task
import kotlinx.coroutines.launch
import com.pandeyganesha.kaamsutra.ui.components.AddTaskDialog
import com.pandeyganesha.kaamsutra.ui.components.DeleteTaskDialog
import com.pandeyganesha.kaamsutra.ui.components.NetWorthCard
import com.pandeyganesha.kaamsutra.ui.components.TaskRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaamSutraTheme {
                KaamSutraApp()
            }
        }
    }
}

@Composable
fun KaamSutraApp() {

    val context = LocalContext.current
    val db = DatabaseProvider.getDatabase(context.applicationContext)
    val taskDao = db.taskDao()
    val coroutineScope = rememberCoroutineScope()
    val activeTasks by taskDao.getActiveTasks().collectAsState(initial = emptyList())
    val existingTaskNames = remember(activeTasks) { activeTasks.map { it.name }.toSet() }
    var showDialog by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var deleteTask by remember { mutableStateOf<Task?>(null) }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            NetWorthCard(0)
            activeTasks.forEach { task ->
                TaskRow(
                    taskName = task.name,
                    worthDelta = task.worthDelta,
                    isChecked = false,
                    onCheckedChange = {},
                    onEditClick = { taskBeingEdited = task },
                    onDeleteClick = { deleteTask = task }
                )
            }
        }
    }
    deleteTask?.let { task ->
        DeleteTaskDialog(
            taskName = task.name,
            onDismiss = { deleteTask = null },
            onConfirm = {
                coroutineScope.launch {
                    taskDao.softDeleteTask(task.copy(isActive = false))
                    deleteTask = null
                }
            }
        )
    }
    taskBeingEdited?.let { task ->
        AddTaskDialog(
            taskName = task.name,
            worthDelta = task.worthDelta.toString(),
            existingTaskNames = existingTaskNames - task.name,
            onDismiss = {
                taskBeingEdited = null
            },
            onConfirm = { taskName, worthDelta ->
                coroutineScope.launch {
                    taskDao.updateTask(task.copy(name = taskName, worthDelta = worthDelta))
                    taskBeingEdited = null
                }
            }
        )

    }
    if (showDialog) {
        AddTaskDialog(
            existingTaskNames = existingTaskNames,
            onDismiss = { showDialog = false },
            onConfirm = { taskName, worthDelta ->
                coroutineScope.launch {
                    taskDao.insertTask(Task(name = taskName, worthDelta = worthDelta))
                }
                showDialog = false
            })
    }
}