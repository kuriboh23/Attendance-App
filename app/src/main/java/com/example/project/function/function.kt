package com.example.project.function

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.project.R

object function {
    fun Context.showCustomToast(message: String, layoutId: Int) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(layoutId, null)

        val toastText = view.findViewById<TextView>(R.id.toastText)
        toastText.text = message

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            this.view = view
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)
            show()
        }
    }

}