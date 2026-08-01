package com.pandeyganesha.kaamsutra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pandeyganesha.kaamsutra.ui.theme.KaamSutraTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import android.content.Context
import android.util.Log
import androidx.room.Room
import java.util.UUID
import androidx.room.Index
import androidx.compose.runtime.rememberCoroutineScope
import kotlin.collections.emptyList
import androidx.room.Update
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch


object DatabaseProvider {
    @Volatile private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build().also { instance = it }
        }
    }
}

@Entity(tableName = "net_worth")
data class NetWorth(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val netWorth: Int
)
@Dao
interface NetWorthDao {
    @Insert
    suspend fun saveNetWorth(netWorth: NetWorth)

    @Query("Select * from net_worth WHERE id = 0 LIMIT 1")
    fun getNetWorth(): Flow<NetWorth?>
}

@Entity(tableName = "task", indices = [Index(value = ["name"], unique = true)])
data class Task(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val worthDelta: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * from task order by createdAt")
    fun getTasks(): Flow<List<Task>>

    @Query("Select * from task where id = :taskId")
    fun getTask(taskId: String): Flow<Task?>
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaamSutraTheme {
                val db = DatabaseProvider.getDatabase(applicationContext)
                val taskDao = db.taskDao()
                val coroutineScope = rememberCoroutineScope()
                val allTasks by taskDao.getTasks().collectAsState(initial = emptyList())
                var showDialog by remember { mutableStateOf(false) }
                var taskBeingEdited by remember { mutableStateOf<Task?>(null)}


                Scaffold(modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showDialog = true}) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task")
                        }
                    }) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        NetWorthCard(0)
                        allTasks.forEach { task ->
                            TaskRow(
                                taskName = task.name,
                                worth = task.worthDelta,
                                isChecked = false,
                                onCheckedChange = {},
                                onEditClick = {
                                    taskBeingEdited = task},
                                onDeleteClick = {}
                            )
                        }
                    }
                }
                taskBeingEdited?.let { task ->
                    AddTaskDialog(
                        taskName = task.name,
                        worthDelta = task.worthDelta.toString(),
                        onDismiss = {
                            taskBeingEdited = null },
                        onConfirm = { taskName, worthDelta ->
                            coroutineScope.launch {
                                taskDao.updateTask(Task(id = task.id, name = taskName, worthDelta = worthDelta))
                                taskBeingEdited = null
                            }
                        }
                    )

                }
                if (showDialog){
                    AddTaskDialog(
                        onDismiss = {showDialog = false},
                        onConfirm = { taskName, worthDelta ->
                            coroutineScope.launch {
                                taskDao.insertTask(Task(name = taskName, worthDelta = worthDelta))
                            }
                            showDialog = false
                        })
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    taskName: String = "",
    worthDelta: String = "",
    onDismiss: () -> Unit,
    onConfirm: (taskName: String, worthDelta: Int) -> Unit,
) {
    var taskName by remember { mutableStateOf(taskName) }
    var worthDeltaText by remember { mutableStateOf(worthDelta) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Task")},
        text = {
            Column {
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = {Text("Task Name")}
                )
                OutlinedTextField(
                    value = worthDeltaText,
                    onValueChange = { worthDeltaText = it },
                    label = { Text("Worth") }
                )
            }
        },
        confirmButton = {

            TextButton(onClick = {
                val worthDelta = worthDeltaText.toIntOrNull() ?: 0
                onConfirm(taskName, worthDelta)
            }) {
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

@Composable
fun NetWorthCard(totalMoney: Int, modifier: Modifier = Modifier)
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Total Money")
            Text(text = "$totalMoney", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun TaskRow(
    taskName: String,
    worth: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically

    ){
        Checkbox(
            checked=isChecked,
            onCheckedChange=onCheckedChange
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(text = taskName)
            Text(text = "$worth pts")
        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KaamSutraTheme {
        Greeting("Android")
    }
}