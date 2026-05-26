package com.example.gymworkoutplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient:
            com.google.android.gms.auth.api.signin.GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // AUTO LOGIN

        val currentUser = auth.currentUser

        if (currentUser != null) {

            currentUser.reload().addOnCompleteListener {

                saveUserToDatabase(
                    currentUser.uid,
                    currentUser.displayName,
                    currentUser.email
                )

                goToMain()
            }

            return
        }

        // GOOGLE BUTTON

        val btnGoogle =
            findViewById<MaterialButton>(R.id.btnGoogleSignIn)

        // GOOGLE SIGN IN OPTIONS

        val gso =
            GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
            )
                .requestIdToken(
                    getString(R.string.default_web_client_id)
                )
                .requestEmail()
                .build()

        googleSignInClient =
            GoogleSignIn.getClient(this, gso)

        // SIGN IN RESULT

        val launcher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->

                val task =
                    GoogleSignIn.getSignedInAccountFromIntent(
                        result.data
                    )

                try {

                    val account = task.result

                    val credential =
                        GoogleAuthProvider.getCredential(
                            account.idToken,
                            null
                        )

                    auth.signInWithCredential(credential)
                        .addOnSuccessListener {

                            val user = auth.currentUser

                            user?.let {

                                saveUserToDatabase(
                                    it.uid,
                                    it.displayName,
                                    it.email
                                )
                            }

                            Toast.makeText(
                                this,
                                "Signed in as ${user?.displayName}",
                                Toast.LENGTH_SHORT
                            ).show()

                            goToMain()

                        }.addOnFailureListener {

                            Toast.makeText(
                                this,
                                "Authentication failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                } catch (e: Exception) {

                    Toast.makeText(
                        this,
                        "Sign-in cancelled",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        // BUTTON CLICK

        btnGoogle.setOnClickListener {

            googleSignInClient.signOut()
                .addOnCompleteListener {

                    launcher.launch(
                        googleSignInClient.signInIntent
                    )
                }
        }
    }

    // SAVE USER TO FIREBASE

    private fun saveUserToDatabase(
        uid: String,
        name: String?,
        email: String?
    ) {

        val database =
            FirebaseDatabase.getInstance().reference

        val userMap = HashMap<String, Any>()

        userMap["name"] =
            name ?: email?.substringBefore("@") ?: "User"

        userMap["email"] = email ?: ""

        // IMPORTANT FIX

        if (!userMap.containsKey("profileImage")) {
            userMap["profileImage"] = ""
        }

        database.child("users")
            .child(uid)
            .updateChildren(userMap)
    }

    // OPEN MAIN SCREEN

    private fun goToMain() {

        startActivity(
            Intent(this, MainActivity::class.java)
        )

        finish()
    }
}