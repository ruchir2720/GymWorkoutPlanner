package com.example.gymworkoutplanner

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import java.util.Locale

class ProgressActivity : AppCompatActivity() {

    private lateinit var uid: String

    private lateinit var chartWorkouts: BarChart
    private lateinit var chartCalories: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_progress)

        uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        chartWorkouts =
            findViewById(R.id.chartWorkouts)

        chartCalories =
            findViewById(R.id.chartCalories)

        setupCharts()

        loadDashboardStats()

        setupBMICard()

        setupBottomNav()
    }

    // =====================================================
    // CHART SETUP
    // =====================================================

    private fun setupCharts() {

        val days =
            arrayOf("S", "M", "T", "W", "T", "F", "S")

        // =====================================================
        // WORKOUT CHART
        // =====================================================

        chartWorkouts.apply {

            description.isEnabled = false

            legend.isEnabled = false

            axisRight.isEnabled = false

            setTouchEnabled(false)

            setPinchZoom(false)

            setScaleEnabled(false)

            setDrawGridBackground(false)

            setDrawBorders(false)

            setExtraOffsets(
                16f,
                10f,
                16f,
                10f
            )

            axisLeft.apply {

                // PREMIUM GRID

                setDrawGridLines(true)

                gridColor =
                    Color.parseColor("#18243D")

                gridLineWidth = 0.8f

                // Y AXIS

                setDrawAxisLine(false)

                axisLineColor =
                    Color.TRANSPARENT

                textColor =
                    Color.parseColor("#6E7C99")

                textSize = 10f

                axisMinimum = 0f

                granularity = 1f

                labelCount = 4

                setDrawZeroLine(false)
            }

            xAxis.apply {

                position =
                    XAxis.XAxisPosition.BOTTOM

                valueFormatter =
                    IndexAxisValueFormatter(days)

                textColor =
                    Color.parseColor("#94A3B8")

                textSize = 11f

                setDrawGridLines(false)

                setDrawAxisLine(false)

                axisLineColor =
                    Color.TRANSPARENT

                granularity = 1f

                labelCount = 7
            }
        }

        // =====================================================
        // CALORIES CHART
        // =====================================================

        chartCalories.apply {

            description.isEnabled = false

            legend.isEnabled = false

            axisRight.isEnabled = false

            setTouchEnabled(false)

            setPinchZoom(false)

            setScaleEnabled(false)

            setDrawGridBackground(false)

            setDrawBorders(false)

            setExtraOffsets(
                16f,
                10f,
                16f,
                10f
            )

            axisLeft.apply {

                // PREMIUM GRID

                setDrawGridLines(true)

                gridColor =
                    Color.parseColor("#18243D")

                gridLineWidth = 0.8f

                // Y AXIS

                setDrawAxisLine(false)

                axisLineColor =
                    Color.TRANSPARENT

                textColor =
                    Color.parseColor("#6E7C99")

                textSize = 10f

                axisMinimum = 0f

                granularity = 1f

                labelCount = 4

                setDrawZeroLine(false)
            }

            xAxis.apply {

                position =
                    XAxis.XAxisPosition.BOTTOM

                valueFormatter =
                    IndexAxisValueFormatter(days)

                textColor =
                    Color.parseColor("#94A3B8")

                textSize = 11f

                setDrawGridLines(false)

                setDrawAxisLine(false)

                axisLineColor =
                    Color.TRANSPARENT

                granularity = 1f

                labelCount = 7
            }
        }
    }

    // =====================================================
    // DASHBOARD
    // =====================================================

    private fun loadDashboardStats() {

        loadLastBMI()

        loadCaloriesToday()

        loadWeeklyWorkouts()

        loadWorkoutChart()

        loadCaloriesChart()
    }

    // =====================================================
    // WORKOUT CHART
    // =====================================================

    private fun loadWorkoutChart() {

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("workout_history")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        val counts =
                            FloatArray(7)

                        val cal =
                            Calendar.getInstance()

                        for (dateSnap in snapshot.children) {

                            val raw =
                                dateSnap.key ?: continue

                            val parts =
                                raw.split("-")

                            if (parts.size != 3)
                                continue

                            try {

                                cal.set(
                                    parts[0].toInt(),
                                    parts[1].toInt() - 1,
                                    parts[2].toInt()
                                )

                                val day =
                                    cal.get(
                                        Calendar.DAY_OF_WEEK
                                    ) - 1

                                counts[day] += 1

                            } catch (_: Exception) {}
                        }

                        val entries =
                            mutableListOf<BarEntry>()

                        for (i in counts.indices) {

                            entries.add(
                                BarEntry(
                                    i.toFloat(),
                                    counts[i]
                                )
                            )
                        }

                        val dataSet =
                            BarDataSet(entries, "")

                        dataSet.color =
                            Color.parseColor("#5B8CFF")

                        dataSet.highLightAlpha = 0

                        dataSet.setDrawValues(false)

                        val data =
                            BarData(dataSet)

                        data.barWidth = 0.36f

                        chartWorkouts.data =
                            data

                        chartWorkouts.setFitBars(true)

                        chartWorkouts.animateY(900)

                        chartWorkouts.invalidate()
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {}
                }
            )
    }

    // =====================================================
    // CALORIES CHART
    // =====================================================

    private fun loadCaloriesChart() {

        val entries =
            mutableListOf<Entry>()

        val cal =
            Calendar.getInstance()

        for (i in 0..6) {

            val date =
                getDateKey(cal)

            val index = i

            FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(uid)
                .child("diet")
                .child(date)
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {

                        override fun onDataChange(
                            snapshot: DataSnapshot
                        ) {

                            var total = 0

                            for (child in snapshot.children) {

                                val entry =
                                    child.getValue(
                                        FoodEntry::class.java
                                    )

                                if (entry != null) {

                                    total += entry.calories
                                }
                            }

                            entries.add(
                                Entry(
                                    index.toFloat(),
                                    total.toFloat()
                                )
                            )

                            entries.sortBy { it.x }

                            val dataSet =
                                LineDataSet(entries, "")

                            dataSet.color =
                                Color.parseColor("#5B8CFF")

                            dataSet.lineWidth = 3f

                            dataSet.setDrawCircles(true)

                            dataSet.circleRadius = 5f

                            dataSet.circleHoleRadius = 2.5f

                            dataSet.setCircleColor(
                                Color.WHITE
                            )

                            dataSet.setDrawCircleHole(true)

                            dataSet.circleHoleColor =
                                Color.parseColor("#5B8CFF")

                            dataSet.setDrawValues(false)

                            dataSet.setDrawFilled(true)

                            dataSet.fillDrawable =
                                getDrawable(
                                    R.drawable.bg_chart_gradient
                                )

                            dataSet.mode =
                                LineDataSet.Mode.CUBIC_BEZIER

                            dataSet.cubicIntensity = 0.18f

                            dataSet.highLightColor =
                                Color.TRANSPARENT

                            val data =
                                LineData(dataSet)

                            chartCalories.data =
                                data

                            chartCalories.animateX(1000)

                            chartCalories.invalidate()
                        }

                        override fun onCancelled(
                            error: DatabaseError
                        ) {}
                    }
                )

            cal.add(
                Calendar.DAY_OF_MONTH,
                -1
            )
        }
    }

    // =====================================================
    // BMI
    // =====================================================

    private fun setupBMICard() {

        findViewById<CardView>(R.id.cardBMI)
            .setOnClickListener {

                startActivity(
                    Intent(
                        this,
                        BMIActivity::class.java
                    )
                )
            }
    }

    private fun loadLastBMI() {

        val tvBMI =
            findViewById<TextView>(R.id.tvLastBMI)

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("bmi")
            .get()
            .addOnSuccessListener { snapshot ->

                val bmi =
                    snapshot.getValue(Double::class.java)

                if (bmi != null) {

                    val category = when {

                        bmi < 18.5 ->
                            "Underweight"

                        bmi < 25 ->
                            "Normal"

                        bmi < 30 ->
                            "Overweight"

                        else ->
                            "Obese"
                    }

                    tvBMI.text =
                        String.format(
                            Locale.getDefault(),
                            "Last BMI: %.1f (%s)",
                            bmi,
                            category
                        )

                } else {

                    tvBMI.text =
                        "Last BMI: --"
                }
            }
            .addOnFailureListener {

                tvBMI.text =
                    "Last BMI: --"
            }
    }

    // =====================================================
    // CALORIES TODAY
    // =====================================================

    private fun loadCaloriesToday() {

        val tvCalories =
            findViewById<TextView>(
                R.id.tvCaloriesToday
            )

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("diet")
            .child(getTodayKey())
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        var total = 0

                        for (child in snapshot.children) {

                            val entry =
                                child.getValue(
                                    FoodEntry::class.java
                                )

                            if (entry != null) {

                                total += entry.calories
                            }
                        }

                        tvCalories.text =
                            "$total kcal"
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {}
                }
            )
    }

    // =====================================================
    // WEEKLY WORKOUTS
    // =====================================================

    private fun loadWeeklyWorkouts() {

        val tvWorkouts =
            findViewById<TextView>(
                R.id.tvWorkoutsWeek
            )

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(uid)
            .child("workout_history")
            .addListenerForSingleValueEvent(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        var count = 0

                        val calendar =
                            Calendar.getInstance()

                        val now =
                            calendar.timeInMillis

                        for (dateSnap in snapshot.children) {

                            val raw =
                                dateSnap.key ?: continue

                            val parts =
                                raw.split("-")

                            if (parts.size != 3)
                                continue

                            try {

                                calendar.set(
                                    parts[0].toInt(),
                                    parts[1].toInt() - 1,
                                    parts[2].toInt()
                                )

                                val workoutTime =
                                    calendar.timeInMillis

                                val diffDays =
                                    (now - workoutTime) /
                                            (1000 * 60 * 60 * 24)

                                if (diffDays <= 7) {

                                    count++
                                }

                            } catch (_: Exception) {}
                        }

                        tvWorkouts.text =
                            "$count this week"
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {}
                }
            )
    }

    // =====================================================
    // BOTTOM NAV
    // =====================================================

    private fun setupBottomNav() {

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

                R.id.nav_progress -> true

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

    // =====================================================

    private fun getTodayKey(): String {

        val cal =
            Calendar.getInstance()

        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private fun getDateKey(cal: Calendar): String {

        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
}