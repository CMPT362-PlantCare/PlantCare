package com.example.plantcare

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val PLANT_API_KEY = "sk-DtSE65602b5ba0a993104"

object Helpers {
    private const val apiKey =  PLANT_API_KEY

    //    returns watering frequency in days
    fun getWateringFreq(speciesId: String): String {
        var freq = ""
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

    fun getCareGuide(speciesId: String): JSONArray {
        var sections = JSONArray()
        val apiKey = PLANT_API_KEY
        val url = URL("https://perenual.com/api/species-care-guide-list?species_id=$speciesId&key=$apiKey")

        val apiCall = Thread {
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    inputStream.bufferedReader().use {
                        it.lines().forEach{line->
                            var response = JSONObject(line)
                            var valueArray = response.getJSONArray("data")
                            for (t in 0 until valueArray.length()) {
                                sections = valueArray.getJSONObject(t).getJSONArray("section")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        apiCall.start()
        apiCall.join()
        return sections
    }

    fun getDefaultImg(speciesId: String): String {
        var defaultImgUrl = ""
        val url = URL("https://perenual.com/api/species/details/$speciesId?key=$apiKey")

        val apiCall = Thread {
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    inputStream.bufferedReader().use {
                        it.lines().forEach{line->
                            var response = JSONObject(line)
                            var defaultImg = response.getJSONObject("default_image")
                            defaultImgUrl = defaultImg.getString("regular_url")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        apiCall.start()
        apiCall.join()
        return defaultImgUrl
    }

}