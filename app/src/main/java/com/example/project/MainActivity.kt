package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var guest_access:TextView
    lateinit var createAcountBtn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_main)

        guest_access = findViewById(R.id.guest_access)
        createAcountBtn= findViewById(R.id.signIn_btn)


        guest_access.setOnClickListener {
            val intent = Intent(this,GuestActivity::class.java)
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
            // User is already logged in, go to home screen
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
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
