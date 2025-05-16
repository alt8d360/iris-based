
package com.irispay.ui.payment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.irispay.R
import com.irispay.authentication.BiometricAuthManager

/**
 * Activity for processing iris-authenticated payments.
 * Includes UI for entering payment details and triggering iris authentication.
 */
class PaymentActivity : AppCompatActivity() {
    
    private lateinit var viewModel: PaymentViewModel
    private lateinit var biometricManager: BiometricAuthManager
    
    // UI Components
    private lateinit var amountInput: EditText
    private lateinit var merchantInput: EditText
    private lateinit var irisPayButton: Button
    private lateinit var pinPayButton: Button
    private lateinit var fingerprintPayButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        
        setupViews()
        setupViewModel()
        setupBiometricManager()
    }
    
    private fun setupViews() {
        amountInput = findViewById(R.id.amount_input)
        merchantInput = findViewById(R.id.merchant_input)
        irisPayButton = findViewById(R.id.iris_pay_button)
        pinPayButton = findViewById(R.id.pin_pay_button)
        fingerprintPayButton = findViewById(R.id.fingerprint_pay_button)
        statusText = findViewById(R.id.status_text)
        progressBar = findViewById(R.id.progress_bar)
        
        // Default merchant to pre-populated value if passed via intent
        intent.getStringExtra("merchant")?.let {
            merchantInput.setText(it)
        }
        
        // Default amount to pre-populated value if passed via intent
        intent.getStringExtra("amount")?.let {
            amountInput.setText(it)
        }
        
        irisPayButton.setOnClickListener {
            if (validateInputs()) {
                initiateIrisPayment()
            }
        }
        
        pinPayButton.setOnClickListener {
            if (validateInputs()) {
                initiatePinPayment()
            }
        }
        
        fingerprintPayButton.setOnClickListener {
            if (validateInputs()) {
                initiateFingerPrintPayment()
            }
        }
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(PaymentViewModel::class.java)
        
        viewModel.paymentResult.observe(this) { result ->
            result?.let {
                if (it.isSuccess) {
                    showSuccessUI(it.transactionId)
                } else {
                    showErrorUI(it.errorMessage)
                }
            }
        }
        
        viewModel.loading.observe(this) { isLoading ->
            if (isLoading) {
                showLoadingUI()
            } else {
                hideLoadingUI()
            }
        }
    }
    
    private fun setupBiometricManager() {
        biometricManager = BiometricAuthManager(this)
        
        // Check if iris or other biometric auth is available
        val canAuthenticate = biometricManager.canAuthenticate()
        
        // Enable/disable buttons based on available authentication methods
        irisPayButton.isEnabled = canAuthenticate
        fingerprintPayButton.isEnabled = canAuthenticate
        
        // Always enable PIN as fallback
        pinPayButton.isEnabled = true
        
        // Set up biometric prompt
        biometricManager.setupBiometricPrompt(
            this,
            onSuccess = { token ->
                // Process payment with the token
                processPayment(token)
            },
            onError = { error ->
                showErrorUI("Authentication failed: $error")
            }
        )
    }
    
    private fun validateInputs(): Boolean {
        val amount = amountInput.text.toString()
        val merchant = merchantInput.text.toString()
        
        if (amount.isBlank()) {
            amountInput.error = "Amount is required"
            return false
        }
        
        try {
            val amountValue = amount.toDouble()
            if (amountValue <= 0) {
                amountInput.error = "Amount must be greater than zero"
                return false
            }
        } catch (e: NumberFormatException) {
            amountInput.error = "Invalid amount format"
            return false
        }
        
        if (merchant.isBlank()) {
            merchantInput.error = "Merchant name is required"
            return false
        }
        
        return true
    }
    
    private fun initiateIrisPayment() {
        statusText.text = "Looking for iris scanner..."
        biometricManager.authenticate()
    }
    
    private fun initiatePinPayment() {
        // In a real app, this would show a PIN entry dialog
        Toast.makeText(this, "PIN authentication would be shown here", Toast.LENGTH_SHORT).show()
        
        // For demo purposes, we'll just simulate a successful PIN auth
        processPayment("PIN_AUTH_TOKEN")
    }
    
    private fun initiateFingerPrintPayment() {
        statusText.text = "Touch fingerprint sensor..."
        biometricManager.authenticate()
    }
    
    private fun processPayment(token: String) {
        val amount = amountInput.text.toString().toDouble()
        val merchant = merchantInput.text.toString()
        
        viewModel.processPayment(amount, merchant, token)
    }
    
    private fun showLoadingUI() {
        progressBar.visibility = View.VISIBLE
        irisPayButton.isEnabled = false
        pinPayButton.isEnabled = false
        fingerprintPayButton.isEnabled = false
        statusText.text = "Processing payment..."
    }
    
    private fun hideLoadingUI() {
        progressBar.visibility = View.GONE
        irisPayButton.isEnabled = true
        pinPayButton.isEnabled = true
        fingerprintPayButton.isEnabled = biometricManager.canAuthenticate()
    }
    
    private fun showSuccessUI(transactionId: String) {
        statusText.text = "Payment successful!\nTransaction ID: $transactionId"
        statusText.setTextColor(getColor(R.color.success_green))
        
        // In a real app, we might navigate to a receipt screen or back to dashboard
        Toast.makeText(this, "Payment completed successfully", Toast.LENGTH_LONG).show()
    }
    
    private fun showErrorUI(error: String) {
        statusText.text = "Payment failed: $error"
        statusText.setTextColor(getColor(R.color.error_red))
    }
}
