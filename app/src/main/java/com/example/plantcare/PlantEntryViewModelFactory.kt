package com.example.plantcare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PlantEntryViewModelFactory(private val repository: PlantEntryRepository): ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(PlantEntryViewModel::class.java))
            return PlantEntryViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel Class.")
    }
}