package com.example.plantcare

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel() {
    val image = MutableLiveData<Bitmap>()
    var species = MutableLiveData<ArrayList<String>>()
    var id = MutableLiveData<ArrayList<String>>()

    init {
        species.value = ArrayList()
        id.value = ArrayList()
    }
}
