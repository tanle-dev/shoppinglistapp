package com.example.shoppinglistapp.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.example.shoppinglistapp.utils.LocationUtils
import com.example.shoppinglistapp.viewModel.LocationViewModel
import com.example.shoppinglistapp.ui.theme.ShoppingListAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingListAppTheme {
                // A surface container using the 'background' color from the theme

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }
}

@Composable
fun Navigation(){
    val context = LocalContext.current
    val navController = rememberNavController()
    val locationUtils = LocationUtils(context = context)
    val viewModel: LocationViewModel = viewModel()

    NavHost(navController = navController, startDestination = "shoppingListScreen"){
        composable("shoppingListScreen"){
            ShoppingListApp(
                locationUtils = locationUtils,
                viewModel = viewModel,
                navController = navController,
                context = context,
                address = viewModel.address.value.firstOrNull()?.formatted_address ?: "Not found"
            )
        }
        dialog("locationScreen"){backStack ->
            viewModel.location.value?.let{it1 ->
                LocationSelectionScreen(location = it1, onLocationSelected = {locationData ->
                    Log.d("AAA", "${locationData.latitude} - ${locationData.longitude}")
                    viewModel.fetchAddress("${locationData.latitude},${locationData.longitude}")
                    navController.popBackStack()
                })
            }
        }
    }
}
