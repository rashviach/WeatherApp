package com.android.e1ko0o.weatherapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

class EncryptedSharedPreferences {
    /**
     * Opens an instance of encrypted SharedPreferences
     * @author https://medium.com/android-club/store-data-securely-encryptedsharedpreferences-bff71ac39a55
     *
     * @param fileName                  The name of the file to open; can not contain path separators.
     * @param masterKey                 The master key to use.
     * @param prefKeyEncryptionScheme   The scheme to use for encrypting keys.
     * @param prefValueEncryptionScheme The scheme to use for encrypting values.
     * @return The SharedPreferences instance that encrypts all data.
     * @throws GeneralSecurityException when a bad master key or keyset has been attempted
     * @throws IOException              when fileName can not be used
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun create(
        context: Context,
        fileName: String,
        masterKey: MasterKey,
        prefKeyEncryptionScheme: PrefKeyEncryptionScheme,
        prefValueEncryptionScheme: PrefValueEncryptionScheme
    ): SharedPreferences {
        return create(
            context, fileName, masterKey,
            prefKeyEncryptionScheme, prefValueEncryptionScheme
        )
    }
}