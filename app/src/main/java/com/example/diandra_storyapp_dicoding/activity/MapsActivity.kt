package com.example.diandra_storyapp_dicoding.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.diandra_storyapp_dicoding.*
import com.example.diandra_storyapp_dicoding.ViewModel.ViewModelFactory
import com.example.diandra_storyapp_dicoding.api.ApiConfig
import com.example.diandra_storyapp_dicoding.api.ListStoryItem
import com.example.diandra_storyapp_dicoding.api.StoryResponse

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.diandra_storyapp_dicoding.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mapsActiviy")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapsViewModel: AllViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedClientLoc: FusedLocationProviderClient
    private val _storyLocation = MutableLiveData<List<ListStoryItem>>()
    private val storyLocation: LiveData<List<ListStoryItem>> = _storyLocation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLocationViewModel()
       showDataLocation()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedClientLoc = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        setMapStyle()
        getLocation()

        // Add a marker in Sydney and move the camera

        val tangsel = LatLng(- -6.178306, 106.631889)

        storyLocation.observe(this) {
            for(i in storyLocation.value?.indices!!) {
                val location = LatLng(storyLocation.value?.get(i)?.lat!!, storyLocation.value?.get(i)?.lon!!)
                mMap.addMarker(MarkerOptions().position(location).title(getString(R.string.story_upload) + storyLocation.value?.get(i)?.name))
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tangsel, 2f))
        }
    }

    private val PermissionAccess = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getLocation()
                }
                else -> {
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {return ContextCompat.checkSelfPermission(this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation() {
        if(checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ){
            fusedClientLoc.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    mMap.isMyLocationEnabled = true
                    showLocationMarker(location)
                } else {
                    Toast.makeText(
                        this@MapsActivity,
                        getString(R.string.location),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            PermissionAccess.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showLocationMarker(location: Location) {
        val startLocation = LatLng(location.latitude, location.longitude)
        mMap.addMarker(
            MarkerOptions()
                .position(startLocation)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                .draggable(true)
                .title(getString(R.string.location))
        )
    }

    private fun setupLocationViewModel() {
        mapsViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[AllViewModel::class.java]
    }

    private fun showDataLocation() {
        mapsViewModel.getUser().observe(this) {
            if(it != null) {
                val client = ApiConfig.getApiService().getStoriesWithLocation("Bearer " + it.token, 1)
                client.enqueue(object : Callback<StoryResponse> {
                    override fun onResponse(
                        call: Call<StoryResponse>,
                        response: Response<StoryResponse>
                    ) {
                        val responseBody = response.body()
                        if(response.isSuccessful && responseBody?.message == "Stories fetched successfully") {
                            _storyLocation.value = responseBody.listStory
                        }
                    }

                    override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                        Toast.makeText(this@MapsActivity, getString(R.string.fail_load), Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)

        val mapsMenu = menu.findItem(R.id.menu_gmaps)
        mapsMenu.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {

            R.id.menu_add -> {
                val intent = Intent(this, AddStoryActivity::class.java)
                startActivity(intent)
            }

            R.id.menu_language -> {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
            }

            R.id.menu_logout -> {
                mapsViewModel.logout()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return true
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Error: ", exception)
        }
    }

    companion object {
        const val TAG = "MapsActivity"
    }
}