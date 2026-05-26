package com.example.gymworkoutplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BMIActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_bmi)

        // =====================================================
        // VIEWS
        // =====================================================

        val feet =
            findViewById<EditText>(R.id.etFeet)

        val inches =
            findViewById<EditText>(R.id.etInches)

        val convertBtn =
            findViewById<Button>(R.id.btnConvert)

        val height =
            findViewById<EditText>(R.id.etHeight)

        val weight =
            findViewById<EditText>(R.id.etWeight)

        val result =
            findViewById<TextView>(R.id.tvResult)

        val calculateBtn =
            findViewById<Button>(R.id.btnCalculate)

        // =====================================================
        // FEET + INCHES → CM
        // =====================================================

        convertBtn.setOnClickListener {

            val f =
                feet.text.toString()
                    .toDoubleOrNull()

            val i =
                inches.text.toString()
                    .toDoubleOrNull()

            if (f != null && i != null) {

                val cm =
                    (f * 30.48) + (i * 2.54)

                height.setText(
                    String.format("%.2f", cm)
                )

            } else {

                result.text =
                    "Enter valid feet and inches"
            }
        }

        // =====================================================
        // BMI CALCULATION
        // =====================================================

        calculateBtn.setOnClickListener {

            val h =
                height.text.toString()
                    .toDoubleOrNull()

            val w =
                weight.text.toString()
                    .toDoubleOrNull()

            if (h != null && w != null) {

                val heightMeters =
                    h / 100

                val bmi =
                    w / (heightMeters * heightMeters)

                // =========================
                // CATEGORY
                // =========================

                val category = when {

                    bmi < 18.5 ->
                        "Underweight"

                    bmi < 24.9 ->
                        "Normal"

                    bmi < 29.9 ->
                        "Overweight"

                    else ->
                        "Obese"
                }

                // =========================
                // SAVE BMI TO FIREBASE
                // =========================

                val uid =
                    FirebaseAuth.getInstance()
                        .currentUser?.uid

                if (uid != null) {

                    FirebaseDatabase.getInstance()
                        .reference
                        .child("users")
                        .child(uid)
                        .child("bmi")
                        .setValue(bmi)
                }

                // =========================
                // SHOW RESULT
                // =========================

                val status = when {

                    bmi < 18.5 -> "⚠ Underweight"

                    bmi < 24.9 -> "✓ Normal"

                    bmi < 29.9 -> "⚠ Overweight"

                    else -> "⚠ Obese"
                }

                result.text =
                    "BMI: %.1f (%s)"
                        .format(bmi, status)

            } else {

                result.text =
                    "Please enter valid values"
            }
        }

        // =====================================================
        // BOTTOM NAVIGATION
        // =====================================================

        val bottomNav =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNav.selectedItemId =
            R.id.nav_progress

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_workout -> {

                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )

                    overridePendingTransition(0, 0)

                    finish()

                    true
                }

                R.id.nav_beginner -> {

                    startActivity(
                        Intent(
                            this,
                            BeginnerActivity::class.java
                        )
                    )

                    overridePendingTransition(0, 0)

                    finish()

                    true
                }

                R.id.nav_friends -> {

                    startActivity(
                        Intent(
                            this,
                            FriendsActivity::class.java
                        )
                    )

                    overridePendingTransition(0, 0)

                    finish()

                    true
                }

                R.id.nav_progress -> {

                    startActivity(
                        Intent(
                            this,
                            ProgressActivity::class.java
                        )
                    )

                    overridePendingTransition(0, 0)

                    finish()

                    true
                }

                R.id.nav_diet -> {

                    startActivity(
                        Intent(
                            this,
                            DietActivity::class.java
                        )
                    )

                    overridePendingTransition(0, 0)

                    finish()

                    true
                }

                else -> false
            }
        }
    }
}