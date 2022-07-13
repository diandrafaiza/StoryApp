package com.example.diandra_storyapp_dicoding

import android.content.Context
import com.example.diandra_storyapp_dicoding.api.ApiConfig
import com.example.diandra_storyapp_dicoding.repository.StoriesRepository
import com.example.diandra_storyapp_dicoding.repository.StoryDatabase

object Injection {
    fun provideRepository(context: Context): StoriesRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()

        return StoriesRepository(database, apiService)
    }
}