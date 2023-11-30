package com.example.plantcare

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object Helpers {
//    returns watering frequency in days
    fun getWateringFreq(speciesId: String): String {
        var freq = ""
        val apiKey =  BuildConfig.PLANT_API_KEY
        val url = URL("https://perenual.com/api/species/details/$speciesId?key=$apiKey")

        val apiCall = Thread {
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    inputStream.bufferedReader().use {
                        it.lines().forEach{line->
                            var response = JSONObject(line)
                            var wateringData = response.getJSONObject("watering_general_benchmark")
                            freq = wateringData.getString("value").split("-").toTypedArray()[0]
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        apiCall.start()
        apiCall.join()
        return freq
    }

}