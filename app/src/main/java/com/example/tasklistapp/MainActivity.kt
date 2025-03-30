package com.example.tasklistapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tasklistapp.ui.theme.TaskListAppTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskListAppTheme {
                TaskListScreen()
            }
        }
    }
}

@Composable
fun TaskListScreen() {
    val context = LocalContext.current



    // State Hoisting: Elevamos el estado para gestionar las tareas y las imágenes
    val tasks = remember { mutableStateListOf<Task>() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Solicitar permisos en el inicio
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    //Delete task function
    val onDeleteTask: (Task) -> Unit = {task->
        tasks.remove(task)
    }

    //Edit task function
    val onEditTask:(Task, String) -> Unit = {task, newTitle ->
        val index = tasks.indexOf(task)
        if(index != -1){
            tasks[index] = task.copy(title=newTitle)
        }
    }

    TaskListContent(
        tasks = tasks,
        onAddTask = { title, imageUri ->
            tasks.add(Task(title, imageUri))
        },
        onDeleteTask = onDeleteTask,
        onEditTask = onEditTask
    )
}

@Composable
fun TaskListContent(
    tasks: List<Task>,
    onAddTask: (String, String?) -> Unit,
    onDeleteTask:(Task) -> Unit,
    onEditTask: (Task, String) -> Unit
) {
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Task List", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newTaskTitle,
            onValueChange = { newTaskTitle = it },
            label = { Text("Task Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500),
                    contentColor = Color.White
                )) {
                Text("Pick Image")
            }
            Button(
                onClick = {
                    if (newTaskTitle.isNotEmpty()) {
                        onAddTask(newTaskTitle, selectedImageUri)
                        newTaskTitle = ""
                        selectedImageUri = null
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA500),
                    contentColor = Color.White
                )
            ) {
                Text("Add Task")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TaskList(tasks=tasks, onDeleteTask=onDeleteTask, onEditTask =onEditTask)
    }
}
//Using a LazyColumn to display the task list MISSING
@Composable
fun TaskList(tasks: List<Task>, onDeleteTask: (Task) -> Unit, onEditTask: (Task, String) -> Unit) {

    Column {
        tasks.forEach { task ->
            TaskItem(task = task, onDeleteTask = onDeleteTask, onEditTask = onEditTask)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
fun TaskItem(task: Task, onDeleteTask: (Task) -> Unit, onEditTask: (Task, String) -> Unit) {
    var grayscale by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false)}
    var newTitle by remember { mutableStateOf(task.title)}

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { grayscale = !grayscale },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = task.title, style = MaterialTheme.typography.bodyLarge)

        task.imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                colorFilter = if (grayscale) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null
            )
        }

        //Bin icon
        IconButton(onClick = {onDeleteTask(task)}){
            Icon(imageVector = Icons.Default.Delete, contentDescription="Delete Task")
        }
        //Edit icon
        IconButton(onClick={showEditDialog=true}){
            Icon(imageVector= Icons.Default.Edit, contentDescription="Edit Task")
        }

    }

    if(showEditDialog){
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {Text("Edit Task")},
            text = {
                OutlinedTextField(value = newTitle, onValueChange = {newTitle = it},
                    label= {Text("Task title")}
                )
            },
            confirmButton = {
                Button(onClick = {
                    onEditTask(task, newTitle)
                    showEditDialog = false
                }){
                    Text("Save changes")
                }
            },
            dismissButton = {
                Button(onClick={
                    showEditDialog = false
                }){
                    Text("Cancel")
                }
            }
            )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TaskListAppTheme {
        TaskListScreen()
    }
}

data class Task(val title: String, val imageUri: String?)