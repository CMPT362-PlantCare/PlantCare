package com.example.plantcare

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantEntryDatabaseDao {
    @Query("SELECT * FROM plant_entry_table")
    fun getAllPlantEntries(): Flow<List<PlantEntry>>

    @Insert
    suspend fun insertPlantEntry(plantEntry: PlantEntry)

    @Query("SELECT * FROM plant_entry_table WHERE id = :key")
    suspend fun getPlantEntry(key: Long): PlantEntry

    @Query("DELETE FROM plant_entry_table")
    suspend fun deleteAll()

    @Query("DELETE FROM plant_entry_table WHERE id = :key")
    suspend fun deletePlantEntry(key: Long)
}