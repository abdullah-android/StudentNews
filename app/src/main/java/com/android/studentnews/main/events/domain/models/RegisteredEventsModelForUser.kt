package com.android.studentnews.main.events.domain.models

import com.android.studentnews.auth.domain.models.RegistrationData
import com.google.firebase.Timestamp

data class RegisteredEventsModelForUser(
    val eventId: String = "",
    val userId: String = "",
    val registrationData: RegistrationData? = null,
    val registrationCode: String = "",
    val registeredAt: Timestamp,
) {
    constructor() : this(
        userId = "",
        eventId = "",
        registrationData = null,
        registrationCode = "",
        registeredAt = Timestamp.now()
    )
}