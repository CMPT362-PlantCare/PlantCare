/**
 * The code in this file has been adapted from Professor XD's Week 7 Lecture Demo.
 * Link: https://canvas.sfu.ca/courses/80625/pages/room-database
 */
package com.example.plantcare

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class PlantEntryViewModel(private val repository: PlantEntryRepository) : ViewModel() {

    val allPlantEntriesLiveData: LiveData<List<PlantEntry>> =
        repository.allPlantEntries.asLiveData()

    fun insert(plantEntry: PlantEntry) {
        repository.insert(plantEntry)
    }

    suspend fun get(position: Int): PlantEntry? {
        val plantEntryList = allPlantEntriesLiveData.value
        if (!plantEntryList.isNullOrEmpty() && position in plantEntryList.indices) {
            val id = plantEntryList[position].id
            return repository.get(id)
        }
        return null
    }

    fun delete(position: Int) {
        val plantEntryList = allPlantEntriesLiveData.value
        if (!plantEntryList.isNullOrEmpty() && position in plantEntryList.indices) {
            val id = plantEntryList[position].id
            repository.delete(id)
        }
    }

    fun deleteAll() {
        val plantEntryList = allPlantEntriesLiveData.value
        if (!plantEntryList.isNullOrEmpty()) {
            repository.deleteAll()
        }
    }
}