package com.example.intentsapp

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.intentsapp.ui.theme.IntentsAppTheme

class MainActivity : ComponentActivity() {

    private var gradeText by mutableStateOf<String?>(null)
    private var isBound = false
    private var myService: MyService? = null

    private val broadcastReceiver = MyBroadcastReceiver()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MyService.MyBinder
            myService = binder.getService()
            isBound = true
            gradeText = myService?.getMyGrade()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            myService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            IntentsAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        gradeText = gradeText,
                        onStartExplicitly = {
                            val intent = Intent(this, SecondActivity::class.java)
                            startActivity(intent)
                        },
                        onStartImplicitly = {
                            val intent = Intent("com.example.intentsapp.ACTION_CHALLENGES").apply {
                                addCategory(Intent.CATEGORY_DEFAULT)
                            }
                            startActivity(intent)
                        },
                        onStartService = {
                            val intent = Intent(this, MyService::class.java)
                            ContextCompat.startForegroundService(this, intent)
                        },
                        onBindService = {
                            val intent = Intent(this, MyService::class.java)
                            bindService(intent, connection, Context.BIND_AUTO_CREATE)
                        },
                        onSendBroadcast = {
                            val intent = Intent(ACTION_MY_BROADCAST)
                                .setPackage(packageName)
                                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                            sendBroadcast(intent)
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ACTION_MY_BROADCAST)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(broadcastReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    companion object {
        const val ACTION_MY_BROADCAST = "com.example.MY_ACTION"
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    gradeText: String?,
    onStartExplicitly: () -> Unit,
    onStartImplicitly: () -> Unit,
    onStartService: () -> Unit,
    onBindService: () -> Unit,
    onSendBroadcast: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Landon Schlangen",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Student ID: 1209690",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onStartExplicitly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Start Activity Explicitly")
        }

        Button(
            onClick = onStartImplicitly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Start Activity Implicitly")
        }

        Button(
            onClick = onStartService,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Start Service")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onBindService) {
                Text(text = "Bind Service")
            }
            Text(text = gradeText ?: "")
        }

        Button(
            onClick = onSendBroadcast,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Send Broadcast")
        }
    }
}
