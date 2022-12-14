package com.flauschcode.omcast

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun UserPreferencesDialog(playlists: String, onPlaylistsChanged: (String) -> Unit, receiverId: String, onReceiverIdChanged: (String) -> Unit, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = playlists,
                onValueChange = onPlaylistsChanged,
                label = { Text(stringResource(id = R.string.playlist_ids)) }
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = receiverId,
                onValueChange = onReceiverIdChanged,
                label = { Text(stringResource(id = R.string.receiver_application_id)) }
            )

            Button(
                onClick = {
                    onDismiss()
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = stringResource(id = android.R.string.cancel))
            }
        }
    }
}

@Preview
@Composable
fun SettingsPreview() {
    UserPreferencesDialog("PLdJZKgGi4pKDRGaIYi5KjblA21C_qZyw1\n" +
            "PLOhPBXxBh2qBjv9dAzWreaoXwbxpRVALd\n" +
            "PLui6Eyny-Uzyp5P3Vcuv5qCHQOC8W6grN\n" +
            "PLui6Eyny-UzzJ4NSTesh4xRWg4ZWNz5s4\n" +
            "PLui6Eyny-UzzFFfpiil94CUrWKVMaqmkm\n" +
            "PLui6Eyny-UzzkcCfrpXcgUS0wfEGA-kej\n" +
            "PLui6Eyny-UzwIo3OBXV_KlsWaxUANvWhh", {}, "1234", {}, {})
}