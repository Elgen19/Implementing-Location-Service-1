package com.prestosa.implementinglocationservice1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*


private const val REQUEST_LOCATION_PERMISSION = 123
private const val REQUEST_GPS_SETTINGS = 456 // Unique request code for GPS settings

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // Handle location updates
                for (location in locationResult.locations) {
                    updateLocationTextView(location.latitude, location.longitude)
                }
            }
        }

        requestLocationServicePermissionAndFetchLocation()
    }

    private fun requestLocationServicePermissionAndFetchLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            checkFetchLocation()
        }
    }

    private fun checkFetchLocation() {
        if (isGPSEnabled())
            startLocationUpdates()
        else {
            Toast.makeText(this, "GPS is disabled. Please enable GPS.", Toast.LENGTH_LONG).show()
            openGPSSettings()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000 // Update interval in milliseconds
        locationRequest.fastestInterval = 5000 // Fastest update interval in milliseconds

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }


    private fun isGPSEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun openGPSSettings() {
        val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(intent, REQUEST_GPS_SETTINGS)
    }

    @SuppressLint("SetTextI18n")
    private fun updateLocationTextView(latitude: Double, longitude: Double) {
        val locationTextView = findViewById<TextView>(R.id.locationTextView)
        locationTextView.text = "Latitude: $latitude, Longitude: $longitude"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location related operations
                checkFetchLocation()
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GPS_SETTINGS) {
            // GPS settings changed, recheck GPS status
            if (isGPSEnabled()) {
                // GPS is enabled now, proceed with location related operations
                startLocationUpdates()
            } else {
                // GPS is still disabled, show a message or handle accordingly
                Toast.makeText(this, "GPS is still disabled.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
