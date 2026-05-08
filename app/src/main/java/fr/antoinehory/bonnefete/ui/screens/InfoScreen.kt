package fr.antoinehory.bonnefete.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import fr.antoinehory.bonnefete.R

/**
 * Screen displaying information about the application and its developer.
 * Adapted from the divination app.
 *
 * @param onNavigateBack Callback to navigate back to the previous screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_info)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.personal_info_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )

            InfoItemColumn(label = stringResource(R.string.developer_label), value = "Antoine Crock HORY")
            InfoItemColumn(label = stringResource(R.string.website_label), value = "antoinehory.fr", isLink = true, linkUri = "https://antoinehory.fr")
            InfoItemColumn(label = stringResource(R.string.email_label), value = "contact@antoinehory.fr", isLink = true, linkUri = "mailto:contact@antoinehory.fr")
            InfoItemColumn(label = stringResource(R.string.donate_label), value = "paypal.me/kuroku", isLink = true, linkUri = "https://paypal.me/kuroku")

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.find_me_on),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialMediaIcon(
                    iconResId = R.drawable.ic_linkedin_logo,
                    contentDescription = "linkedin_profile",
                    url = "https://www.linkedin.com/in/antoinehory/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_behance_logo,
                    contentDescription = "behance_profile",
                    url = "https://www.behance.net/antoine-hory"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_instagram_logo,
                    contentDescription = "instagram_profile",
                    url = "https://www.instagram.com/antoine.hory.web/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_facebook_logo,
                    contentDescription = "facebook_profile",
                    url = "https://www.facebook.com/antoinehory/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_spotify_logo,
                    contentDescription = "spotify_profile",
                    url = "https://open.spotify.com/user/crockstylie"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_steam_logo,
                    contentDescription = "steam_profile",
                    url = "https://steamcommunity.com/id/crockstylie/"
                )
            }
        }
    }
}

@Composable
fun InfoItemColumn(
    label: String,
    value: String,
    isLink: Boolean = false,
    linkUri: String? = null,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "$label :",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (isLink && linkUri != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(linkUri) }
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * A composable that displays a clickable social media icon.
 * Clicking the icon opens the provided [url].
 *
 * @param iconResId The drawable resource ID for the social media icon.
 * @param contentDescription A textual description of the icon for accessibility.
 * @param url The URL string to open when the icon is clicked.
 * @param modifier Optional [Modifier] to be applied to the IconButton.
 */
@Composable
fun SocialMediaIcon(
    iconResId: Int,
    contentDescription: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current // Used to open URIs.
    IconButton(
        onClick = { uriHandler.openUri(url) }, // Open URL on click.
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.Unspecified, // Use original icon colors, do not apply tint.
            modifier = Modifier.size(40.dp) // Fixed size for the icon.
        )
    }
}
