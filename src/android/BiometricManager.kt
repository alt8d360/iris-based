
package com.irispay.authentication

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import java.security.Signature
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Manages biometric authentication including iris recognition on supported devices,
 * with fallback to fingerprint or PIN when necessary.
 */
class BiometricAuthManager(private val context: Context) {

    companion object {
        private const val KEY_NAME = "com.irispay.iris_auth_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    private val biometricManager = BiometricManager.from(context)
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    /**
     * Checks if iris authentication is available on this device.
     * Falls back to other biometric methods if iris is not available.
     */
    fun canAuthenticate(): Boolean {
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Initializes the BiometricPrompt with the appropriate UI and callbacks.
     */
    fun setupBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        executor = ContextCompat.getMainExecutor(context)
        
        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    
                    // Generate and sign a token using the device's secure enclave
                    try {
                        val token = generateSignedToken(result.cryptoObject)
                        onSuccess(token)
                    } catch (e: Exception) {
                        onError("Failed to generate secure token: ${e.message}")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError("Authentication error: $errString")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed")
                }
            })

        // Configure the prompt
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Iris Authentication")
            .setSubtitle("Authenticate with your iris to proceed with payment")
            .setDescription("Look at the camera to verify your identity")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
    }

    /**
     * Starts the authentication process.
     */
    fun authenticate() {
        val signature = generateSignatureObject()
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(signature))
    }

    /**
     * Generates a hardware-backed key for signing.
     */
    private fun generateKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_NAME)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_HMAC_SHA256, 
                ANDROID_KEYSTORE
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            ).apply {
                setDigests(KeyProperties.DIGEST_SHA256)
                setUserAuthenticationRequired(true)
                // Invalidate key if user enrolls new biometrics
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setInvalidatedByBiometricEnrollment(true)
                }
                // Require user authentication for key use (not just generation)
                setUserAuthenticationValidityDurationSeconds(-1)
            }.build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    /**
     * Creates a signature object using the hardware-backed key.
     */
    private fun generateSignatureObject(): Signature {
        generateKey()

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        val key = keyStore.getKey(KEY_NAME, null)
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(key as java.security.PrivateKey)
        
        return signature
    }

    /**
     * Generates a token signed with the hardware-backed key.
     */
    private fun generateSignedToken(cryptoObject: BiometricPrompt.CryptoObject?): String {
        if (cryptoObject?.signature == null) {
            throw IllegalArgumentException("No signature in crypto object")
        }
        
        val signature = cryptoObject.signature!!
        
        // Create a challenge/payload that includes timestamp and device info
        val timestamp = System.currentTimeMillis()
        val deviceId = android.os.Build.SERIAL
        val payload = "IRIS_AUTH:$timestamp:$deviceId"
        
        signature.update(payload.toByteArray())
        val signedData = signature.sign()
        
        // Encode signed data in Base64
        val encodedSignature = Base64.encodeToString(signedData, Base64.NO_WRAP)
        
        // Return token in format that server can validate
        return "$encodedSignature.$payload"
    }
}
