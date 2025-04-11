package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.project.fragment.Attendance
import com.example.project.fragment.Home
import com.example.project.R
import com.example.project.data.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {

    private lateinit var navButton: BottomNavigationView
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navButton = findViewById(R.id.bottomNavigationView)
        navButton.itemIconTintList = null

        loadFragment(Home())

        navButton.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> loadFragment(Home())
                R.id.nav_attendance -> loadFragment(Attendance())
                R.id.nav_signOut -> {
                    signOut()
                }
//                R.id.nav_signOut -> {
//                    auth.signOut()
//                    Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }
            }
            true
        }
    }

    private fun signOut() {
        UserPrefs.clearUserId(this)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)


        transaction.commit()
        updateBottomNavSelection(fragment)
    }

    private fun updateBottomNavSelection(fragment: Fragment) {
        when (fragment) {
            is Home -> navButton.menu.findItem(R.id.nav_home).isChecked = true
            is Attendance -> navButton.menu.findItem(R.id.nav_attendance).isChecked = true
        }
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
        if (currentFragment !is Home) {
            loadFragment(Home()) // Go directly to Home
        } else {
            super.onBackPressed() // Exit app
        }
    }
}
