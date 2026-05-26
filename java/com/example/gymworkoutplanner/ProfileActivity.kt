package com.example.gymworkoutplanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var imgProfile: ImageView

    // =========================
    // IMAGE PICKER
    // =========================

    private val imagePicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->

            if (uri != null) {

                imgProfile.setImageURI(uri)

                try {

                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                } catch (_: Exception) {
                }

                val prefs =
                    getSharedPreferences(
                        "ProfilePrefs",
                        MODE_PRIVATE
                    )

                prefs.edit()
                    .putString(
                        "profile_image",
                        uri.toString()
                    )
                    .apply()

                Toast.makeText(
                    this,
                    "Profile photo updated",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        // =========================
        // USER INFO
        // =========================

        val tvUserName =
            findViewById<TextView>(R.id.tvUserName)

        val tvEmail =
            findViewById<TextView>(R.id.tvEmail)

        // =========================
        // STATS
        // =========================

        val tvWorkoutCount =
            findViewById<TextView>(R.id.tvWorkoutCount)

        val tvCalories =
            findViewById<TextView>(R.id.tvCalories)

        val tvStreak =
            findViewById<TextView>(R.id.tvStreak)

        // =========================
        // BUTTONS
        // =========================

        val logout =
            findViewById<Button>(R.id.btnLogout)

        val cardPrivacy =
            findViewById<CardView>(R.id.cardPrivacy)

        val cardNotifications =
            findViewById<CardView>(R.id.cardNotifications)

        val cardAbout =
            findViewById<CardView>(R.id.cardAbout)

        val btnSettings =
            findViewById<ImageView>(R.id.btnSettings)

        val btnEditPhoto =
            findViewById<CardView>(R.id.btnEditPhoto)

        imgProfile =
            findViewById(R.id.imgProfile)

        // =========================
        // USER DATA
        // =========================

        val name =
            user?.displayName ?: "User"

        val email =
            user?.email ?: "No email"

        tvUserName.text = name
        tvEmail.text = email

        // =========================
        // LOAD SAVED IMAGE
        // =========================

        val prefs =
            getSharedPreferences(
                "ProfilePrefs",
                MODE_PRIVATE
            )

        val savedImage =
            prefs.getString(
                "profile_image",
                null
            )

        if (savedImage != null) {

            imgProfile.setImageURI(
                Uri.parse(savedImage)
            )
        }

        // =========================
        // LOAD REAL STATS
        // =========================

        loadWorkoutCount(tvWorkoutCount)

        loadCalories(tvCalories)

        loadStreak(tvStreak)

        // =========================
        // SETTINGS BUTTON
        // =========================

        btnSettings.setOnClickListener {

            Toast.makeText(
                this,
                "Settings panel coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // PRIVACY
        // =========================

        cardPrivacy.setOnClickListener {

            Toast.makeText(
                this,
                "Privacy & Security screen coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // NOTIFICATIONS
        // =========================

        cardNotifications.setOnClickListener {

            Toast.makeText(
                this,
                "Notifications screen coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // ABOUT
        // =========================

        cardAbout.setOnClickListener {

            Toast.makeText(
                this,
                "R27FitCrew v1.0",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // CHANGE PROFILE PHOTO
        // =========================

        btnEditPhoto.setOnClickListener {

            imagePicker.launch("image/*")
        }

        // =========================
        // LOGOUT
        // =========================

        logout.setOnClickListener {

            auth.signOut()

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finish()
        }
    }

    // =========================
    // TOTAL WORKOUTS
    // =========================

    private fun loadWorkoutCount(tv: TextView) {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid ?: return

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("workout_history")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    tv.text =
                        snapshot.childrenCount.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =========================
    // CALORIES TODAY
    // =========================

    private fun loadCalories(tv: TextView) {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid ?: return

        val today =
            getTodayKey()

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("diet")
            .child(today)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    var total = 0

                    for (child in snapshot.children) {

                        val entry =
                            child.getValue(FoodEntry::class.java)

                        if (entry != null) {

                            total += entry.calories
                        }
                    }

                    tv.text = total.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =========================
    // STREAK
    // =========================

    private fun loadStreak(tv: TextView) {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid ?: return

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("workout_history")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val dates = mutableListOf<String>()

                    for (child in snapshot.children) {

                        child.key?.let {
                            dates.add(it)
                        }
                    }

                    dates.sortDescending()

                    var streak = 0

                    val cal =
                        java.util.Calendar.getInstance()

                    for (date in dates) {

                        val expected =
                            "${cal.get(java.util.Calendar.YEAR)}-" +
                                    "${cal.get(java.util.Calendar.MONTH) + 1}-" +
                                    "${cal.get(java.util.Calendar.DAY_OF_MONTH)}"

                        if (date == expected) {

                            streak++

                            cal.add(
                                java.util.Calendar.DAY_OF_MONTH,
                                -1
                            )

                        } else {

                            break
                        }
                    }

                    tv.text = streak.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =========================
    // TODAY KEY
    // =========================

    private fun getTodayKey(): String {

        val cal =
            java.util.Calendar.getInstance()

        val year =
            cal.get(java.util.Calendar.YEAR)

        val month =
            cal.get(java.util.Calendar.MONTH) + 1

        val day =
            cal.get(java.util.Calendar.DAY_OF_MONTH)

        return "$year-$month-$day"
    }
}