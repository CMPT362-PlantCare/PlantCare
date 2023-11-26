/**
 * The code in this file has been adapted from Professor XD's Week 7 Lecture Demo.
 * Link: https://canvas.sfu.ca/courses/80625/pages/room-database
 */
package com.example.plantcare

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlantEntry::class], version = 1)
abstract class PlantEntryDatabase: RoomDatabase() {
    abstract val plantEntryDatabaseDao: PlantEntryDatabaseDao
    companion object{
        @Volatile
        private var INSTANCE: PlantEntryDatabase? = null

        fun getInstance(context: Context): PlantEntryDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(context.applicationContext,
                        PlantEntryDatabase::class.java, "plant_entry_table").fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}