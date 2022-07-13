package com.example.diandra_storyapp_dicoding.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.diandra_storyapp_dicoding.*
import com.example.diandra_storyapp_dicoding.ViewModel.ViewModelFactory
import com.example.diandra_storyapp_dicoding.api.ApiConfig
import com.example.diandra_storyapp_dicoding.api.LoginResponse
import com.example.diandra_storyapp_dicoding.databinding.ActivityMainBinding
import com.example.diandra_storyapp_dicoding.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: AllViewModel
    private lateinit var activityMainBinding: ActivityMainBinding

    companion object {
        private const val TAG = "Main Activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding =ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        setupViewModel()

        activityMainBinding.etPasswordLogin.write = "password"

        activityMainBinding.btnLogin.setOnClickListener {

            val inputEmail = activityMainBinding.etEmailLogin.text.toString()
            val inputPassword = activityMainBinding.etPasswordLogin.text.toString()


            masukAccount(inputEmail, inputPassword)
        }

        activityMainBinding.btnRegister.setOnClickListener {
            val intent = Intent(this, DaftarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupViewModel() {
        mainViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[AllViewModel::class.java]

        mainViewModel.getUser().observe(this) { user ->
            if(user.isLogin) {
                val intent = Intent(this, StoryActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        val addMenu = menu.findItem(R.id.menu_add)
        val logoutMenu = menu.findItem(R.id.menu_logout)

        addMenu.isVisible = false
        logoutMenu.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_language -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
                return true
            }
        }
        return true
    }

    private fun masukAccount(inputEmail: String, inputPassword: String) {

        val client = ApiConfig.getApiService().login(inputEmail, inputPassword)
        client.enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {

                val responseBody = response.body()
                Log.d(TAG, "onResponse: $responseBody")
                if(response.isSuccessful && responseBody?.message == "success") {
                    mainViewModel.saveUser(User(responseBody.loginResult.token, true))
                    Toast.makeText(this@MainActivity, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MainActivity, StoryActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.e(TAG, "onFailure1: ${response.message()}")
                    Toast.makeText(this@MainActivity, getString(R.string.login_fail), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "onFailure2: ${t.message}")
                Toast.makeText(this@MainActivity, getString(R.string.login_fail), Toast.LENGTH_SHORT).show()
            }

        })
    }


}