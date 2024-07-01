package me.dio.copa.catar.notification.scheduler.extensions

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import me.dio.copa.catar.domain.model.MatchDomain
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

private const val NOTIFICATION_TITLE_KEY = "NOTIFICATION_TITLE_KEY"
private const val NOTIFICATION_CONTENT_KEY = "NOTIFICATION_CONTENT_KEY"

class NotificationMatcheWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val title = inputData.getString(NOTIFICATION_TITLE_KEY)
            ?: throw IllegalArgumentException("Missing notification title")
        val content = inputData.getString(NOTIFICATION_CONTENT_KEY)
            ?: throw IllegalArgumentException("Missing content")

        context.showNotification(title, content)

        return Result.success()
    }

    companion object {
        @OptIn(ExperimentalTime::class)
        fun start(context: Context, match: MatchDomain) {

            val inputData = workDataOf(
                NOTIFICATION_TITLE_KEY to "Começará em Breve",
                NOTIFICATION_CONTENT_KEY to "${match.team1.displayName} vs ${match.team2.displayName}"
            )

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    match.id,
                    ExistingWorkPolicy.REPLACE,
                    createRequest(inputData)
                )
        }

        private fun createRequest(inputData: Data): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<NotificationMatcheWorker>()
                .setInitialDelay(20000, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()


        fun stop(context: Context, match: MatchDomain) {
            WorkManager.getInstance(context).cancelUniqueWork(match.id)
        }
    }
}