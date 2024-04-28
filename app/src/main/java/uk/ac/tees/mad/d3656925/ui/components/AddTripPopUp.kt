package uk.ac.tees.mad.d3656925.ui.components

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import uk.ac.tees.mad.d3656925.utils.location.LocationRepository

@Composable
fun AddTripPopupBox(
    popupWidth: Float = 350f,
    showPopup: Boolean,
    onClickOutside: () -> Unit,
    onConfirm: (startDestination: String, endDestination: String, price: String, time: Long, latitude: Double, longitude: Double) -> Unit,
    isLoading: Boolean = false,
    latitude: Double,
    longitude: Double
) {
    val context = LocalContext.current
    val activity = (context as ComponentActivity)
    val locationManager = LocationRepository(context, activity)
    var startDestination by remember {
        mutableStateOf(
            locationManager.getAddressFromCoordinate(
                latitude, longitude
            )
        )
    }
    var endDestination by remember {
        mutableStateOf("")
    }
    var price by remember {
        mutableStateOf("")
    }
    var time by remember {
        mutableLongStateOf(0L)
    }

    val focusManager = LocalFocusManager.current

    if (showPopup) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(0.5f))
                .zIndex(10F),
            contentAlignment = Alignment.Center
        ) {
            Popup(
                alignment = Alignment.Center,
                properties = PopupProperties(
                    excludeFromSystemGesture = true,
                    focusable = true
                ),
                onDismissRequest = { onClickOutside() }
            ) {
                if (isLoading) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .width(popupWidth.dp)
                            .height(popupWidth.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .width(popupWidth.dp)
                            .background(Color.White), contentAlignment = Alignment.Center
                    ) {

                        Column(
                            modifier = Modifier
                                .padding(40.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Start new trip",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = startDestination,
                                onValueChange = { startDestination = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(text = "Set start location")
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "my location",
                                        modifier = Modifier.clickable {
                                            Log.d(
                                                "LATLONG", "$latitude $longitude"
                                            )
                                            startDestination =
                                                locationManager.getAddressFromCoordinate(
                                                    latitude, longitude
                                                )
                                        }
                                    )
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        focusManager.moveFocus(
                                            FocusDirection.Down
                                        )
                                    }
                                )

                            )
                            OutlinedTextField(
                                value = endDestination,
                                onValueChange = { endDestination = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(text = "Set end Destination")
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        focusManager.moveFocus(
                                            FocusDirection.Down
                                        )
                                    }
                                )
                            )
                            OutlinedTextField(
                                value = price,
                                onValueChange = { price = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text(text = "Price (€)")
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.Decimal
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                    }
                                )
                            )
                            SelectTimeTextField(time = { time = it })
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = {
                                if (startDestination.isNotEmpty() && endDestination.isNotEmpty() && price.isNotEmpty() && time != 0L) {
                                    onConfirm(startDestination, endDestination, price, time, latitude, longitude)
                                } else {
                                    Toast.makeText(context, "Empty fields", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }, shape = RectangleShape, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "Confirm")
                            }
                        }
                    }
                }
            }
        }
    }
}