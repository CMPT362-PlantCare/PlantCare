/**
 * The code in this file has been adapted from Professor XD's Week 7 Lecture Demo.
 * Link: https://canvas.sfu.ca/courses/80625/pages/room-database
 */
package com.example.plantcare

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantEntryRepository(private val plantEntryDatabaseDao: PlantEntryDatabaseDao) {

    val allPlantEntries: Flow<List<PlantEntry>> = plantEntryDatabaseDao.getAllPlantEntries()

    fun insert(plantEntry: PlantEntry){
        CoroutineScope(IO).launch {
            plantEntryDatabaseDao.insertPlantEntry(plantEntry)
        }
    }

    suspend fun get(id: Long): PlantEntry = withContext(IO) {
        plantEntryDatabaseDao.getPlantEntry(id)
    }

    fun delete(id: Long){
        CoroutineScope(IO).launch {
            plantEntryDatabaseDao.deletePlantEntry(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(IO).launch {
            plantEntryDatabaseDao.deleteAll()
        }
    }
}