package fr.antoinehory.bonnefete.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import fr.antoinehory.bonnefete.ui.MainViewModel
import java.util.Locale

/**
 * Screen displaying the complete list of saints.
 * Format: Date (jj / mm) - Nom du saint
 *
 * @param onNavigateBack Callback to navigate back to the previous screen.
 * @param viewModel ViewModel providing the saints data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaintsListScreen(
    onNavigateBack: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val saints by viewModel.allSaints.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendrier des Saints") },
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(saints) { saint ->
                ListItem(
                    headlineContent = {
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "%02d / %02d - %s %s",
                                saint.day,
                                saint.month,
                                saint.title,
                                saint.name
                            ).trim()
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                HorizontalDivider()
            }
        }
    }
}
