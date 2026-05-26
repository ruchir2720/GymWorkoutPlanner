package com.example.gymworkoutplanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class WorkoutActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var day: String
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ExerciseAdapter
    private lateinit var emptyText: TextView

    // =========================
    // HISTORY SECTION
    // =========================

    private lateinit var historyRecycler: RecyclerView
    private lateinit var historyAdapter: WorkoutHistoryAdapter

    private val historyList = mutableListOf<WorkoutHistory>()

    // =========================
    // EXERCISES
    // =========================

    private val exerciseList = mutableListOf<Exercise>()

    private var workoutListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_workout)

        // =========================
        // CURRENT DAY
        // =========================

        day = intent.getStringExtra("DAY_NAME") ?: ""

        // SAVE LAST OPENED WORKOUT

        val prefs =
            getSharedPreferences("gym_data", MODE_PRIVATE)

        prefs.edit()
            .putString("last_workout", day)
            .apply()

        findViewById<TextView>(R.id.tvWorkoutDay).text = day

        // =========================
        // MAIN EXERCISE LIST
        // =========================

        recycler = findViewById(R.id.recyclerExercises)

        recycler.layoutManager =
            LinearLayoutManager(this)

        recycler.isNestedScrollingEnabled = false

        emptyText = findViewById(R.id.tvEmpty)

        // =========================
        // HISTORY LIST
        // =========================

        historyRecycler =
            findViewById(R.id.recyclerWorkoutHistory)

        historyRecycler.layoutManager =
            LinearLayoutManager(this)

        historyRecycler.isNestedScrollingEnabled = false

        historyAdapter =
            WorkoutHistoryAdapter(historyList)

        historyRecycler.adapter = historyAdapter

        // =========================
        // DATABASE
        // =========================

        val uid =
            FirebaseAuth.getInstance().currentUser?.uid
                ?: return

        database =
            FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(uid)
                .child("workouts")

        // =========================
        // ADAPTER
        // =========================

        adapter = ExerciseAdapter(
            this,
            day,
            exerciseList,
            { },
            false
        )

        recycler.adapter = adapter

        // =========================
        // ADD EXERCISE
        // =========================

        findViewById<Button>(R.id.btnAdd)
            .setOnClickListener {

                val intent =
                    Intent(this, AddExerciseActivity::class.java)

                intent.putExtra("DAY_NAME", day)

                startActivity(intent)
            }

        // =========================
        // INITIALIZE
        // =========================

        setupFinishWorkoutButton()

        attachWorkoutListener()

        loadWorkoutHistory()
    }

    // =========================
    // FINISH WORKOUT
    // =========================

    private fun setupFinishWorkoutButton() {

        val finishButton =
            findViewById<Button>(R.id.btnFinishWorkout)

        finishButton.setOnClickListener {

            if (exerciseList.isEmpty()) {

                Toast.makeText(
                    this,
                    "No exercises added",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val uid =
                FirebaseAuth.getInstance().currentUser?.uid
                    ?: return@setOnClickListener

            val todayKey = getTodayKey()

            val historyRef =
                FirebaseDatabase.getInstance()
                    .reference
                    .child("users")
                    .child(uid)
                    .child("workout_history")
                    .child(todayKey)
                    .child(day.lowercase())

            // REMOVE OLD VERSION OF TODAY'S SESSION

            historyRef.removeValue().addOnSuccessListener {

                // SAVE UPDATED FULL SESSION

                for (exercise in exerciseList) {

                    val entryId =
                        historyRef.push().key ?: continue

                    val entry = HashMap<String, Any>()

                    entry["name"] = exercise.name
                    entry["sets"] = exercise.sets
                    entry["reps"] = exercise.reps
                    entry["weight"] = exercise.weight

                    historyRef.child(entryId)
                        .setValue(entry)
                }

                Toast.makeText(
                    this,
                    "Workout Saved",
                    Toast.LENGTH_SHORT
                ).show()

                loadWorkoutHistory()
            }
        }
    }

    // =========================
    // LIVE WORKOUT LISTENER
    // =========================

    private fun attachWorkoutListener() {

        workoutListener =
            object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    exerciseList.clear()

                    for (workoutSnap in snapshot.children) {

                        val muscle =
                            workoutSnap.child("muscle")
                                .getValue(String::class.java)
                                ?: continue

                        if (muscle == day) {

                            val exerciseName =
                                workoutSnap.child("exercise")
                                    .getValue(String::class.java)
                                    ?: continue

                            val sets =
                                workoutSnap.child("sets")
                                    .getValue(Int::class.java)
                                    ?: continue

                            val reps =
                                workoutSnap.child("reps")
                                    .getValue(Int::class.java)
                                    ?: continue

                            val weight =
                                workoutSnap.child("weight")
                                    .getValue(Double::class.java)
                                    ?: 0.0

                            val workoutId =
                                workoutSnap.key ?: continue

                            exerciseList.add(
                                Exercise(
                                    id = workoutId,
                                    name = exerciseName,
                                    sets = sets,
                                    reps = reps,
                                    weight = weight,
                                    muscle = muscle
                                )
                            )
                        }
                    }

                    // EMPTY STATE

                    if (exerciseList.isEmpty()) {

                        emptyText.visibility = View.VISIBLE

                        recycler.visibility = View.GONE

                    } else {

                        emptyText.visibility = View.GONE

                        recycler.visibility = View.VISIBLE
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            }

        database.addValueEventListener(workoutListener!!)
    }

    // =========================
    // LOAD HISTORY
    // =========================

    private fun loadWorkoutHistory() {

        val uid =
            FirebaseAuth.getInstance().currentUser?.uid
                ?: return

        val emptyHistoryText =
            findViewById<TextView>(R.id.tvEmptyHistory)

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("workout_history")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    historyList.clear()

                    // EACH DATE
                    for (dateSnap in snapshot.children) {

                        val rawDate =
                            dateSnap.key ?: continue

                        val muscleSnap =
                            dateSnap.child(day.lowercase())

                        // SKIP IF THIS MUSCLE NOT FOUND
                        if (!muscleSnap.exists()) continue

                        val notesBuilder = StringBuilder()

                        var counter = 1

                        // EACH EXERCISE INSIDE THAT DATE
                        for (exerciseSnap in muscleSnap.children) {

                            val name =
                                exerciseSnap.child("name")
                                    .getValue(String::class.java)
                                    ?: continue

                            val sets =
                                exerciseSnap.child("sets")
                                    .getValue(Int::class.java)
                                    ?: 0

                            val reps =
                                exerciseSnap.child("reps")
                                    .getValue(Int::class.java)
                                    ?: 0

                            val weight =
                                exerciseSnap.child("weight")
                                    .getValue(Double::class.java)
                                    ?: 0.0

                            notesBuilder.append(
                                "$counter. $name • " +
                                        "${sets}x${reps} • " +
                                        "${weight}kg\n"
                            )

                            counter++
                        }

                        // ADD COMPLETE SESSION
                        historyList.add(
                            WorkoutHistory(
                                formatDate(rawDate),
                                notesBuilder.toString().trim()
                            )
                        )
                    }

                    // NEWEST FIRST
                    historyList.reverse()

                    historyAdapter.notifyDataSetChanged()

                    // EMPTY HISTORY STATE

                    if (historyList.isEmpty()) {

                        emptyHistoryText.visibility = View.VISIBLE
                        historyRecycler.visibility = View.GONE

                    } else {

                        emptyHistoryText.visibility = View.GONE
                        historyRecycler.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // =========================
    // TODAY KEY
    // =========================

    private fun getTodayKey(): String {

        val cal = java.util.Calendar.getInstance()

        val year =
            cal.get(java.util.Calendar.YEAR)

        val month =
            cal.get(java.util.Calendar.MONTH) + 1

        val day =
            cal.get(java.util.Calendar.DAY_OF_MONTH)

        return "$year-$month-$day"
    }

    // =========================
    // FORMAT DATE
    // =========================

    private fun formatDate(raw: String): String {

        val parts = raw.split("-")

        if (parts.size != 3) return raw

        val year = parts[0]

        val month = parts[1].toInt()

        val day = parts[2]

        val months = arrayOf(
            "Jan","Feb","Mar","Apr",
            "May","Jun","Jul","Aug",
            "Sep","Oct","Nov","Dec"
        )

        return "$day ${months[month - 1]} $year"
    }

    // =========================
    // CLEANUP
    // =========================

    override fun onDestroy() {

        super.onDestroy()

        workoutListener?.let {

            database.removeEventListener(it)
        }
    }
}