package com.example.project.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.project.R
import com.example.project.UserPrefs
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        DynamicColors.applyToActivitiesIfAvailable(application)

        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.itemIconTintList = null

        // Setup navigation controller from NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // Connect bottom nav to navController
        bottomNav.setupWithNavController(navController)
        bottomNav.isItemActiveIndicatorEnabled = false

        // Handle special action (sign out)
        bottomNav.setOnItemSelectedListener { item ->

                navController.navigate(item.itemId)
                true
        }
    }


    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (navController.currentDestination?.id != R.id.nav_home) {
            navController.navigate(R.id.nav_home)
        } else {
            finish()
        }
    }
}
