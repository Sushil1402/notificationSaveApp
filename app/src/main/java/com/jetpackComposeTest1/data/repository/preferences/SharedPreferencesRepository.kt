package com.jetpackComposeTest1.data.repository.preferences

interface SharedPreferencesRepository {
    fun setAppSelectionCompleted(completed: Boolean)
    fun isAppSelectionCompleted(): Boolean
}