package com.example.gymworkoutplanner

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DietActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView

    private lateinit var adapter: FoodAdapter

    private val foodList =
        mutableListOf<FoodEntry>()

    private lateinit var calorieProgress: ProgressBar

    private lateinit var tvCaloriesToday: TextView

    private lateinit var tvCurrentCalories: TextView

    // FIXED
    private var dailyGoal = 2200

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_diet)

        // RECYCLER

        recycler =
            findViewById(R.id.recyclerFood)

        recycler.layoutManager =
            LinearLayoutManager(this)

        // CALORIES

        calorieProgress =
            findViewById(R.id.calorieProgress)

        tvCaloriesToday =
            findViewById(R.id.tvCaloriesToday)

        tvCurrentCalories =
            findViewById(R.id.tvCurrentCalories)

        // ADAPTER

        adapter =
            FoodAdapter(foodList)

        recycler.adapter =
            adapter

        // ADD FOOD BUTTON

        findViewById<Button>(R.id.btnAddFood)
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        AddFoodActivity::class.java
                    )
                )
            }

        // SET GOAL BUTTON

        findViewById<TextView>(R.id.btnSetGoal)
            .setOnClickListener {

                showGoalDialog()
            }

        setupBottomNavigation()

        loadDailyGoal()
    }

    private fun showGoalDialog() {

        val editText = EditText(this)

        editText.hint =
            "Enter calorie target"

        editText.setText(
            dailyGoal.toString()
        )

        AlertDialog.Builder(this)
            .setTitle("Set Daily Goal")
            .setView(editText)

            .setPositiveButton("Save") { _, _ ->

                val value =
                    editText.text
                        .toString()
                        .toIntOrNull()

                if (value != null && value > 0) {

                    dailyGoal = value

                    val uid =
                        FirebaseAuth.getInstance()
                            .currentUser?.uid ?: return@setPositiveButton

                    FirebaseDatabase.getInstance()
                        .reference
                        .child("users")
                        .child(uid)
                        .child("dailyGoal")
                        .setValue(value)

                    calorieProgress.max = dailyGoal

                    loadDiet()
                }
            }

            .setNegativeButton(
                "Cancel",
                null
            )

            .show()
    }

    private fun loadDailyGoal() {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("dailyGoal")

            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        val goal =
                            snapshot.getValue(Int::class.java)

                        if (goal != null) {

                            dailyGoal = goal
                        }

                        calorieProgress.max =
                            dailyGoal

                        loadDiet()
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {}
                }
            )
    }

    private fun setupBottomNavigation() {

        val bottomNav =
            findViewById<BottomNavigationView>(
                R.id.bottomNavigation
            )

        bottomNav.selectedItemId =
            R.id.nav_diet

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_workout -> {

                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )

                    overridePendingTransition(
                        0,
                        0
                    )

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

                    overridePendingTransition(
                        0,
                        0
                    )

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

                    overridePendingTransition(
                        0,
                        0
                    )

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

                    overridePendingTransition(
                        0,
                        0
                    )

                    finish()

                    true
                }

                R.id.nav_diet -> true

                else -> false
            }
        }
    }

    private fun loadDiet() {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        val today =
            getTodayKey()

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("diet")
            .child(today)
            .addValueEventListener(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        foodList.clear()

                        var totalCalories = 0

                        for (child in snapshot.children) {

                            val entry =
                                child.getValue(
                                    FoodEntry::class.java
                                )

                            if (entry != null) {

                                foodList.add(
                                    entry.copy(
                                        id = child.key ?: ""
                                    )
                                )

                                totalCalories +=
                                    entry.calories
                            }
                        }

                        adapter.notifyDataSetChanged()

                        calorieProgress.max =
                            dailyGoal

                        calorieProgress.progress =
                            totalCalories

                        tvCurrentCalories.text =
                            totalCalories.toString()

                        tvCaloriesToday.text =
                            "/ $dailyGoal kcal"
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {}
                }
            )
    }

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