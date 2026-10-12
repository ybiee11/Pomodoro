package com.example.pomodoro

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private val taskList = mutableListOf<Task>()
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)

        db = AppDatabase.getDatabase(requireContext())
        sessionManager = SessionManager(requireContext())

        val btnAddTask = view.findViewById<Button>(R.id.btnAddTask)
        rvTasks = view.findViewById(R.id.rvTasks)
        rvTasks.layoutManager = LinearLayoutManager(requireContext())

        taskAdapter = TaskAdapter(
            tasks = taskList,
            onTaskClick = { selectedTask ->
                // Save selected task ID for the Pomodoro Dashboard
                sessionManager.saveActiveTaskId(selectedTask.id)
                Toast.makeText(requireContext(), "Selected '${selectedTask.title}' for Timer!", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { task, position ->
                lifecycleScope.launch {
                    db.userDao().deleteTask(task)
                    taskList.removeAt(position)
                    taskAdapter.notifyItemRemoved(position)
                }
            }
        )
        rvTasks.adapter = taskAdapter

        btnAddTask.setOnClickListener { showAddTaskDialog() }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Always refresh list when switching to this tab
        loadTasksFromDatabase()
    }

    private fun loadTasksFromDatabase() {
        lifecycleScope.launch {
            val savedTasks = db.userDao().getAllTasks()
            taskList.clear()
            taskList.addAll(savedTasks)
            taskAdapter.notifyDataSetChanged()
        }
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Create New Task")

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val etTitle = EditText(requireContext()).apply { hint = "Task Title" }
        val etPomodoros = EditText(requireContext()).apply {
            hint = "Estimated Pomodoros (e.g., 3)"
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
                    loadTasksFromDatabase()
                    Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}