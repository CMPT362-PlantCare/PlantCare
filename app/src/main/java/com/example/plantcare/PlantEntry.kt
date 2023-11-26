package com.example.plantcare

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_entry_table")
data class PlantEntry(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "plant_name_column")
    var plantName: String = "",

    @ColumnInfo(name = "plant_species_column")
    var plantSpecies: String = "",

    @ColumnInfo(name = "plant_species_id_column")
    var plantSpeciesId: String = "",

    @ColumnInfo(name = "pot_size_column")
    var potSize: Double = 0.0,

    @ColumnInfo(name = "terracotta_pot_column")
    var terracottaPot: Boolean = false,

    @ColumnInfo(name = "drainage_holes_column")
    var drainageHoles: Boolean = false,

    @ColumnInfo(name = "adoption_date_column")
    var adoptionDate: Long = 0L,

    @ColumnInfo(name = "image_uri_column")
    var imageUri: String = "",
)
