package com.pandeyganesha.kaamsutra.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class MissedTaskWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = DatabaseProvider.getDatabase(applicationContext)
        val taskDao = db.taskDao()
        val taskLogDao = db.taskLogDao()

        val dayJustEnded = LocalDate.now().minusDays(1).toString()

        val activeTasks = taskDao.getActiveTasksOnce()
        val yesterdaysLogs = taskLogDao.getLogsForDateOnce(dayJustEnded)

        val missedTasks = activeTasks.filter { task ->
            yesterdaysLogs.none { it.taskId == task.id && it.pointsAwarded > 0 }
        }

        missedTasks.forEach { task ->
            taskLogDao.insertLog(
                TaskLog(
                    taskId = task.id,
                    date = dayJustEnded,
                    pointsAwarded = -task.worthDelta
                )
            )
        }

        return Result.success()
    }
}

fun scheduleMissedTaskSettlement(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<MissedTaskWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateDelayUntil(0, 5), TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "missed_task_settlement",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}