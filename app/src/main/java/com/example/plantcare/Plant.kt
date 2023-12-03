package com.example.plantcare

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class Plant(
    var id: Long? = null,

    var plantName: String? = null,

    var plantSpecies: String? = null,

    var plantSpeciesId: String? = null,

    var potSize: Double? = null,

    var terracottaPot: Boolean? = null,

    var drainageHoles: Boolean? = null,

    var adoptionDate: Long? = null,

    var imageName: String? = null,

    var wateringFreq: Int = 10,

    var lastWateredDate: Long? = null,
)
