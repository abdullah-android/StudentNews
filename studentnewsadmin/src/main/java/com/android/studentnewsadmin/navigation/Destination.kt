package com.android.studentnewsadmin.main.navigation

import com.android.studentnewsadmin.main.events.domain.models.EditEventModel
import kotlinx.serialization.Serializable

sealed class Destination {

    @Serializable
    data object MAIN_SCREEN: Destination()

    @Serializable
    data object UPLOAD_NEWS_SCREEN: Destination()

    @Serializable
    data object UPLOAD_CATEGORY_SCREEN: Destination()

    @Serializable
    data object UPLOAD_EVENTS_SCREEN: Destination()

    @Serializable
    data class EDIT_EVENT_SCREEN(
        val eventId: String,
        val eventRelatedData: EditEventModel
    ): Destination()


    @Serializable
    data object UPLOAD_OFFERS_SCREEN: Destination()

    @Serializable
    data class EDIT_OFFER_SCREEN(
        val offerId: String
    )

}