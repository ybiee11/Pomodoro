package com.example.pomodoro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val dateString: String,
    val eventTitle: String,
)
