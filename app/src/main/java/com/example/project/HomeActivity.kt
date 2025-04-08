package com.example.project


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.project.fragment.Attendance
import com.example.project.fragment.Home
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeActivity : AppCompatActivity() {
    lateinit var navButton:BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navButton = findViewById(R.id.bottomNavigationView)

        loadFragment(Home())
        navButton.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> loadFragment(Home())
                R.id.nav_attendance -> loadFragment(Attendance())
                /*
                R.id.nav_settings -> loadFragment(SettingsFragment()) */
            }
            true
        }

        navButton.itemIconTintList = null

    }

fun loadFragment(fragment: Fragment, addToBackStack: Boolean = false) {
    val transaction = supportFragmentManager.beginTransaction()
        .replace(R.id.frameLayout, fragment)

    if (addToBackStack) {
        transaction.addToBackStack(null)
    }

    transaction.commit()
}

}
