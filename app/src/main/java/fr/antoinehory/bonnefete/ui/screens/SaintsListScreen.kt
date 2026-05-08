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
import androidx.compose.ui.res.stringResource
import fr.antoinehory.bonnefete.R
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
    val saints by viewModel.allSaints.collectAsState()

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.saints_calendar_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
                            text = stringResource(
                                R.string.saint_date_format,
                                saint.day,
                                saint.month,
                                saint.name
                            )
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                HorizontalDivider()
            }
        }
    }
}
