package com.example.diandra_storyapp_dicoding.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.diandra_storyapp_dicoding.*
import com.example.diandra_storyapp_dicoding.ViewModel.ViewModelFactory
import com.example.diandra_storyapp_dicoding.api.ApiConfig
import com.example.diandra_storyapp_dicoding.api.FileUploadResponse
import com.example.diandra_storyapp_dicoding.databinding.ActivityAddStoryBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*


class AddStoryActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AddStoryActivity"
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10

    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private lateinit var addViewModel: AllViewModel
    private lateinit var activityAddStoryBinding: ActivityAddStoryBinding
    private var getFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAddStoryBinding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(activityAddStoryBinding.root)


        setupViewModel()

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        activityAddStoryBinding.btnCamera.setOnClickListener {
            startCamera()
        }

        activityAddStoryBinding.btnGallery.setOnClickListener {
            startGallery()
        }

        activityAddStoryBinding.btnUpload.setOnClickListener {
            uploadImage()
        }
    }

    private fun setupViewModel() {
        addViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[AllViewModel::class.java]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        val addMenu = menu.findItem(R.id.menu_add)
        addMenu.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_language -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
                return true
            }

            R.id.menu_logout -> {
                addViewModel.logout()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.menu_gmaps -> {
                addViewModel.logout()
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, getString(R.string.permission_deny), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun uploadImage() {
        when {
            activityAddStoryBinding.etDescription.text.toString().isEmpty() -> {
                activityAddStoryBinding.etDescription.error =
                    getString(R.string.invalid_description)
            }

                getFile != null -> {
                val file = reduceFileImage(getFile as File)
                val description = activityAddStoryBinding.etDescription.text.toString()
                    .toRequestBody("text/plain".toMediaType())
                val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )

                addViewModel.getUser().observe(this) {
                    if (it != null) {
                        val client = ApiConfig.getApiService()
                            .uploadStories("Bearer " + it.token, imageMultipart, description)
                        client.enqueue(object : Callback<FileUploadResponse> {
                            override fun onResponse(
                                call: Call<FileUploadResponse>,
                                response: Response<FileUploadResponse>
                            ) {
                                showLoading(false)
                                val responseBody = response.body()
                                Log.d(TAG, "onResponse: $responseBody")
                                if (response.isSuccessful && responseBody?.message == "Story created successfully") {
                                    Toast.makeText(
                                        this@AddStoryActivity,
                                        getString(R.string.upload_success),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent =
                                        Intent(this@AddStoryActivity, StoryActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Log.e(TAG, "onFailure1: ${response.message()}")
                                    Toast.makeText(
                                        this@AddStoryActivity,
                                        getString(R.string.upload_fail),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                                showLoading(false)
                                Log.e(TAG, "onFailure2: ${t.message}")
                                Toast.makeText(
                                    this@AddStoryActivity,
                                    getString(R.string.upload_fail),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                }

            }

        }
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            getFile = myFile

            val result = rotateBitmap(
                BitmapFactory.decodeFile(myFile.path),
                isBackCamera
            )

            activityAddStoryBinding.imgPreview.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddStoryActivity)
            getFile = myFile
            activityAddStoryBinding.imgPreview.setImageURI(selectedImg)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            activityAddStoryBinding.progressBar.visibility = View.VISIBLE
        } else {
            activityAddStoryBinding.progressBar.visibility = View.GONE
        }
    }



}