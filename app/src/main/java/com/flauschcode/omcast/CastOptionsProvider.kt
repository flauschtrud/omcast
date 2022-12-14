package com.flauschcode.omcast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import kotlinx.coroutines.runBlocking

internal class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(appContext: Context): CastOptions {
        return runBlocking {
            val receiverApplicationId = getUserPreferences(appContext).receiverApplicationId

            CastOptions.Builder()
                .setReceiverApplicationId(receiverApplicationId)
                .build()
        }
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}