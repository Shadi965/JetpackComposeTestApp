package com.example.weatherapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.R
import com.example.weatherapp.data.WeatherModel
import com.example.weatherapp.ui.theme.BlueLight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainBackground() {
    Image(
        painter = painterResource(id = R.drawable.main_background),
        contentScale = ContentScale.Crop,
        contentDescription = "Main screen background",
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.5f),
    )
}

@Composable
fun MainCard(
    currentDay: MutableState<WeatherModel>,
    onClickSync: () -> Unit,
    onClickSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        colors = CardDefaults.cardColors(containerColor = BlueLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                    text = currentDay.value.time,
                    fontSize = 15.sp,
                    color = Color.White
                )
                AsyncImage(
                    modifier = Modifier
                        .size(35.dp)
                        .padding(top = 3.dp, end = 8.dp),
                    model = "https:${currentDay.value.iconUrl}",
                    contentDescription = "Weather icon"
                )
            }
            Text(
                text = currentDay.value.city,
                fontSize = 24.sp,
                color = Color.White
            )
            Text(
                text = if (currentDay.value.currentTemp.isEmpty())
                    "${currentDay.value.maxTemp}°C/${currentDay.value.minTemp}°C"
                else
                    "${currentDay.value.currentTemp}°C",
                fontSize = 65.sp,
                color = Color.White
            )
            Text(
                text = currentDay.value.condition,
                fontSize = 16.sp,
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    onClickSearch.invoke()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search icon",
                        tint = Color.White
                    )
                }
                Text(
                    text = if (currentDay.value.currentTemp.isEmpty())
                        ""
                    else
                        "${currentDay.value.maxTemp}°C/${currentDay.value.minTemp}°C",
                    fontSize = 16.sp,
                    color = Color.White
                )
                IconButton(onClick = {
                    onClickSync.invoke()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cloud_sync),
                        contentDescription = "Synchronization icon",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    val tabList = listOf("HOURS", "DAYS")
    val pagerState = rememberPagerState()
    val tabIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .padding(horizontal = 5.dp)
    ) {
        androidx.compose.material.TabRow(
            selectedTabIndex = tabIndex,
            indicator = { pos ->
                androidx.compose.material.TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, pos)
                )
            },
            backgroundColor = BlueLight,
            contentColor = Color.White
        ) {
            tabList.forEachIndexed { index, string ->
                androidx.compose.material.Tab(
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        androidx.compose.material.Text(text = string)
                    })
            }
        }
        HorizontalPager(
            count = tabList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when (index) {
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list = list, currentDay = currentDay)
        }
    }
}

private fun getWeatherByHours(hours: String): List<WeatherModel> {
    if (hours.isEmpty()) return listOf()
    val hoursArray = JSONArray(hours)
    val list = ArrayList<WeatherModel>()
    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        list.add(
            WeatherModel(
                city = "",
                time = item.getString("time"),
                currentTemp = item.getString("temp_c").toFloat().toInt().toString(),
                condition = item.getJSONObject("condition").getString("text"),
                iconUrl = item.getJSONObject("condition").getString("icon"),
                maxTemp = "",
                minTemp = "",
                hours = "",
            )
        )
    }
    return list
}

