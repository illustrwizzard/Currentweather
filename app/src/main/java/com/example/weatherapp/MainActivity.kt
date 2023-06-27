package com.example.weatherapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var cityEditText: EditText
    private lateinit var checkWeatherButton: Button
    private lateinit var weatherTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityEditText = findViewById(R.id.cityEditText)
        checkWeatherButton = findViewById(R.id.checkWeatherButton)
        weatherTextView = findViewById(R.id.weatherTextView)

        // Set click listener for the Check Weather button
        checkWeatherButton.setOnClickListener {
            val city = cityEditText.text.toString()
            fetchWeatherData(city)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchWeatherData(city: String) {
        val apiKey = "" // Replace with your OpenWeatherMap API key

        // Construct the API URL with the city and API key
        val apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey"

        // Make an API call using coroutines to run it on a background thread
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Create URL object and open connection
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection

                // Get the response code
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response from the API
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    reader.close()

                    // Parse the JSON response
                    val weatherData = parseWeatherData(response.toString())

                    // Update the UI with the weather information
                    runOnUiThread {
                        weatherTextView.text = weatherData
                        weatherTextView.visibility = View.VISIBLE
                    }
                } else {
                    // Show an error message if the API call fails
                    runOnUiThread {
                        weatherTextView.text = "Error: $responseCode"
                        weatherTextView.visibility = View.VISIBLE
                    }
                }

                // Close the connection
                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    weatherTextView.text = "Error: ${e.message}"
                    weatherTextView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun parseWeatherData(response: String): String {
        // Parse the JSON response and extract the relevant weather information
        val jsonObject = JSONObject(response)
        val main = jsonObject.getJSONObject("main")
        val temperature = main.getDouble("temp")
        val temp=temperature-273.15
        val decimalFormat = DecimalFormat("#.##")
        val formattedTemperature = decimalFormat.format(temp)
        val humidity = main.getInt("humidity")
        val weatherArray = jsonObject.getJSONArray("weather")
        val weatherObj = weatherArray.getJSONObject(0)
        val weatherDescription = weatherObj.getString("description")

        return "Temperature: $formattedTemperature %c\nHumidity: $humidity%\nDescription: $weatherDescription"
    }
}
