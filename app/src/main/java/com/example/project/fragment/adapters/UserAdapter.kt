package com.example.project.fragment.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.User

class UserAdapter(
    private var userList: List<UserWithStatus>,
    private val onUserClick: (User) -> Unit // Add click callback
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
        val userStatus: TextView = itemView.findViewById(R.id.tvUserStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val userWithStatus = userList[position]
        val user = userWithStatus.user
        holder.userName.text = "${user.lastName} ${user.name}"
        holder.userStatus.text = userWithStatus.status

        // Set status color
        val context = holder.itemView.context
        val statusColor = when (userWithStatus.status.lowercase()) {
            "present" -> ContextCompat.getColor(context, android.R.color.holo_green_dark)
            "absent" -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
            "late" -> ContextCompat.getColor(context, android.R.color.holo_orange_dark)
            else -> ContextCompat.getColor(context, android.R.color.black)
        }
        holder.userStatus.setTextColor(statusColor)

        // Set click listener
        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateUsers(newUserList: List<UserWithStatus>) {
        userList = newUserList
        notifyDataSetChanged()
    }
}