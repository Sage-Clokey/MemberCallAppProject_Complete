package com.example.membercallapp

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import java.text.SimpleDateFormat
import java.util.*

class SheetsServiceHelper(context: Context, credential: GoogleAccountCredential) {

    private val service: Sheets = Sheets.Builder(
        AndroidHttp.newCompatibleTransport(),
        GsonFactory.getDefaultInstance(),
        credential
    ).setApplicationName("Member Call App").build()

    private val sheetId = "private val sheetId = \"1CF9h9v4Z9ldmY8hUxswMfnvBCnbbySLpTiGElT0HtuE"
    private val sheetName = "Sheet1"

    fun appendNoteToMember(firstName: String, lastName: String, newNote: String, initials: String = "SC") {
        Thread {
            try {
                val fullRange = "$sheetName!A1:Z"
                val response = service.spreadsheets().values().get(sheetId, fullRange).execute()
                val rows = response.getValues()

                val header = rows.first()
                val fNameCol = header.indexOf("First Name")
                val lNameCol = header.indexOf("Last Name")
                val notesCol = header.indexOf("Member Notes")

                val date = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
                val prepend = "[$initials] $date $newNote"

                for ((index, row) in rows.withIndex()) {
                    if (index == 0) continue // skip header
                    val fName = row.getOrNull(fNameCol)?.toString()?.trim()
                    val lName = row.getOrNull(lNameCol)?.toString()?.trim()

                    if (fName.equals(firstName.trim(), true) && lName.equals(lastName.trim(), true)) {
                        val oldNote = row.getOrNull(notesCol)?.toString() ?: ""
                        val updatedNote = "$prepend\n$oldNote"
                        val targetRange = "$sheetName!${(65 + notesCol).toChar()}${index + 1}"

                        val body = ValueRange().setValues(listOf(listOf(updatedNote)))
                        service.spreadsheets().values()
                            .update(sheetId, targetRange, body)
                            .setValueInputOption("RAW")
                            .execute()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}