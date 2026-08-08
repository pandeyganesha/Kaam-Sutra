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
import com.pandeyganesha.kaamsutra.ui.components.Screen
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import com.pandeyganesha.kaamsutra.data.TaskLog
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.pandeyganesha.kaamsutra.data.scheduleTestNotification
import com.pandeyganesha.kaamsutra.data.scheduleMissedTaskSettlement
import com.pandeyganesha.kaamsutra.ui.components.AppBottomBar
import com.pandeyganesha.kaamsutra.ui.components.HomeScreen
import com.pandeyganesha.kaamsutra.ui.components.TasksScreen


class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            // optional: handle the user's response here, if you want
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        scheduleTestNotification(applicationContext)
        scheduleMissedTaskSettlement(applicationContext)
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
    val taskLogDao = db.taskLogDao()
    val coroutineScope = rememberCoroutineScope()
    val activeTasks by taskDao.getActiveTasks().collectAsState(initial = emptyList())
    val existingTaskNames = remember(activeTasks) { activeTasks.map { it.name }.toSet() }
    var showDialog by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var taskBeingDeleted by remember { mutableStateOf<Task?>(null) }
    val netWorth by taskLogDao.getNetWorth().collectAsState(initial = 0)
    val today = remember { LocalDate.now().toString() }
    val allTaskLogsForToday  by taskLogDao.getLogsForDate(today).collectAsState(initial = emptyList())
    var currentScreen by remember {mutableStateOf(Screen.HOME)}


    Scaffold(
        bottomBar = {
            AppBottomBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it },
                onAddClick = { showDialog = true }
            )
        },
    ) { innerPadding ->
        when (currentScreen) {
            Screen.HOME -> HomeScreen(
                netWorth = netWorth ?:0,
                modifier = Modifier.padding(innerPadding)
            )
            Screen.TASKS -> TasksScreen(
                activeTasks = activeTasks,
                allTaskLogsForToday = allTaskLogsForToday,
                onCheckedChange = { checked, task ->
                    coroutineScope.launch {
                        taskLogDao.upsertLog(
                            TaskLog(
                                taskId = task.id,
                                date = today,
                                pointsAwarded = if (checked) task.worthDelta else 0
                            )
                        )
                    }
                },
                onEditClicked = { task -> taskBeingEdited = task },
                onDeleteClicked = { task -> taskBeingDeleted = task},
                modifier = Modifier.padding(innerPadding)
            )
            Screen.GOALS -> {
                // placeholder for now
            }
            Screen.SETTINGS -> {
                // placeholder for now
            }
        }
    }
    taskBeingDeleted?.let { task ->
        DeleteTaskDialog(
            taskName = task.name,
            onDismiss = { taskBeingDeleted = null },
            onConfirm = {
                coroutineScope.launch {
                    taskDao.softDeleteTask(task.copy(isActive = false))
                    taskBeingDeleted = null
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