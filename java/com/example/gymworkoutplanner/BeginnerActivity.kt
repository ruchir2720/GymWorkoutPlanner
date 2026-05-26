package com.example.gymworkoutplanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class BeginnerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_beginner)

        // =========================
        // PREMIUM TUTORIAL LIST
        // =========================

        val tutorials = listOf(

            Tutorial(
                name = "Bodyweight Squats",
                description = "Master the essential foundation of lower body power and structural stability.",
                category = "LOWER BODY",
                difficulty = "EASY",
                image = R.drawable.tutorial_squat,
                videoUrl = "https://www.youtube.com/watch?v=aclHkVaku9U"
            ),

            Tutorial(
                name = "Push-ups",
                description = "Build upper body resilience and core integration with technical push-up form.",
                category = "CHEST & ARMS",
                difficulty = "EASY",
                image = R.drawable.tutorial_pushup,
                videoUrl = "https://www.youtube.com/watch?v=IODxDxX7oi4"
            ),

            Tutorial(
                name = "Plank",
                description = "The ultimate test of isometric core strength and mental focus.",
                category = "CORE",
                difficulty = "MEDIUM",
                image = R.drawable.tutorial_plank,
                videoUrl = "https://www.youtube.com/watch?v=pSHjTRCQxIw"
            ),

            Tutorial(
                name = "Jumping Jacks",
                description = "Ignite your metabolism and improve coordination with this classic movement.",
                category = "CARDIO",
                difficulty = "EASY",
                image = R.drawable.tutorial_jumping,
                videoUrl = "https://www.youtube.com/watch?v=c4DAnQ6DtF8"
            ),

            Tutorial(
                name = "Glute Bridge",
                description = "Develop posterior chain activation and posture correction strength.",
                category = "GLUTES",
                difficulty = "BEGINNER",
                image = R.drawable.tutorial_glute,
                videoUrl = "https://www.youtube.com/watch?v=m2Zx-57cSok"
            )
        )

        // =========================
        // RECYCLER VIEW
        // =========================

        val recycler =
            findViewById<RecyclerView>(R.id.recyclerTutorials)

        recycler.layoutManager =
            LinearLayoutManager(this)

        recycler.adapter =
            TutorialAdapter(this, tutorials)

        recycler.setPadding(
            0,
            10,
            0,
            24
        )

        recycler.clipToPadding = false

        // =========================
        // BOTTOM NAVIGATION
        // =========================

        val bottomNav =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.selectedItemId =
            R.id.nav_beginner

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