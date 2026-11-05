package com.vozbarrial.core.biometric

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class BiometricAuthManager(
    private val context: Context,
    private val userId: String
) {
    private val keyStoreAlias = "VozBarrialKey_$userId"
    private val androidKeyStore = "AndroidKeyStore"
    private val prefsName = "biometric_prefs_$userId"

    init {
        createKey()
    }

    private fun createKey() {
        try {
            val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
            if (!keyStore.containsAlias(keyStoreAlias)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    androidKeyStore
                )
                val builder = KeyGenParameterSpec.Builder(
                    keyStoreAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(true)
                }

                keyGenerator.init(builder.build())
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getEncryptCipher(): Cipher? {
        return try {
            val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
            val secretKey = keyStore.getKey(keyStoreAlias, null) as? SecretKey ?: return null
            Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}").apply {
                init(Cipher.ENCRYPT_MODE, secretKey)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getDecryptCipher(iv: ByteArray): Cipher? {
        return try {
            val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
            val secretKey = keyStore.getKey(keyStoreAlias, null) as? SecretKey ?: return null
            Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}").apply {
                init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 1. Encriptar y guardar credenciales (llamar al iniciar sesión con contraseña)
     */
    fun encryptAndSaveCredentials(
        activity: FragmentActivity,
        secretValue: String,
        onReady: (Boolean) -> Unit
    ) {
        val cipher = getEncryptCipher()
        if (cipher == null) {
            onReady(false)
            return
        }

        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    try {
                        val authenticatedCipher = result.cryptoObject?.cipher ?: return onReady(false)
                        val encryptedBytes = authenticatedCipher.doFinal(secretValue.toByteArray(Charsets.UTF_8))
                        val iv = authenticatedCipher.iv

                        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                        prefs.edit()
                            .putString("encrypted_data", Base64.encodeToString(encryptedBytes, Base64.DEFAULT))
                            .putString("iv", Base64.encodeToString(iv, Base64.DEFAULT))
                            .apply()
                        onReady(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onReady(false)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onReady(false)
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Activar acceso biométrico")
            .setSubtitle("Confirma tu huella para habilitar el inicio de sesión rápido")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    /**
     * 2. Desencriptar y recuperar credenciales (llamar en la pantalla de login con huella)
     */
    fun decryptCredentials(
        activity: FragmentActivity,
        onSuccess: (String) -> Unit,
        onError: () -> Unit
    ) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val encryptedStr = prefs.getString("encrypted_data", null)
        val ivStr = prefs.getString("iv", null)

        if (encryptedStr == null || ivStr == null) {
            onError()
            return
        }

        val encryptedBytes = Base64.decode(encryptedStr, Base64.DEFAULT)
        val iv = Base64.decode(ivStr, Base64.DEFAULT)

        val cipher = getDecryptCipher(iv)
        if (cipher == null) {
            onError()
            return
        }

        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    try {
                        val authenticatedCipher = result.cryptoObject?.cipher ?: return onError()
                        val decryptedBytes = authenticatedCipher.doFinal(encryptedBytes)
                        val decryptedValue = String(decryptedBytes, Charsets.UTF_8)
                        onSuccess(decryptedValue)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onError()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Iniciar sesión con huella")
            .setSubtitle("Autentícate para acceder a tu cuenta")
            .setNegativeButtonText("Usar contraseña")
            .build()

        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    /**
     * Limpiar credenciales y clave (solo al cambiar de usuario o eliminar cuenta)
     */
    fun clearBiometricData() {
        try {
            val keyStore = KeyStore.getInstance(androidKeyStore).apply { load(null) }
            if (keyStore.containsAlias(keyStoreAlias)) {
                keyStore.deleteEntry(keyStoreAlias)
            }
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).edit().clear().apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
