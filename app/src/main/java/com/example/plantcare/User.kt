package com.example.plantcare

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class User(
    var uid: String? = null,
    var email: String? = null,
    var plants: List<Plant>? = null)