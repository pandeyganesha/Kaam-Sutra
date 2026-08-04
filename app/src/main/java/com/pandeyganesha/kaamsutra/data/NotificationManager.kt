package com.pandeyganesha.kaamsutra.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // 1. Get arguments passed to the worker
        val title = inputData.getString("NOTIFICATION_TITLE") ?: "Task Complete"
        val message = inputData.getString("NOTIFICATION_MSG") ?: "Your background task finished!"

        // 2. Trigger the notification
        showNotification(title, message)

        // 3. Indicate whether the work finished successfully
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "work_manager_channel"
        val notificationId = 1

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel if using Android 8.0 (Oreo) or higher
        val channel = NotificationChannel(
            channelId,
            "Background Tasks",
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