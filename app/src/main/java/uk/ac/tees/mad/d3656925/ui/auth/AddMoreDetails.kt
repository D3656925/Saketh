package uk.ac.tees.mad.d3656925.ui.auth

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraEnhance
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import java.io.ByteArrayOutputStream

object UserDetailsDestination : NavigationDestination {
    override val routeName: String
        get() = "add_details"
    override val titleResource: Int
        get() = R.string.add_details
}


@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserDetails(
    onSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()

    var profileImage by remember {
        mutableStateOf<ByteArray?>(null)
    }
    var phone by remember {
        mutableStateOf("")
    }
    var rating by remember {
        mutableStateOf(4.5)
    }
    var isDriver by remember {
        mutableStateOf(true)
    }
    var carNumber by remember {
        mutableStateOf("")
    }
    var carName by remember {
        mutableStateOf("")
    }
    var noOfSeats by remember {
        mutableStateOf("2")
    }
    val updateDetailsStatus = viewModel.updateDetailsStatus.collectAsState(initial = null)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val galleryLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                profileImage = handleImageSelection(uri, context)
            }
        }

    val requestCameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                val result = handleImageCapture(it)
                profileImage = result
            }
        }

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
                windowInsets = WindowInsets.ime
            ) {
                // Sheet content
                PhotoPickerOptionBottomSheet(onGalleryClick = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                    galleryLauncher.launch("image/*")
                }, onCameraClick = {
                    scope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                        if (!bottomSheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                    if (!cameraPermission.status.isGranted) {
                        cameraPermission.launchPermissionRequest()
                    }
                    if (cameraPermission.status.isGranted) {
                        requestCameraLauncher.launch(null)
                    }
                })
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Details", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Give us more details so we can serve you better.",
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .border(BorderStroke(2.dp, Color.Black), CircleShape)
                .size(100.dp)
                .clickable {
                    showBottomSheet = true
                }
        ) {
            Box(
                Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomStart
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "",
                    modifier = Modifier.padding(4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(4.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (profileImage == null) {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = "Add photo",
                        tint = Color.Gray,
                        modifier = Modifier.size(70.dp)
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .crossfade(true)
                            .data(profileImage).build(),
                        contentDescription = "Selected image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(text = "Phone Number", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = "Phone")
                    },
                    maxLines = 1,
                    visualTransformation = VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                )
            }
            Column {
                Text(text = "Select Mode:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
//                Spacer(modifier = Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Driver")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = isDriver, onClick = { isDriver = true })
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Passenger")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = !isDriver, onClick = { isDriver = false })
                    }
                }
            }
            if (isDriver) {
                Column {
                    Text(text = "Car number", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = carNumber,
                        onValueChange = {
                            carNumber = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Car number")
                        },
                        maxLines = 1,
                        visualTransformation = VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                    )
                }
                Column {
                    Text(text = "Car name", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = carName,
                        onValueChange = {
                            carName = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "Car name")
                        },
                        maxLines = 1,
                        visualTransformation = VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        }),
                    )
                }
                Column {
                    Text(text = "Number of seats", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = noOfSeats,
                        onValueChange = {
                            noOfSeats = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(text = "2")
                        },
                        maxLines = 1,
                        visualTransformation = VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        }),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))


        Button(
            onClick = {
                if (profileImage != null || phone.isNotEmpty()) {
                    scope.launch {
                        viewModel.addUserDetail(
                            profileImage!!,
                            phone,
                            carNumber,
                            rating,
                            carName,
                            noOfSeats
                        )
                    }
                } else {
                    Toast.makeText(context, "Empty fields", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RectangleShape
        ) {
            if (updateDetailsStatus.value?.isLoading == true) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.background)
            } else {
                Text(text = "Submit", fontSize = 20.sp)
            }
        }

        LaunchedEffect(key1 = updateDetailsStatus.value?.isSuccess) {
            scope.launch {
                if (updateDetailsStatus.value?.isSuccess != null) {
                    val success = updateDetailsStatus.value?.isSuccess
                    Toast.makeText(context, "$success", Toast.LENGTH_LONG).show()
                    onSuccess()
                }
            }
        }

        LaunchedEffect(key1 = updateDetailsStatus.value?.isError) {
            scope.launch {
                if (updateDetailsStatus.value?.isError?.isNotEmpty() == true) {
                    val error = updateDetailsStatus.value?.isError
                    Toast.makeText(context, "$error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}


fun handleImageSelection(uri: Uri, context: Context): ByteArray {
    val bitmap = if (Build.VERSION.SDK_INT < 28) {
        MediaStore.Images
            .Media
            .getBitmap(context.contentResolver, uri)

    } else {
        val source = ImageDecoder
            .createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
    return convertBitmapToByteArray(bitmap)
}

fun handleImageCapture(bitmap: Bitmap): ByteArray {
    return convertBitmapToByteArray(bitmap)
}

fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}


@Composable
fun PhotoPickerOptionBottomSheet(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable {
                    onCameraClick()
                }
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CameraEnhance,
                contentDescription = "",
                modifier = Modifier
                    .padding(16.dp)
                    .size(35.dp)
            )

            Text(
                text = "Camera",
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp
            )
        }
        Row(
            modifier = Modifier
                .clickable {
                    onGalleryClick()
                }
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Image, contentDescription = "",
                modifier = Modifier
                    .padding(16.dp)
                    .size(35.dp)
            )
            Text(
                text = "Gallery",
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp
            )
        }
    }
}
