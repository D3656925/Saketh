package uk.ac.tees.mad.d3656925.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import uk.ac.tees.mad.d3656925.ui.viewmodels.TripDetailViewModel
import java.text.DecimalFormat

object TripDetailDestination : NavigationDestination {
    override val routeName: String
        get() = "trip_detail"
    override val titleResource: Int
        get() = R.string.trip_detail
    const val tripIdArg = "tripId"
    val routeWithArgs = "$routeName/{$tripIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    onBack: () -> Unit
) {
    val tripDetailViewModel: TripDetailViewModel = hiltViewModel()
    val currentTripStatus = tripDetailViewModel.getTripStatus.collectAsState(initial = null)
    val joinTripStatus = tripDetailViewModel.joinTripStatus.collectAsState(initial = null)
    val hasUserJoined = tripDetailViewModel.hasUserJoined.collectAsState(initial = null)
    val currentTrip = currentTripStatus.value?.isSuccess
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ride Detail",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "bakc")
                    }
                }
            )

        }
    ) {
        Box(modifier = Modifier
            .padding(it)
            .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(70.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(70.dp)
                        ) {
                            if (currentTrip?.driverImage?.isEmpty() == true) {
                                Icon(
                                    imageVector = Icons.Outlined.PersonOutline,
                                    contentDescription = "Profile photo",
                                    tint = Color.Gray
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .crossfade(true)
                                        .data(currentTrip?.driverImage).build(),
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(70.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Yellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = DecimalFormat("#.#")
                                    .format(currentTrip?.driverRating ?: 4.5),
                                color = Color.White,
                                fontSize = 14.sp
                            )

                        }
                    }
                    Column {
                        Text(
                            text = buildAnnotatedString {

                                pushStyle(
                                    SpanStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                append("Car name:   ")
                                pushStyle(
                                    SpanStyle(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                append("${currentTrip?.driverCarName}")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildAnnotatedString {

                                pushStyle(
                                    SpanStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                append("Car number:   ")
                                pushStyle(
                                    SpanStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                append("${currentTrip?.driverCarNumber}")
                            }
                        )

                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    Column(
                        modifier = Modifier
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Start", color = Color.Red)
                        VerticalDivider(
                            modifier = Modifier
                                .weight(1f)
                                .drawWithContent {
                                    drawLine(
                                        color = Color.Red,
                                        start = Offset(0f, 0f),
                                        end = Offset(0f, size.height),
                                        pathEffect = pathEffect
                                    )
                                }
                        )
                        Text(text = "End", color = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(
                        Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Text(
                                text = currentTrip?.startLocation ?: "Start",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row {

                            Text(
                                text = currentTrip?.endLocation ?: "End",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Available seats: ")
                        Text(
                            text = "${currentTrip?.availableSeats ?: 5} seats available",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    HorizontalDivider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Driver's phone: ")
                        Text(
                            text = currentTrip?.driverPhone ?: "9856321470",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    HorizontalDivider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Ride share: ")
                        Text(
                            text = "€${currentTrip?.price ?: 5}",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    if (hasUserJoined.value == true) {
                        Button(
                            onClick = {
                                tripDetailViewModel.joinTrip()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            if (joinTripStatus.value?.isLoading == true) {
                                CircularProgressIndicator()
                            } else {
                                Text(text = "Join")
                            }
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Already joined")
                        }
                    }
                }
                LaunchedEffect(key1 = joinTripStatus.value?.isSuccess) {
                    joinTripStatus.value?.isSuccess?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
                LaunchedEffect(key1 = joinTripStatus.value?.isError) {
                    joinTripStatus.value?.isError?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}