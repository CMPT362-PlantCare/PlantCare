package com.example.plantcare

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel: ViewModel() {
    val image = MutableLiveData<Bitmap>()

}
