package com.android.studentnewsadmin.main.navigation

import kotlinx.serialization.Serializable

sealed class Destination {

    @Serializable
    object MAIN_SCREEN

    @Serializable
    object UPLOAD_NEWS_SCREEN

    @Serializable
    object UPLOAD_CATEGORY_SCREEN

    @Serializable
    object UPLOAD_EVENTS_SCREEN

}