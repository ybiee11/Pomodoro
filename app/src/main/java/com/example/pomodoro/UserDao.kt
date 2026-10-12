package com.example.pomodoro

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    // CREATE: Register new user
    @Insert
    suspend fun registerUser(user: User)

    // READ: Validate login credentials
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun loginUser(email: String, password: String): User?

    // READ: Fetch user by ID for account view
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): User?

    // UPDATE: Update profile information
    @Update
    suspend fun updateUser(user: User)

    // DELETE: Delete account
    @Delete
    suspend fun deleteUser(user: User)

    //Calendar
    @Insert
    suspend fun insertEvent(event: CalendarEvent)

    //Calendar
    @Query("SELECT * FROM calendar_events WHERE userId = :userId AND dateString = :dateString")
    suspend fun getEventsForDate(userId: Int, dateString: String): List<CalendarEvent>

    // --- TASK CRUD OPERATIONS ---
    @Insert
    suspend fun insertTask(task: Task): Long

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    @Delete
    suspend fun deleteTask(task: Task)

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)
}