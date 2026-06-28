package com.example.homeserv.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homeserv.R
import com.example.homeserv.data.Roles
import com.example.homeserv.data.User
import com.example.homeserv.db.DBHelper
import com.google.android.material.button.MaterialButton

class AdminUsersActivity : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_users)
        db = DBHelper(this)
        val user = db.getUserById(SessionManager(this).getUserId())
        if (user == null || user.role != Roles.ADMIN) {
            startActivity(Intent(this, AuthActivity::class.java)); finish(); return
        }
        adapter = UsersAdapter(emptyList()) { u, block ->
            db.setUserBlocked(u.id, block)
            Toast.makeText(this, if (block) "User blocked." else "User unblocked.", Toast.LENGTH_SHORT).show()
            load()
        }
        findViewById<RecyclerView>(R.id.rvUsers).apply {
            layoutManager = LinearLayoutManager(this@AdminUsersActivity)
            adapter = this@AdminUsersActivity.adapter
        }
        load()
    }

    private fun load() {
        adapter.updateData(db.getAllUsers())
    }

    inner class UsersAdapter(
        private var users: List<User>,
        private val onToggleBlock: (User, Boolean) -> Unit
    ) : RecyclerView.Adapter<UsersAdapter.UserVH>() {

        fun updateData(newList: List<User>) { users = newList; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH =
            UserVH(LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false))

        override fun onBindViewHolder(holder: UserVH, position: Int) = holder.bind(users[position])
        override fun getItemCount() = users.size

        inner class UserVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tvUserName)
            private val tvPhone: TextView = itemView.findViewById(R.id.tvUserPhone)
            private val tvRole: TextView = itemView.findViewById(R.id.tvUserRole)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvUserStatus)
            private val btnToggle: MaterialButton = itemView.findViewById(R.id.btnToggleBlock)

            fun bind(user: User) {
                tvName.text = user.name
                tvPhone.text = user.phone
                tvRole.text = user.role
                if (user.isBlocked) {
                    tvStatus.text = "Blocked"
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_active))
                    btnToggle.text = "Unblock"
                    btnToggle.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.primary_green)
                } else {
                    tvStatus.text = "Active"
                    tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_completed))
                    btnToggle.text = "Block"
                    btnToggle.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.status_active)
                }
                btnToggle.setOnClickListener { onToggleBlock(user, !user.isBlocked) }
            }
        }
    }
}
