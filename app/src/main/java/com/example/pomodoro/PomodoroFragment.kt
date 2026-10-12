package com.example.pomodoro

import android.app.AlertDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Locale

class PomodoroFragment : Fragment() {

    private lateinit var tvCurrentTaskTitle: TextView
    private lateinit var tvPomodoroCount: TextView
    private lateinit var tvTimerDisplay: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnReset: Button
    private lateinit var btnAddTaskDashboard: Button
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var countDownTimer: CountDownTimer? = null
    private var isTimerRunning = false
    private val START_TIME_IN_MILLIS: Long = 25 * 60 * 1000 // 25 Minutes standard Pomodoro
    private var timeLeftInMillis: Long = START_TIME_IN_MILLIS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pomodoro, container, false)

        tvCurrentTaskTitle = view.findViewById(R.id.tvTaskTitleDisplay)
        tvPomodoroCount = view.findViewById(R.id.tvPomodoroCounter)
        tvTimerDisplay = view.findViewById(R.id.tvTimer)
        btnStart = view.findViewById(R.id.btnStart)
        btnPause = view.findViewById(R.id.btnPause)
        btnReset = view.findViewById(R.id.btnReset)
        btnAddTaskDashboard = view.findViewById(R.id.btnAddTask)

        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())

        btnStart.setOnClickListener {
            if (!isTimerRunning) {
                startTimer()
            }
        }

        btnPause.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            }
        }

        btnReset.setOnClickListener {
            resetTimer()
        }

        btnAddTaskDashboard.setOnClickListener {
            showAddTaskDialog()
        }

        updateCountDownText()

        return view
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                isTimerRunning = false
                Toast.makeText(requireContext(), "Pomodoro Completed!", Toast.LENGTH_LONG).show()
                resetTimer()
            }
        }.start()

        isTimerRunning = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = START_TIME_IN_MILLIS
        updateCountDownText()
    }

    private fun updateCountDownText() {
        val minutes = (timeLeftInMillis / 1000).toInt() / 60
        val seconds = (timeLeftInMillis / 1000).toInt() % 60

        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        tvTimerDisplay.text = timeLeftFormatted
    }

    override fun onResume() {
        super.onResume()
        syncActiveTask()
    }

    override fun onPause() {
        super.onPause()
        pauseTimer()
    }

    private fun syncActiveTask() {
        lifecycleScope.launch {
            val tasks = db.userDao().getAllTasks()
            val activeTaskId = sessionManager.getActiveTaskId()

            if (tasks.isNotEmpty()) {
                val activeTask = tasks.find { it.id == activeTaskId } ?: tasks.last()
                tvCurrentTaskTitle.text = activeTask.title
                tvPomodoroCount.text = "${activeTask.estimatedPomodoros} Pomodoro(s)"
            } else {
                tvCurrentTaskTitle.text = "No Active Task"
                tvPomodoroCount.text = "0 Pomodoros"
            }
        }
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Task from Dashboard")

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etTitle = EditText(requireContext()).apply { hint = "Task Title" }
        val etPomodoros = EditText(requireContext()).apply {
            hint = "Pomodoros count"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(etTitle)
        layout.addView(etPomodoros)
        builder.setView(layout)

        builder.setPositiveButton("Save") { _, _ ->
            val title = etTitle.text.toString().trim()
            val pomodorosStr = etPomodoros.text.toString().trim()

            if (title.isNotEmpty()) {
                val count = pomodorosStr.toIntOrNull() ?: 1
                val newTask = Task(title = title, estimatedPomodoros = count)

                lifecycleScope.launch {
                    val newId = db.userDao().insertTask(newTask)
                    sessionManager.saveActiveTaskId(newId.toInt())
                    syncActiveTask()
                    Toast.makeText(requireContext(), "Task added to database!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}