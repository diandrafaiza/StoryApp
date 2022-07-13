package com.example.diandra_storyapp_dicoding.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.diandra_storyapp_dicoding.databinding.ActivityDetailStoryBinding
import com.example.diandra_storyapp_dicoding.model.Story

class DetailStoryActivity : AppCompatActivity() {

    companion object {
        const val DETAIL_STORY = "detail_story"
    }

    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val story = intent.getParcelableExtra<Story>(DETAIL_STORY) as Story
        Glide.with(this)
            .load(story.photo)
            .into(binding.imgDetailPhoto)
        binding.tvDetailTitle.text = story.name
        binding.tvDetailDes.text = story.description
    }
}