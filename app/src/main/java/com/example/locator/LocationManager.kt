package com.example.locator

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.location.FusedLocationProviderClient

class LocationManager(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val context: Context
) {

    @SuppressLint("MissingPermission")
    fun getLocation(callback: (Double?, Double?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.d("Localização", "Localização obtida: Lat $latitude, Long $longitude.")
                    callback(latitude, longitude)
                } else {
                    Log.e("Localização", "Localização nula. Pode ser que o GPS esteja desligado.")
                    callback(null, null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Localização", "Falha ao obter a localização: ${e.message}")
                callback(null, null)
            }
    }
}