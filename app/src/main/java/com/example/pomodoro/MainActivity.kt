package com.example.pomodoro

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

            replaceFragment(PomodoroFragment())

            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_pomodoro -> replaceFragment(PomodoroFragment())
                    R.id.nav_tasks -> replaceFragment(TasksFragment())
                    R.id.nav_calendar -> replaceFragment(CalendarFragment())
                    R.id.nav_account -> replaceFragment(AccountFragment())
                }
                true
            }
        }

        private fun replaceFragment(fragment: Fragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }



