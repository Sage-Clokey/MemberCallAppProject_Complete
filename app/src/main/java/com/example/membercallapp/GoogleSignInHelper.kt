package com.example.membercallapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.sheets.v4.SheetsScopes

object GoogleSignInHelper {

    private lateinit var googleSignInClient: GoogleSignInClient

    fun init(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(SheetsScopes.SPREADSHEETS))
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun signIn(activity: Activity, requestCode: Int) {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, requestCode)
    }

    fun getSignedInAccount(context: Context): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    fun handleSignInResult(data: Intent?, onSuccess: (GoogleSignInAccount) -> Unit, onFailure: (Exception) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                onSuccess(account)
            } else {
                onFailure(Exception("Account is null"))
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun signOut(context: Context) {
        googleSignInClient.signOut()
    }
}