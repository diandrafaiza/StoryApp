package com.example.diandra_storyapp_dicoding

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.diandra_storyapp_dicoding.model.User
import kotlinx.coroutines.launch

class AllViewModel(private val pref: UserPreference) : ViewModel() {
    fun getUser() : LiveData<User> {
        return pref.getUser().asLiveData()
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            pref.saveUser(user)
        }
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }
}