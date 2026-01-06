package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var cityInput: EditText
    private lateinit var getWeatherBtn: Button
    private lateinit var weatherResult: TextView
    private lateinit var humidityResult: TextView
    private lateinit var precipitationResult: TextView
    private lateinit var weatherCard: androidx.cardview.widget.CardView
    private lateinit var weatherIcon: ImageView

    private val apiKey = "f2a06777b8ac29b8087bd69a4ddaf415"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityInput = findViewById(R.id.cityInput)
        getWeatherBtn = findViewById(R.id.getWeatherBtn)
        weatherResult = findViewById(R.id.weatherResult)
        humidityResult = findViewById(R.id.humidityResult)
        precipitationResult = findViewById(R.id.precipitationResult)
        weatherCard = findViewById(R.id.weatherCard)
        weatherIcon = findViewById(R.id.weatherIcon)

        getWeatherBtn.setOnClickListener {
            val city = cityInput.text.toString()
            getWeather(city)
        }
    }

    private fun getWeather(city: String) {
        val client = OkHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    weatherResult.text = "❌ Error: ${e.message}"
                    weatherCard.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonData = response.body?.string()
                    try {
                        val jsonObject = JSONObject(jsonData!!)
                        val cityName = jsonObject.getString("name")
                        val temp = jsonObject.getJSONObject("main").getDouble("temp")
                        val condition = jsonObject.getJSONArray("weather")
                            .getJSONObject(0).getString("description")
                        val iconCode = jsonObject.getJSONArray("weather")
                            .getJSONObject(0).getString("icon")
                        val humidity = jsonObject.getJSONObject("main").getInt("humidity")
                        val precipitation = jsonObject.optJSONObject("rain")?.optDouble("1h", 0.0) ?: 0.0

                        val result = """
                             City: $cityName
                            🌡 Temperature: $temp °C
                            ☁ Condition: ${condition.replaceFirstChar { it.uppercase() }}
                        """.trimIndent()

                        runOnUiThread {
                            weatherResult.text = result
                            humidityResult.text = "💧 Humidity: $humidity%"
                            precipitationResult.text = "🌧 Precipitation: $precipitation mm (last 1h)"
                            weatherCard.visibility = View.VISIBLE

                            // Load weather icon from OpenWeatherMap
                            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                            Thread {
                                try {
                                    val input = java.net.URL(iconUrl).openStream()
                                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                                    runOnUiThread {
                                        weatherIcon.setImageBitmap(bitmap)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }.start()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            weatherResult.text = "⚠️ Parsing error"
                            weatherCard.visibility = View.VISIBLE
                        }
                    }
                }
            }
        })
    }
}


