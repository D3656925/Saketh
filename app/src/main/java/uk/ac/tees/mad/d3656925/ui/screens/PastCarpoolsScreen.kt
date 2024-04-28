package uk.ac.tees.mad.d3656925.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import uk.ac.tees.mad.d3656925.ui.viewmodels.PastCarpoolViewModel
import java.text.SimpleDateFormat
import java.util.Locale


object PastCarpoolsDestination : NavigationDestination {
    override val routeName: String
        get() = "past_carpools"
    override val titleResource: Int
        get() = R.string.profile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastCarpoolsScreen(
    onBack: () -> Unit,
    onCardClick: (String) -> Unit
) {
    val viewModel: PastCarpoolViewModel = hiltViewModel()
    val pastCarpoolStatus by viewModel.pastCarpoolStatus.collectAsState(initial = null)

    val carpoolList = pastCarpoolStatus?.isSuccess
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Past Trips",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "back")
                    }
                }
            )

        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                if (pastCarpoolStatus?.isLoading == true) {
                    item {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    items(carpoolList ?: emptyList()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.elevatedCardElevation(4.dp),
                            onClick = { onCardClick(it.tripId) }
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Trip id: ${it.tripId}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Time: ${
                                        SimpleDateFormat(
                                            "HH:mm",
                                            Locale.getDefault()
                                        ).format(it.startTime)
                                    }",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(100.dp)
                            ) {
                                val pathEffect =
                                    PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
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
                                            text = it.startLocation ?: "Start",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Row {

                                        Text(
                                            text = it.endLocation ?: "End",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}