package com.example.homeserv.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homeserv.R
import com.example.homeserv.data.Offer
import com.example.homeserv.data.Roles
import com.example.homeserv.data.User
import com.example.homeserv.db.DBHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class BookingConfirmationActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private var offer: Offer? = null
    private var customers = listOf<User>()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_confirmation)
        db = DBHelper(this)
        currentUser = db.getUserById(SessionManager(this).getUserId())
        offer = db.getOfferById(intent.getIntExtra(EXTRA_OFFER_ID, -1))

        if (currentUser == null || offer == null) { finish(); return }
        if (currentUser!!.role != Roles.CUSTOMER && currentUser!!.role != Roles.ADMIN) {
            Toast.makeText(this, R.string.only_customers_book, Toast.LENGTH_SHORT).show()
            finish(); return
        }

        findViewById<TextView>(R.id.tvConfirmTitle).text = offer!!.title
        findViewById<TextView>(R.id.tvConfirmProvider).text = getString(R.string.provider_format, offer!!.providerName)
        findViewById<TextView>(R.id.tvConfirmPrice).text = getString(R.string.price_format, offer!!.price)
        findViewById<TextView>(R.id.tvConfirmDuration).text = getString(R.string.duration_format, offer!!.duration)

        val spinner = findViewById<Spinner>(R.id.spinnerCustomer)
        val spinnerLabel = findViewById<TextView>(R.id.tvCustomerLabel)

        if (currentUser!!.role == Roles.ADMIN) {
            // Admin can book on behalf of any customer
            customers = db.getCustomers()
            spinner.visibility = View.VISIBLE
            spinnerLabel?.visibility = View.VISIBLE
            spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, customers.map { it.name })
        } else {
            // Regular customer: hide spinner, book for themselves
            customers = listOf(currentUser!!)
            spinner.visibility = View.GONE
            spinnerLabel?.visibility = View.GONE
        }

        findViewById<MaterialButton>(R.id.btnConfirmBooking).setOnClickListener { confirm() }
    }

    private fun confirm() {
        val customer = if (currentUser!!.role == Roles.ADMIN) {
            customers.getOrNull(findViewById<Spinner>(R.id.spinnerCustomer).selectedItemPosition)
                ?: return toast(getString(R.string.no_customer_found))
        } else {
            currentUser!!
        }
        val notes = findViewById<TextInputEditText>(R.id.etBookingNotes).text.toString().trim()
        if (db.addBooking(offer!!.id, customer.id, notes) > 0) {
            toast(getString(R.string.booking_confirmed))
            startActivity(Intent(this, MyBookingsActivity::class.java))
            finish()
        } else toast(getString(R.string.something_wrong))
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    companion object { const val EXTRA_OFFER_ID = "extra_offer_id" }
}
