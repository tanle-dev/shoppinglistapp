package com.example.shoppinglistapp.view

import android.Manifest
import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.shoppinglistapp.viewModel.LocationViewModel
import com.example.shoppinglistapp.utils.LocationUtils

data class ShoppingItem(
    val id: Int,
    var nameItem: String,
    var quantity: Int,
    var isEditing: Boolean,
    var address: String = "",
    )

@Composable
fun ShoppingListApp(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address: String
) {
    var sItems by remember { mutableStateOf(listOf<ShoppingItem>()) }
    var showDialog by remember {
        mutableStateOf(false)
    }
    var itemName by remember {
        mutableStateOf("")
    }
    var itemQuantity by remember {
        mutableStateOf("1")
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
                permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true){
                // I have access to location
                locationUtils.requestLocationUpdates(viewModel = viewModel)
            }else{
                // Ask for permission
                val retionaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )|| ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (retionaleRequired){
                    Toast.makeText(context, "Location Permission is request for this feature to work.", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(context, "Permission is required. Please enable it in Android Setting.", Toast.LENGTH_LONG).show()
                }
            }
        })
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Add Item")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(sItems) {
                item ->
                if(item.isEditing){
                    ShoppingEditing(item = item, onEditComplete = {
                        editTextName, editTextQuality ->
//                        find which item is editing and set it to false
                        sItems = sItems.map{it.copy(isEditing = false)}
                        val editedItem = sItems.find { it.id == item.id }
                        editedItem?.let {
                            it.nameItem = editTextName
                            it.quantity = editTextQuality
                            it.address = address
                        }
                    })
                }
                else{
                    ShoppingItem(
                        item = item,
                        onEditClick = {
                            sItems = sItems.map{it.copy(isEditing = it.id == item.id)}
                        },
                        onDeleteClick = {
                            sItems = sItems - item
                        }
                    )
                }
            }
        }
    }

    if (showDialog){
       AlertDialog(
           onDismissRequest = { showDialog = false },
           confirmButton = {
                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween
                      )  {
                          Button(onClick = {
                              if(itemName.isNotBlank()){
                                val newItem = ShoppingItem(
                                    sItems.size + 1,
                                    itemName,
                                    itemQuantity.toInt(),
                                    false,
                                    address = address
                                )
                                  sItems += newItem
                                  itemName = ""
                                  showDialog = false
                              }
                          }) {
                              Text(text = "Add")
                          }
                          Button(onClick = { showDialog = false }) {
                              Text(text = "Cancel")
                          }
                      }
           },
           title = {Text(text = "Add Shopping List")},
           text = {
               Column {
                   OutlinedTextField(
                       value = itemName,
                       onValueChange = {itemName = it},
                       singleLine = true,
                       modifier =  Modifier.fillMaxWidth()
                       )
                   Spacer(modifier = Modifier.height(8.dp))
                   OutlinedTextField(
                       value = itemQuantity,
                       onValueChange = {itemQuantity = it},
                       singleLine = true,
                       modifier =  Modifier.fillMaxWidth()
                   )
                   Spacer(modifier = Modifier.height(8.dp))
                   Button(onClick = {
                       if(locationUtils.hasLocationPermission(context)){
                           locationUtils.requestLocationUpdates(viewModel)
                           navController.navigate("locationScreen"){
                               this.launchSingleTop
                           }
                       }else{
                           requestPermissionLauncher.launch(
                               arrayOf(
                                   Manifest.permission.ACCESS_COARSE_LOCATION,
                                   Manifest.permission.ACCESS_FINE_LOCATION
                               )
                           )
                       }
                   }) {
                       Text(text = "Address")
                   }
               }
           }
           )
    }
}

@Composable
fun ShoppingEditing(item: ShoppingItem, onEditComplete: (String, Int) -> Unit){
    var editTextName by remember {
        mutableStateOf(item.nameItem)
    }
    var editTextQuality by remember {
        mutableStateOf(item.quantity.toString())
    }
    var isEditing by remember {
        mutableStateOf(item.isEditing)
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(8.dp),
        horizontalArrangement = Arrangement.Center
        ) {
        Column {
            BasicTextField(
                value = editTextName,
                onValueChange = {editTextName = it},
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
            BasicTextField(
                value = editTextQuality.toString(),
                onValueChange = { editTextQuality = it},
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
        }

        Button(onClick = {
            isEditing = false
            onEditComplete(editTextName, editTextQuality.toIntOrNull() ?: 1)
        }) {
            Text(text = "Save")
        }
    }
}

@Composable
fun ShoppingItem(
    item: ShoppingItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
){
    Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .border(
                    border = BorderStroke(2.dp, Color(0XFF018786)),
                    shape = RoundedCornerShape(20)
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
    ) {


        Column(modifier = Modifier
            .weight(1f)
            .padding(8.dp)) {
            Row {
                Text(text = item.nameItem, modifier = Modifier.padding(8.dp))
                Text(text = "Qty: " + item.quantity.toString(), modifier = Modifier.padding(8.dp))
            }
            Row(modifier = Modifier.fillMaxWidth()){
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Text(text = item.address)
            }
        }

        Row (modifier = Modifier.padding(8.dp)){
            IconButton(onClick = { onEditClick() }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Icon")
            }
            IconButton(onClick = { onDeleteClick() }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
            }
        }
    }
}