package com.example.project.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.project.R
import com.example.project.UserPrefs
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    lateinit var guest_access:TextView
    lateinit var createAcountBtn:Button

    lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_main)

        guest_access = findViewById(R.id.guest_access)
        createAcountBtn= findViewById(R.id.signIn_btn)

        firebaseRef = FirebaseDatabase.getInstance().getReference("msg")
        firebaseRef.setValue("Yakuza Surface")

        guest_access.setOnClickListener {
            val intent = Intent(this, GuestActivity::class.java)
            startActivity(intent)
        }
        createAcountBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        val isLoggedIn = UserPrefs.isLoggedIn(this)
        if (isLoggedIn) {
            if (UserPrefs.getUserRole(this) == "user") {
                // User is logged in, go to home screen
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Admin is logged in, go to admin home screen
                val intent = Intent(this, AdminHomeActivity::class.java)
                startActivity(intent)
                finish()
            }

        }
    }
}

//    override fun onStart() {
//        super.onStart()
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            // User is already logged in, go to home screen
//            val intent = Intent(this,MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//    }
