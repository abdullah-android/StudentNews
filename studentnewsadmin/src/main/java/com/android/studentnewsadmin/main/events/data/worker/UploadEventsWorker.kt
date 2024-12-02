package com.android.studentnewsadmin.main.events.data.worker

import android.Manifest
import android.R
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.UserHandle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.android.studentnewsadmin.NotificationRelated
import com.android.studentnewsadmin.core.domain.resource.EventsState
import com.android.studentnewsadmin.main.events.domain.repository.EventsRepository
import com.android.studentnewsadmin.main.news.data.worker.DESCRIPTION
import com.android.studentnewsadmin.main.news.data.worker.SUCCESS_MSG
import com.android.studentnewsadmin.main.news.data.worker.TITLE
import com.android.studentnewsadmin.main.news.data.worker.URI_LIST
import kotlinx.coroutines.flow.first


const val ADDRESS = "address"
const val STARTING_DATE = "startingDate"
const val STARTING_TIME_HOUR = "startingTimeHour"
const val STARTING_TIME_MINUTES = "startingTimeMinutes"
const val STARTING_TIME_STATUS = "startingTimeStatus"
const val ENDING_DATE = "endingDate"
const val ENDING_TIME_HOUR = "endingTimeHour"
const val ENDING_TIME_MINUTES = "endingTimeMinutes"
const val ENDING_TIME_STATUS = "endingTimeStatus"
const val IS_AVAILABLE = "isAvailable"


class UploadEventsWorker(
    private val context: Context,
    val workerParameters: WorkerParameters,
    private val eventsRepository: EventsRepository,
): CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {

        return try {

            val title = inputData.getString(TITLE) ?: ""
            val description = inputData.getString(DESCRIPTION) ?: ""
            val address = inputData.getString(ADDRESS) ?: ""
            val startingDate = inputData.getLong(STARTING_DATE, 1732082977213L)
            val startingTimeHour = inputData.getInt(STARTING_TIME_HOUR, 12)
            val startingTimeMinutes = inputData.getInt(STARTING_TIME_MINUTES, 10)
            val startingTimeStatus = inputData.getString(STARTING_TIME_STATUS) ?: ""
            val endingDate = inputData.getLong(ENDING_DATE, 1732082977213L)
            val endingTimeHour = inputData.getInt(ENDING_TIME_HOUR, 12)
            val endingTimeMinutes = inputData.getInt(ENDING_TIME_MINUTES, 10)
            val endingTimeStatus = inputData.getString(ENDING_TIME_STATUS) ?: ""
            val stringArray = inputData.getStringArray(URI_LIST)
            val isAvailable = inputData.getBoolean(IS_AVAILABLE, true)

            val uriList = stringArray?.map { Uri.parse(it) } ?: emptyList()


            setForeground(uploadingNotificationForeground(title))

            val uploadTask = eventsRepository
                .onEventUpload(
                    title = title,
                    description = description,
                    address = address,
                    startingDate = startingDate,
                    startingTimeHour = startingTimeHour,
                    startingTimeMinutes = startingTimeMinutes,
                    startingTimeStatus = startingTimeStatus,
                    endingDate = endingDate,
                    endingTimeHour = endingTimeHour,
                    endingTimeMinutes = endingTimeMinutes,
                    endingTimeStatus = endingTimeStatus,
                    uriList = uriList,
                    isAvailable = isAvailable,
                    context = context
                ).first()

            when (uploadTask) {
                is EventsState.Failed -> {
                    failureNotification(uploadTask.message.localizedMessage ?: "")
                    Result.failure()
                }

                is EventsState.Success -> {
                    val successData = Data.Builder()
                        .putString(SUCCESS_MSG, uploadTask.data)
                        .putString(TITLE, title)
                        .build()
                    Result.success(successData)
                }

                else -> {
                    failureNotification("An Unknown Error Occurred!")
                    Result.failure()
                }
            }

        } catch (e: Exception) {
            failureNotification(e.localizedMessage ?: "")
            Result.failure()
        }

    }



    private fun uploadingNotificationForeground(title: String): ForegroundInfo {

        val notification = NotificationCompat.Builder(context, NotificationRelated.MEDIA_ADDING_CHANNEL_ID)
            .setSmallIcon(R.drawable.stat_sys_upload_done)
            .setContentTitle("UPLOADING EVENT...")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(title)
            )
            .build()

        return ForegroundInfo(NotificationRelated.MEDIA_ADDING_NOTIFICATION_ID, notification)
    }

    private fun failureNotification(errorMsg: String) {

        val notification = NotificationCompat.Builder(context, NotificationRelated.FAILURE_CHANNEL_ID)
            .setSmallIcon(R.drawable.stat_notify_error)
            .setContentTitle("Failed to Upload Event")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(errorMsg)
            )
            .build()

        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NotificationRelated.FAILURE_NOTIFICATION_ID, notification)
    }

}