package com.example.shoppinglistapp.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglistapp.models.GeocodingResult
import com.example.shoppinglistapp.models.LocationData
import com.example.shoppinglistapp.network.RetrofitClient
import kotlinx.coroutines.launch
import java.lang.Exception

class LocationViewModel: ViewModel() {
    private val _location = mutableStateOf<LocationData?>(null)
    val location: State<LocationData?> = _location

    fun updateLocation(newLocation: LocationData){
        _location.value = newLocation
    }

    private val _address = mutableStateOf(listOf<GeocodingResult>())
    val address: State<List<GeocodingResult>> = _address

    fun fetchAddress(latlng: String){
        try {
            viewModelScope.launch {
                val result = RetrofitClient.create().getAddressFromCoordinates(
                    latlng = latlng,
                    apiKey = "AIzaSyCQ585S7HpgQYcPMkkWCjBTJcboKjCqY3s"
                )
                _address.value = result.results
                Log.d("AAA", "${_address.value}")
            }
        }catch (e: Exception){
            Log.d("Fetching API", "${e.message}")
        }
    }
}