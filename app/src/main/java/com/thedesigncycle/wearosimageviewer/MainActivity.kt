package com.thedesigncycle.wearosimageviewer

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.thedesigncycle.wearosimageviewer.ui.theme.WearOSPhotoGalleryTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WearOSPhotoGalleryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background,

                    ) {
                    Page(context = this)
                }
            }
        }
    }
}


suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(
                task.exception ?: RuntimeException("Unknown task exception")
            )
        }
    }
}

@Composable
fun Page(context: Context) {

    var nodeState = remember {
        mutableListOf<Node>().toMutableStateList()
    }


    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            nodeState.clear()
            nodeState.addAll(Wearable.getNodeClient(context).connectedNodes.await())
        }
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (nodeState.isEmpty()) Text(text = "No watch node connected")

        for (node in nodeState) {
            // if (node.isNearby)
            Text(text = "Connected: ${node.displayName}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        PickPhoto(
            enabled = !nodeState.isEmpty(),
            onPick = {
                sendToWatch(
                    context,
                    it,
                    nodeState
                )
            },

            )
    }
}

@Composable
fun PickPhoto(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onPick: (uri: Uri) -> Unit,
) {


    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onPick(uri)
            }
        }
    )
    Button(
        onClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        enabled = enabled
    ) {
        Text(text = "Pick image")
    }

}


fun sendToWatch(context: Context, uri: Uri, nodes: List<Node>) {

    GlobalScope.launch(Dispatchers.Main) {

        val channelClient = Wearable.getChannelClient(context)
        for (node in nodes) {
            channelClient.openChannel(node.id, "/image").addOnCompleteListener(
                OnCompleteListener {
                    val channel = it.result
                    val cursor: Cursor? = context.contentResolver
                        .query(uri, arrayOf<String>(MediaStore.Images.ImageColumns.DATA), null, null, null)
                    if (cursor != null) {
                        cursor.moveToFirst()
                        val filePath = cursor.getString(0)
                        val fileUri = Uri.parse("file://${filePath}")
                        channelClient.sendFile(channel, fileUri)

                            .addOnCompleteListener(OnCompleteListener {
                                cursor.close()
                                channelClient.close(channel)
                            })

                    }
                })




        }
    }


}


