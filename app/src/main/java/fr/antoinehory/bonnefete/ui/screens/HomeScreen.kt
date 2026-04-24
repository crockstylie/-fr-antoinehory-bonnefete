package fr.antoinehory.bonnefete.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import fr.antoinehory.bonnefete.R

/**
 * Main Home screen of the application.
 * Displays the splash screen as background and navigation buttons at the bottom.
 *
 * @param onNavigateToSettings Callback to navigate to settings screen.
 * @param onNavigateToList Callback to navigate to saints list screen.
 * @param onNavigateToInfo Callback to navigate to info screen.
 */
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToList: () -> Unit,
    onNavigateToInfo: () -> Unit
) {
    val royalBlue = Color(0xFF4169E1)

    Box(modifier = Modifier.fillMaxSize()) {
        // Background: Splash Screen Image
        Image(
            painter = painterResource(id = R.drawable.ic_splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Floating Buttons at the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = onNavigateToSettings,
                containerColor = royalBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres")
            }

            FloatingActionButton(
                onClick = onNavigateToList,
                containerColor = royalBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.List, contentDescription = "Liste des Saints")
            }

            FloatingActionButton(
                onClick = onNavigateToInfo,
                containerColor = royalBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Info, contentDescription = "Informations")
            }
        }
    }
}
