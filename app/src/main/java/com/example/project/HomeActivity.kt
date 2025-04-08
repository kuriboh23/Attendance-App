package com.example.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.project.fragment.Attendance
import com.example.project.fragment.Home
import com.example.project.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var navButton: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navButton = findViewById(R.id.bottomNavigationView)
        navButton.itemIconTintList = null

        loadFragment(Home(), false)

        navButton.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> loadFragment(Home(), false)
                R.id.nav_attendance -> loadFragment(Attendance(), false)
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        } else {
            // Clear back stack so Home becomes the root
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

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
            loadFragment(Home(), false) // Go directly to Home
        } else {
            super.onBackPressed() // Exit app
        }
    }
}
