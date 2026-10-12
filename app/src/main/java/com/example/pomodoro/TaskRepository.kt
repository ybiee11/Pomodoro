package com.example.pomodoro

object TaskRepository {
    val taskList = mutableListOf<Task>()
    var selectedTask: Task? = null
}