package fr.antoinehory.bonnefete

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import fr.antoinehory.bonnefete.ui.MainViewModel
import fr.antoinehory.bonnefete.ui.theme.BonneFeteTheme
import fr.antoinehory.bonnefete.ui.screens.HomeScreen
import fr.antoinehory.bonnefete.ui.screens.SaintsListScreen
import fr.antoinehory.bonnefete.ui.screens.InfoScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BonneFeteTheme {
                RequestPermissions()
                
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToList = { navController.navigate("list") },
                            onNavigateToInfo = { navController.navigate("info") }
                        )
                    }
                    composable("settings") {
                        MainScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("list") {
                        SaintsListScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("info") {
                        InfoScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

@Composable
fun RequestPermissions() {
    val context = LocalContext.current
    val permissionsToRequest = mutableListOf<String>()
    
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Handle results if necessary
    }

    LaunchedEffect(Unit) {
        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@Composable
fun MainScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Paramètres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aujourd'hui, nous fêtons :",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${uiState.todaySaint?.title ?: ""} ${uiState.todaySaint?.name ?: "..."}".trim(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Paramètres de notification",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifier uniquement si dans les contacts",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.onlyContacts,
                    onCheckedChange = { viewModel.updateOnlyContacts(it) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Heure de notification",
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", uiState.notificationHour, uiState.notificationMinute),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            Button(onClick = { 
                TimePickerDialog(
                    context,
                    { _, hour, minute -> viewModel.updateNotificationTime(hour, minute) },
                    uiState.notificationHour,
                    uiState.notificationMinute,
                    true
                ).show()
            }) {
                Text("Modifier l'heure")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.testNotification() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tester la notification immédiatement")
            }
        }
    }
}
