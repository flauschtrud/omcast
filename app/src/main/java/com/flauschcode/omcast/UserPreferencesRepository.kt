package com.flauschcode.omcast

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val USER_PREFERENCES_NAME = "omcast_preferences"

private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

val PLAYLIST_IDS = stringPreferencesKey("playlist_ids")
val RECEIVER_APPLICATION_ID = stringPreferencesKey("receiver_application_id")

data class UserPreferences(
    val playlistIds: String,
    val receiverApplicationId: String
)

suspend fun getUserPreferences(context: Context): UserPreferences {
    val preferences = context.dataStore.data.first().toPreferences()
    val playlistIds = preferences[PLAYLIST_IDS].orEmpty()
    val receiverApplicationId = preferences[RECEIVER_APPLICATION_ID].orEmpty()

    return UserPreferences(playlistIds, receiverApplicationId)
}

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