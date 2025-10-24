package com.jetpackComposeTest1.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val packageName:String,
    val packageId:String,
    val icon:String,
    val messageTitle:String,
    val message:String,
    val timestamp: Long = System.currentTimeMillis()
)