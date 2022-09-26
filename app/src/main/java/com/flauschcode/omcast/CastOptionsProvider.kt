package com.flauschcode.omcast

import android.content.Context
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.OptionsProvider

internal class CastOptionsProvider : OptionsProvider {

    private val receiverId = BuildConfig.RECEIVER_ID // TODO make configurable

    override fun getCastOptions(appContext: Context): CastOptions {
        return CastOptions.Builder()
                .setReceiverApplicationId(receiverId)
                .build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
        return null
    }
}