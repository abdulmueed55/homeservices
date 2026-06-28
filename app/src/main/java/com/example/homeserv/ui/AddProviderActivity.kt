package com.example.homeserv.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homeserv.R
import com.example.homeserv.data.Roles
import com.example.homeserv.db.DBHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddProviderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_provider)
        val db = DBHelper(this)
        val user = db.getUserById(SessionManager(this).getUserId())
        if (user == null || user.role != Roles.ADMIN) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish(); return
        }
        val spinner = findViewById<Spinner>(R.id.spinnerServiceType)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Plumber", "Electrician", "Cleaner", "Carpenter", "Painter"))
        findViewById<MaterialButton>(R.id.btnRegisterProvider).setOnClickListener {
            val name = findViewById<TextInputEditText>(R.id.etProviderName).text.toString().trim()
            val phone = findViewById<TextInputEditText>(R.id.etProviderPhone).text.toString().trim()
            val password = findViewById<TextInputEditText>(R.id.etProviderPassword).text.toString().trim()
            if (name.isBlank() || phone.isBlank() || password.isBlank()) {
                return@setOnClickListener Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
            }
            val result = db.addProvider(name, phone, spinner.selectedItem.toString(), password)
            if (result > 0) {
                Toast.makeText(this, R.string.provider_added, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Phone already exists.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
