package io.github.ndtung723.nihongoit.learningservice.dto

data class UpdatePreferencesRequest(
    val notificationPreferences: String? = null,
    val reminderEnabled: Boolean? = null,
    val reminderTime: String? = null,
    val minCardThreshold: Int? = null,
    val leechNotificationsEnabled: Boolean? = null,
)
