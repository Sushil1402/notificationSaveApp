package com.notistorex.app.data.repository.preferences

interface SharedPreferencesRepository {
    fun setAppSelectionCompleted(completed: Boolean)
    fun isAppSelectionCompleted(): Boolean
}