package com.android.studentnews.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaTimestamp
import androidx.core.app.NotificationManagerCompat
import com.android.studentnews.main.events.ADDRESS
import com.android.studentnews.main.events.BOOKINGS
import com.android.studentnews.main.events.DESCRIPTION
import com.android.studentnews.main.events.ENDING_DATE
import com.android.studentnews.main.events.ENDING_TIME_HOUR
import com.android.studentnews.main.events.ENDING_TIME_MINUTES
import com.android.studentnews.main.events.ENDING_TIME_STATUS
import com.android.studentnews.main.events.EVENT_ID
import com.android.studentnews.main.events.IS_AVAILABLE
import com.android.studentnews.main.events.SAVED_EVENT_ACTION
import com.android.studentnews.main.events.STARTING_DATE
import com.android.studentnews.main.events.STARTING_TIME_HOUR
import com.android.studentnews.main.events.STARTING_TIME_MINUTES
import com.android.studentnews.main.events.STARTING_TIME_STATUS
import com.android.studentnews.main.events.TITLE
import com.android.studentnews.main.events.URL_LIST
import com.android.studentnews.main.events.domain.models.EventsBookingModel
import com.android.studentnews.main.events.domain.repository.EventsRepository
import com.android.studentnews.main.news.SAVE_NEWS_ACTION
import com.android.studentnews.news.domain.model.NewsModel
import com.android.studentnews.news.domain.model.UrlList
import com.android.studentnews.news.domain.repository.NewsRepository
import com.android.studentnews.news.domain.resource.NewsState
import com.android.studentnewsadmin.core.domain.resource.EventsState
import com.android.studentnewsadmin.main.events.domain.models.EventsModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.collections.map
import kotlin.text.split
import kotlin.text.toLong

class MyBroadcastReceiver : BroadcastReceiver(), KoinComponent {


    private val notificationManager: NotificationManagerCompat by inject()
    private val scope = CoroutineScope(Dispatchers.Default)


    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == SAVE_NEWS_ACTION) {

            val newsRepository: NewsRepository by inject()

            val notificationId = intent.getIntExtra("notification_id", 1)
            notificationManager.cancel(notificationId)

            val title = intent.getStringExtra("title") ?: ""
            val description = intent.getStringExtra("description") ?: ""
            val newsId = intent.getStringExtra("newsId") ?: ""
            val category = intent.getStringExtra("category") ?: ""
            val link = intent.getStringExtra("link") ?: ""
            val linkTitle = intent.getStringExtra("link_title") ?: ""
            val serializedUrlList = intent.getStringExtra("url_list")
            val stringArray = intent.getStringArrayListExtra("likes")


            val likes = stringArray?.map { it.toString() }
            val urlList = serializedUrlList
                ?.split(",")
                ?.map {
                    val parts = it.split(";")
                    UrlList(parts[0], parts[1], parts[2].toLong(), parts[3])
                } ?: emptyList()

            try {

                val news = NewsModel(
                    title = title,
                    description = description,
                    newsId = newsId,
                    category = category,
                    timestamp = Timestamp.now(),
                    link = link,
                    linkTitle = linkTitle,
                    urlList = urlList,
                    likes = likes
                )

                scope.launch {
                    newsRepository
                        .onNewsSave(news)
                        .collect { result ->
                            when (result) {
                                is NewsState.Success<*> -> {
                                    scope.cancel()
                                }

                                else -> {
                                    scope.cancel()
                                }
                            }
                        }
                }

            } catch (e: Exception) {
                scope.cancel()
            }

        }

        if (intent?.action == SAVED_EVENT_ACTION) {

            val eventsRepository: EventsRepository by inject()

//            val notificationId = intent.getIntExtra("notification_id", 1)
//            notificationManager.cancel(notificationId)

            val title = intent.getStringExtra(TITLE) ?: ""
            val description = intent.getStringExtra(DESCRIPTION) ?: ""
            val address = intent.getStringExtra(ADDRESS) ?: ""
            val eventId = intent.getStringExtra(EVENT_ID) ?: ""
            val startingDate = intent.getLongExtra(STARTING_DATE, 0L)
            val startingTimeHour = intent.getIntExtra(STARTING_TIME_HOUR, 0)
            val startingTimeMinutes = intent.getIntExtra(STARTING_TIME_MINUTES, 0)
            val startingTimeStatus = intent.getStringExtra(STARTING_TIME_STATUS) ?: ""
            val endingDate = intent.getLongExtra(ENDING_DATE, 0L)
            val endingTimeHour = intent.getIntExtra(ENDING_TIME_HOUR, 0)
            val endingTimeMinutes = intent.getIntExtra(ENDING_TIME_MINUTES, 0)
            val endingTimeStatus = intent.getStringExtra(ENDING_TIME_STATUS) ?: ""
            val serializedUrlList = intent.getStringExtra(URL_LIST)
            val serializedBookingsList = intent.getStringExtra(BOOKINGS) ?: null
            val isAvailable = intent.getBooleanExtra(IS_AVAILABLE, true)

            val urlList = serializedUrlList
                ?.split(",")
                ?.mapNotNull {
                    val parts = it.split(";")
                    UrlList(parts[0], parts[1], parts[2].toLong(), parts[3])
                } ?: emptyList()

            val bookings = serializedBookingsList
                ?.split(",")
                ?.mapNotNull {
                    val parts = it.split(";")
                    if (parts.size == 8) {
                        EventsBookingModel(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            parts[4],
                            parts[5],
                            parts[6],
                            parts[7].toIntOrNull() ?: 0
                        )
                    } else null
                } ?: emptyList()

            try {

                val event = EventsModel(
                    title = title,
                    description = description,
                    eventId = eventId,
                    address = address,
                    startingDate = startingDate,
                    startingTimeHour = startingTimeHour,
                    startingTimeMinutes = startingTimeMinutes,
                    startingTimeStatus = startingTimeStatus,
                    endingDate = endingDate,
                    endingTimeHour = endingTimeHour,
                    endingTimeMinutes = endingTimeMinutes,
                    endingTimeStatus = endingTimeStatus,
                    timestamp = Timestamp.now(),
                    urlList = urlList,
                    bookings = bookings,
                    isAvailable = isAvailable
                )

                scope.launch {
                    eventsRepository
                        .onEventSave(event = event)
                        .collectLatest { result ->
                            when (result) {
                                is EventsState.Success -> {
                                    scope.cancel()
                                }

                                else -> scope.cancel()
                            }
                        }
                }
            } catch (e: Exception) {
                scope.cancel()
            }


        }

    }

}