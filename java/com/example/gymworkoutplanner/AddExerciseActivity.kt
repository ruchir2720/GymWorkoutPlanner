package com.example.gymworkoutplanner

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddExerciseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_exercise)

        val day = intent.getStringExtra("DAY_NAME") ?: return

        val etName = findViewById<EditText>(R.id.etName)
        val etSets = findViewById<EditText>(R.id.etSets)
        val etReps = findViewById<EditText>(R.id.etReps)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val btnSave = findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {

            val name = etName.text.toString().trim()
            val sets = etSets.text.toString().toIntOrNull()
            val reps = etReps.text.toString().toIntOrNull()
            val weight = etWeight.text.toString().toDoubleOrNull()

            if (name.isEmpty() || sets == null || reps == null || weight == null) {
                Toast.makeText(this, "Fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveWorkoutToFirebase(day, name, sets, reps, weight)
        }
    }

    private fun saveWorkoutToFirebase(
        muscle: String,
        exercise: String,
        sets: Int,
        reps: Int,
        weight: Double
    ) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().reference

        val workoutRef = database.child("users")
            .child(uid)
            .child("workouts")
            .push()

        val workoutMap = HashMap<String, Any>()
        workoutMap["muscle"] = muscle
        workoutMap["exercise"] = exercise
        workoutMap["sets"] = sets
        workoutMap["reps"] = reps
        workoutMap["weight"] = weight
        workoutMap["timestamp"] = System.currentTimeMillis()

        workoutRef.setValue(workoutMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Workout Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show()
            }
    }
}