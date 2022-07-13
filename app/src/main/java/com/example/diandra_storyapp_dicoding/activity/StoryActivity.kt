package com.example.diandra_storyapp_dicoding.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.diandra_storyapp_dicoding.*
import com.example.diandra_storyapp_dicoding.ViewModel.StoryViewModel
import com.example.diandra_storyapp_dicoding.ViewModel.ViewModelFactory
import com.example.diandra_storyapp_dicoding.databinding.ActivityStoryBinding
import com.example.diandra_storyapp_dicoding.repository.LoadingStateAdapter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class StoryActivity : AppCompatActivity() {

    private lateinit var ViewModel: AllViewModel
    private val storyViewModel: StoryViewModel by viewModels {
        StoryViewModel.ViewModelFactory(this)
    }
    private lateinit var binding: ActivityStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewModelStory()

        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager

        getStoriesData()
    }

    private fun ViewModelStory() {
        ViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[AllViewModel::class.java]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_gmaps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_add -> {
                val intent = Intent(this, AddStoryActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_language -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
            }

            R.id.menu_logout -> {
                ViewModel.logout()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return true
    }

    private fun getStoriesData() {
        val adapter = StoryAdapter()
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        ViewModel.getUser().observe(this) { userAuth ->
            if(userAuth != null) {
                storyViewModel.stories("Bearer " + userAuth.token).observe(this) { stories ->
                    adapter.submitData(lifecycle, stories)
                }
            }
        }
    }
}