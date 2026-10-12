package com.example.pomodoro

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val etFullName = view.findViewById<EditText>(R.id.etFullName)
        val btnUpdateProfile = view.findViewById<Button>(R.id.btnUpdateProfile)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = view.findViewById<Button>(R.id.btnDeleteAccount)

        val db = AppDatabase.getDatabase(requireContext())
        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

        // Fetch current user profile details
        lifecycleScope.launch {
            currentUser = db.userDao().getUserById(userId)
            currentUser?.let {
                tvUserEmail.text = it.email
                etFullName.setText(it.fullName)
            }
        }

        // Modify user full name
        btnUpdateProfile.setOnClickListener {
            val updatedName = etFullName.text.toString().trim()
            currentUser?.let { user ->
                user.fullName = updatedName
                lifecycleScope.launch {
                    db.userDao().updateUser(user)
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // LOGOUT WITH CONFIRMATION DIALOG
        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    sessionManager.clearSession()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // DELETE: Remove account from database
        btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    currentUser?.let { user ->
                        lifecycleScope.launch {
                            db.userDao().deleteUser(user)
                            sessionManager.clearSession()
                            Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        return view
    }
}