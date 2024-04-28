package uk.ac.tees.mad.d3656925.ui.screens

import android.Manifest
import android.graphics.Bitmap
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.ac.tees.mad.d3656925.R
import uk.ac.tees.mad.d3656925.navigation.NavigationDestination
import uk.ac.tees.mad.d3656925.ui.auth.PhotoPickerOptionBottomSheet
import uk.ac.tees.mad.d3656925.ui.auth.handleImageCapture
import uk.ac.tees.mad.d3656925.ui.auth.handleImageSelection
import uk.ac.tees.mad.d3656925.ui.viewmodels.EditViewModel
import java.io.ByteArrayOutputStream
import java.net.URL


object EditProfileDestination : NavigationDestination {
    override val routeName: String
        get() = "edit_profile"
    override val titleResource: Int
        get() = R.string.profile
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onSuccess: () -> Unit
) {
    val viewModel: EditViewModel = hiltViewModel()
    val currentUserState = viewModel.currentUserStatus.collectAsState(initial = null)
    var profileImage by remember {
        mutableStateOf<ByteArray?>(null)
    }

    var userName by remember {
        mutableStateOf("")
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(currentUserState.value?.isSuccess) {

        currentUserState.value?.isSuccess?.item?.let {
            scope.launch(Dispatchers.IO) {
                profileImage = imageUrlToByteArray(it.profileImage)
            }
            userName = it.name
        }
    }
    val context = LocalContext.current
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onSuccess) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Edit profile", fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier
            .border(BorderStroke(2.dp, Color.Black), CircleShape)
            .size(100.dp)
            .clickable {
                showBottomSheet = true
            }
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(10f)
            ) {
                Text(
                    text = "Edit",
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)

                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.BottomCenter),
                    color = Color.White
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
                        model = ImageRequest.Builder(LocalContext.current).crossfade(true)
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
                Text(text = "Name", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = "Name")
                    },
                    maxLines = 1,
                    visualTransformation = VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))


        Button(
            onClick = {
                if (profileImage != null || userName.isNotEmpty()) {
                    scope.launch {
                        viewModel.updateUserDetail(
                            userName, profileImage
                        )
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                } else {
                    Toast.makeText(context, "Empty fields", Toast.LENGTH_SHORT).show()
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp), shape = RectangleShape
        ) {
            Text(text = "Submit", fontSize = 20.sp)
        }
    }
}


fun imageUrlToByteArray(imageUrl: String): ByteArray? {
    return try {
        val url = URL(imageUrl)
        val inputStream = url.openStream()
        val outputStream = ByteArrayOutputStream()

        val buffer = ByteArray(1024)
        var bytesRead: Int

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }

        inputStream.close()
        outputStream.toByteArray()
    } catch (e: Exception) {
        e.printStackTrace()
        null // Or handle error appropriately
    }
}