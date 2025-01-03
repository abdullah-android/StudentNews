package com.android.studentnews.main.events.domain.repository

import androidx.paging.PagingData
import com.android.studentnews.main.events.domain.models.EventsBookingModel
import com.android.studentnewsadmin.core.domain.resource.EventsState
import com.android.studentnewsadmin.main.events.domain.models.EventsModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow

interface EventsRepository {

    val userDocRef: DocumentReference?
    val eventsColRef: CollectionReference?
    val savedEventsColRef: CollectionReference?


    fun getEventsList(
        availableStatus: Boolean?,
        limit: Int
    ): Flow<PagingData<EventsModel>>

    fun getEventById(eventId: String): Flow<EventsState<EventsModel?>>

    fun onEventRegister(
        eventId: String,
        eventsBookingModel: EventsBookingModel
    ): Flow<EventsState<String>>

    fun onEventSave(event: EventsModel): Flow<EventsState<String>>

    fun onEventRemoveFromSave(event: EventsModel): Flow<EventsState<String>>

    fun getSavedEventById(eventId: String): Flow<EventsState<EventsModel?>>

    fun getRegisteredEventsList(limit: Int): Flow<PagingData<EventsModel>> // Booked Events of Current User

    fun getSavedEventsList(limit: Int): Flow<PagingData<EventsModel>>

    fun getEventsListByAvailableStatus(availableStatus: Boolean): Flow<EventsState<List<EventsModel>>>


    suspend fun getEventsUpdates(): EventsModel?

    suspend fun getCurrentUserName(): String

    fun startEventWorker()

    fun cancelEventsWorker()

}