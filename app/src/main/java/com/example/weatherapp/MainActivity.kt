package com.example.weatherapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.android.volley.Request.Method
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.data.WeatherModel
import com.example.weatherapp.screens.MainBackground
import com.example.weatherapp.screens.MainCard
import com.example.weatherapp.screens.SearchDialog
import com.example.weatherapp.screens.TabLayout
import com.example.weatherapp.ui.theme.WeatherAppTheme
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val cityName = remember {
                mutableStateOf("Moscow")
            }
            val dialogState = remember {
                mutableStateOf(false)
            }
            val daysList = remember {
                mutableStateOf(listOf<WeatherModel>())
            }
            val currentDay = remember {
                mutableStateOf(
                    WeatherModel(
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                    )
                )
            }
            if (dialogState.value) {
                SearchDialog(dialogState) {
                    cityName.value = it
                    getData(cityName.value, this, daysList, currentDay)
                }
            }
            getData(cityName.value, this, daysList, currentDay)
            WeatherAppTheme {
                MainBackground()
                Column {
                    MainCard(currentDay, onClickSync = {
                        getData(cityName.value, this@MainActivity, daysList, currentDay)
                    }, {
                        dialogState.value = true
                    })
                    TabLayout(daysList, currentDay)
                }
            }

        }
    }
}

private fun getData(
    city: String,
    context: Context,
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>
) {
    val url = createGetUrl(API_KEY, city, 3)
    val queue = Volley.newRequestQueue(context)
    val stringRequest = StringRequest(
        Method.GET,
        url,
        { response ->
            val list = getWeatherByDays(response)
            currentDay.value = list[0]
            daysList.value = list
        },
        {
            Log.d("MyLog", it.message ?: "Volley error")
        }
    )
    queue.add(stringRequest)
}

private fun createGetUrl(key: String, city: String, days: Int): String {
    return "https://api.weatherapi.com/v1/forecast.json?key=$key&q=$city&days=$days&aqi=no&alerts=no"
}

private fun createGetUrl(key: String, latitude: Float, longitude: Float, days: Int): String {
    return "https://api.weatherapi.com/v1/forecast.json?key=$key&q=$latitude,$longitude&days=$days&aqi=no&alerts=no"
}

private fun getWeatherByDays(response: String): List<WeatherModel> {
    response.ifEmpty { return listOf() }
    val mainObject = JSONObject(response)
    val list = ArrayList<WeatherModel>()
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    val currentDay = days[0] as JSONObject
    list.add(
        WeatherModel(
            city = city,
            time = mainObject.getJSONObject("current").getString("last_updated"),
            currentTemp = mainObject.getJSONObject("current").getString("temp_c").toFloat().toInt()
                .toString(),
            condition = currentDay.getJSONObject("day").getJSONObject("condition")
                .getString("text"),
            iconUrl = currentDay.getJSONObject("day").getJSONObject("condition").getString("icon"),
            maxTemp = currentDay.getJSONObject("day").getString("maxtemp_c").toFloat().toInt()
                .toString(),
            minTemp = currentDay.getJSONObject("day").getString("mintemp_c").toFloat().toInt()
                .toString(),
            hours = currentDay.getJSONArray("hour").toString(),
        )
    )

    for (i in 1 until days.length()) {
        val day = days[i] as JSONObject
        list.add(
            WeatherModel(
                city = city,
                time = day.getString("date"),
                currentTemp = "",
                condition = day.getJSONObject("day").getJSONObject("condition").getString("text"),
                iconUrl = day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                maxTemp = day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt()
                    .toString(),
                minTemp = day.getJSONObject("day").getString("mintemp_c").toFloat().toInt()
                    .toString(),
                hours = day.getJSONArray("hour").toString(),
            )
        )
    }
    return list
}