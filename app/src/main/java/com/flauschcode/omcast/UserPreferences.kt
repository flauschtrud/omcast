package com.flauschcode.omcast

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val USER_PREFERENCES_NAME = "omcast_preferences"
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

val PLAYLIST_IDS = stringPreferencesKey("playlist_ids")
val RECEIVER_APPLICATION_ID = stringPreferencesKey("receiver_application_id")

@Composable
fun <T> rememberPreference(
    key: Preferences.Key<T>,
    defaultValue: T,
): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val state = remember {
        context.dataStore.data
            .map {
                it[key] ?: defaultValue
            }
    }.collectAsState(initial = defaultValue)

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = state.value
                set(value) {
                    coroutineScope.launch {
                        context.dataStore.edit {
                            it[key] = value
                        }
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

@Composable
fun UserPreferencesDialog(playlists: String, onPlaylistsChanged: (String) -> Unit, onDismiss: () -> Unit) {
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
                label = { Text("Playlists") }
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
            "PLui6Eyny-UzwIo3OBXV_KlsWaxUANvWhh", {}, {})
}