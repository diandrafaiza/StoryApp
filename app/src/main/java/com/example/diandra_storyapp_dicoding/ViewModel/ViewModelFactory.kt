package com.example.diandra_storyapp_dicoding.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diandra_storyapp_dicoding.AllViewModel
import com.example.diandra_storyapp_dicoding.UserPreference

class ViewModelFactory(private val pref: UserPreference) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>) : T {
        return when {
            modelClass.isAssignableFrom(AllViewModel::class.java) -> {
                AllViewModel(pref) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}