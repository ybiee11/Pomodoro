package com.example.pomodoro

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Calendar

class CalendarFragment : Fragment() {

    private var selectedDate: String = ""
    private lateinit var eventAdapter: CalendarEventAdapter
    private lateinit var db: AppDatabase
    private var userId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val btnAddEvent = view.findViewById<Button>(R.id.btnAddEvent)
        val rvCalendarEvents = view.findViewById<RecyclerView>(R.id.rvCalendarEvents)

        db = AppDatabase.getDatabase(requireContext())
        val sessionManager = SessionManager(requireContext())
        userId = sessionManager.getUserId()

        eventAdapter = CalendarEventAdapter(
            events = mutableListOf(),
            onDeleteClick = { eventToDelete ->
                lifecycleScope.launch {
                    db.userDao().deleteEvent(eventToDelete) // Remove from SQLite
                    Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show()
                    loadEventsForSelectedDate() // Refresh event list
                }
            }
        )

        rvCalendarEvents.layoutManager = LinearLayoutManager(requireContext())
        rvCalendarEvents.adapter = eventAdapter

        val cal = Calendar.getInstance()
        selectedDate = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            loadEventsForSelectedDate()
        }

        btnAddEvent.setOnClickListener {
            showAddEventDialog()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadEventsForSelectedDate()
    }

    private fun loadEventsForSelectedDate() {
        lifecycleScope.launch {
            val events = db.userDao().getEventsForDate(userId, selectedDate)
            eventAdapter.updateEvents(events)
        }
    }

    private fun showAddEventDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Event ($selectedDate)")

        val input = EditText(requireContext()).apply {
            hint = "Enter event details..."
        }
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val eventText = input.text.toString().trim()
            if (eventText.isNotEmpty()) {
                val newEvent = CalendarEvent(
                    userId = userId,
                    dateString = selectedDate,
                    eventTitle = eventText
                )

                lifecycleScope.launch {
                    db.userDao().insertEvent(newEvent)
                    Toast.makeText(requireContext(), "Event Saved!", Toast.LENGTH_SHORT).show()
                    loadEventsForSelectedDate()
                }
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}