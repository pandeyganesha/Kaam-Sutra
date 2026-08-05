package com.pandeyganesha.kaamsutra.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.WorkerParameters
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = DatabaseProvider.getDatabase(applicationContext)
        val taskDao = db.taskDao()
        val taskLogDao = db.taskLogDao()
        val today = LocalDate.now().toString()
        val activeTasks = taskDao.getActiveTasksOnce()
        val todayLogs = taskLogDao.getLogsForDateOnce(today)

        val undoneTasks = activeTasks.filter { task -> todayLogs.none {it.taskId == task.id && it.done} }

        if (undoneTasks.isNotEmpty()) {
            val names = undoneTasks.joinToString(", ") { it.name }
            showNotification("Pending Tasks", names)
        }
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "work_manager_channel"
        val notificationId = 1

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel if using Android 8.0 (Oreo) or higher
        val channel = NotificationChannel(
            channelId,
            "Remaining Tasks",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications triggered by scheduled background jobs"
        }
        notificationManager.createNotificationChannel(channel)

        // Build the notification
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(com.pandeyganesha.kaamsutra.R.mipmap.ic_launcher) // System icon for illustration
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)


        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(notificationId, builder.build())
        }
    }
}

fun scheduleTestNotification(context: Context) {
    val data = workDataOf(
        "NOTIFICATION_TITLE" to "Test Reminder",
        "NOTIFICATION_MSG" to "This is a placeholder notification."
    )
    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInputData(data)
        .setInitialDelay(10, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}