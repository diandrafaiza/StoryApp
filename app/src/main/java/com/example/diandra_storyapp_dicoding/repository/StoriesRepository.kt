package com.example.diandra_storyapp_dicoding.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.diandra_storyapp_dicoding.api.ApiService
import com.example.diandra_storyapp_dicoding.api.ListStoryItem

class StoriesRepository(private val storiesDatabase: StoryDatabase, private val apiService: ApiService) {
    fun getStoriesForPaging(header: String) : LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
            ),
            pagingSourceFactory = {
                StoriesPagingSource(apiService, header)
            }
        ).liveData
    }
}