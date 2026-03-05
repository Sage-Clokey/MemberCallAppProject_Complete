package com.example.membercallapp

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val members = mutableListOf<Member>()
    private var currentIndex = 0

    private lateinit var nameView: TextView
    private lateinit var phoneView: TextView
    private lateinit var emailView: TextView
    private lateinit var notesView: TextView
    private lateinit var callButton: Button
    private lateinit var noteButton: Button
    private lateinit var nextButton: Button

    private val sheetUrl = "https://docs.google.com/spreadsheets/d/YOUR_SHEET_ID/export?format=csv"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameView = findViewById(R.id.nameView)
        phoneView = findViewById(R.id.phoneView)
        emailView = findViewById(R.id.emailView)
        notesView = findViewById(R.id.notesView)
        callButton = findViewById(R.id.callButton)
        noteButton = findViewById(R.id.noteButton)
        nextButton = findViewById(R.id.nextButton)

        fetchMembersFromSheet()

        callButton.setOnClickListener {
            val phone = members[currentIndex].phone
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        }

        noteButton.setOnClickListener {
            showNoteDialog(members[currentIndex])
        }

        nextButton.setOnClickListener {
            currentIndex = (currentIndex + 1) % members.size
            displayMember(currentIndex)
        }
    }

    private fun displayMember(index: Int) {
        val member = members[index]
        nameView.text = "${member.firstName} ${member.lastName}"
        phoneView.text = member.phone
        emailView.text = member.email
        notesView.text = member.notes
    }

    private fun showNoteDialog(member: Member) {
        val input = EditText(this)
        AlertDialog.Builder(this).apply {
            setTitle("Add Note for ${member.firstName}")
            setView(input)
            setPositiveButton("Save") { _, _ ->
                val initials = "SC"
                val date = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
                val newNote = "[$initials] $date ${input.text}\n${member.notes}"
                member.notes = newNote
                notesView.text = newNote
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    private fun fetchMembersFromSheet() {
        val client = OkHttpClient()
        val request = Request.Builder().url(sheetUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val csv = response.body?.string()
                if (csv != null) {
                    val lines = csv.split("\n").drop(1)
                    members.clear()
                    for (line in lines) {
                        val cols = line.split(",")
                        if (cols.size >= 8) {
                            val member = Member(
                                id = members.size + 1,
                                firstName = cols[0],
                                lastName = cols[1],
                                phone = cols[2],
                                email = cols[3],
                                notes = cols[4],
                                status = cols[5],
                                university = cols[6]
                            )
                            members.add(member)
                        }
                    }
                    runOnUiThread {
                        displayMember(currentIndex)
                    }
                }
            }
        })
    }
}

data class Member(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    var notes: String,
    val status: String,
    val university: String
)