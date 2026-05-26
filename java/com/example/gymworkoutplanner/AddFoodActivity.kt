package com.example.gymworkoutplanner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class AddFoodActivity : AppCompatActivity() {

    private lateinit var etFood: EditText
    private lateinit var etCalories: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var loading: TextView

    private lateinit var foodWatcher: TextWatcher

    private val apiKey =
        "gBkWl78ySJ3nkwEJvfr0OGUVj6eeVzLtQmDtyZZA"

    private val handler =
        Handler(Looper.getMainLooper())

    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_food)

        etFood =
            findViewById(R.id.etFood)

        etCalories =
            findViewById(R.id.etCalories)

        recycler =
            findViewById(R.id.recyclerFoodResults)

        recycler.layoutManager =
            LinearLayoutManager(this)

        loading =
            findViewById(R.id.tvLoading)

        recycler.layoutManager =
            LinearLayoutManager(this)

        val btnSave =
            findViewById<Button>(R.id.btnSaveFood)

        btnSave.setOnClickListener {

            val food =
                etFood.text.toString().trim()

            val calories =
                etCalories.text.toString()
                    .toIntOrNull()
                    ?: return@setOnClickListener

            saveFood(food, calories)
        }

        // SEARCH WATCHER

        foodWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

                val query =
                    s.toString()
                        .trim()
                        .lowercase()

                if (query.isEmpty()) {

                    recycler.adapter = null
                    return
                }

                searchRunnable?.let {
                    handler.removeCallbacks(it)
                }

                searchRunnable = Runnable {
                    searchFood(query)
                }

                handler.postDelayed(
                    searchRunnable!!,
                    500
                )
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}
        }

        etFood.addTextChangedListener(foodWatcher)
    }

    private fun saveFood(
        food: String,
        calories: Int
    ) {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        val today =
            getTodayKey()

        val ref =
            FirebaseDatabase.getInstance()
                .reference
                .child("users")
                .child(uid)
                .child("diet")
                .child(today)
                .push()

        val entry =
            FoodEntry(
                id = ref.key ?: "",
                food = food,
                calories = calories,
                date = System.currentTimeMillis()
            )

        ref.setValue(entry)

        finish()
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

    private fun searchFood(food: String) {

        loading.visibility = View.VISIBLE

        val url =
            "https://api.nal.usda.gov/fdc/v1/foods/search?query=$food&api_key=$apiKey&pageSize=15"

        val client =
            OkHttpClient()

        val request =
            Request.Builder()
                .url(url)
                .build()

        client.newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException
                ) {

                    runOnUiThread {

                        loading.visibility =
                            View.GONE
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response
                ) {

                    try {

                        val body =
                            response.body?.string()
                                ?: ""

                        val json =
                            JSONObject(body)

                        val foods =
                            json.optJSONArray("foods")

                        val results =
                            mutableListOf<FoodItem>()

                        if (foods != null) {

                            for (i in 0 until foods.length()) {

                                val item =
                                    foods.getJSONObject(i)

                                val name =
                                    item.optString(
                                        "description",
                                        ""
                                    )

                                if (!name.contains(food, ignoreCase = true)) {
                                    continue
                                }

                                if (name.isBlank())
                                    continue

                                val lowerName =
                                    name.lowercase()

                                if (
                                    lowerName.contains("powder") ||
                                    lowerName.contains("mix") ||
                                    lowerName.contains("protein") ||
                                    lowerName.contains("shake") ||
                                    lowerName.contains("restaurant") ||
                                    lowerName.contains("fast food") ||
                                    lowerName.contains("dried")
                                ) {
                                    continue
                                }

                                if (name.isBlank())
                                    continue

                                val nutrients =
                                    item.optJSONArray(
                                        "foodNutrients"
                                    )

                                var calories = 0.0

                                if (nutrients != null) {

                                    for (j in 0 until nutrients.length()) {

                                        val nutrient =
                                            nutrients.getJSONObject(j)

                                        val nutrientName =
                                            nutrient.optString(
                                                "nutrientName",
                                                ""
                                            )

                                        val unit =
                                            nutrient.optString(
                                                "unitName",
                                                ""
                                            )

                                        if (
                                            nutrientName.equals("Energy", true)
                                            &&
                                            unit.equals("KCAL", true)
                                        ) {

                                            calories =
                                                nutrient.optDouble(
                                                    "value",
                                                    0.0
                                                )

                                            break
                                        }
                                    }
                                }

                                if (calories <= 0)
                                    continue



                                val serving =
                                    item.optString(
                                        "servingSizeUnit",
                                        "100g"
                                    )

                                results.add(
                                    FoodItem(
                                        "$name ($serving)",
                                        calories
                                    )
                                )

                                if (results.size >= 10)
                                    break
                            }
                        }

                        runOnUiThread {

                            loading.visibility =
                                View.GONE

                            recycler.adapter =
                                FoodSearchAdapter(results) { foodItem ->

                                    etFood.removeTextChangedListener(
                                        foodWatcher
                                    )

                                    etFood.setText(
                                        foodItem.name
                                    )

                                    etFood.setSelection(
                                        etFood.text.length
                                    )

                                    etFood.addTextChangedListener(
                                        foodWatcher
                                    )

                                    etCalories.setText(
                                        foodItem.calories
                                            .toInt()
                                            .toString()
                                    )

                                    recycler.adapter = null
                                }
                        }

                    } catch (e: Exception) {

                        runOnUiThread {

                            loading.visibility =
                                View.GONE
                        }
                    }
                }
            })
    }
}