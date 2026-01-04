package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var cityInput: EditText
    private lateinit var getWeatherBtn: Button
    private lateinit var weatherResult: TextView

    private val apiKey = "f2a06777b8ac29b8087bd69a4ddaf415" // Replace with your OpenWeatherMap API key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityInput = findViewById(R.id.cityInput)
        getWeatherBtn = findViewById(R.id.getWeatherBtn)
        weatherResult = findViewById(R.id.weatherResult)

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
                    weatherResult.text = "Error: ${e.message}"
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

                        val result = "$cityName\nTemperature: $temp °C\nCondition: $condition"

                        runOnUiThread {
                            weatherResult.text = result
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            weatherResult.text = "Parsing error"
                        }
                    }
                }
            }
        })
    }
}
