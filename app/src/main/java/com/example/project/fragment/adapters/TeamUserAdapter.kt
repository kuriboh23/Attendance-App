import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.User

class TeamUserAdapter(private var teamUsers: List<User>,
                      private val onUserClick: (User) -> Unit) :
    RecyclerView.Adapter<TeamUserAdapter.ViewHolder>() {

    private var fullUserList: List<User> = teamUsers

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = teamUsers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = teamUsers[position]
        holder.userName.text = "${user.lastName} ${user.name}"

        // Set click listener
        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateUsers(newUsers: List<User>) {
        fullUserList = newUsers
        teamUsers = newUsers
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterUsers(query: String) {
        teamUsers = if (query.isEmpty()) {
            fullUserList
        } else {
            fullUserList.filter {
                val fullName = "${it.lastName} ${it.name}".lowercase()
                val userId = "${it.id}".lowercase()
                (fullName.contains(query.lowercase().trim()) || userId.contains(query.lowercase().trim())) }
        }
        notifyDataSetChanged()
    }
}

