
package com.irispay.ui.payment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.irispay.api.ApiService
import com.irispay.api.NetworkModule
import com.irispay.models.PaymentRequest
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel for the payment process.
 * Handles API communication and business logic.
 */
class PaymentViewModel(application: Application) : AndroidViewModel(application) {
    
    private val apiService: ApiService = NetworkModule.provideApiService()
    
    private val _paymentResult = MutableLiveData<PaymentResult>()
    val paymentResult: LiveData<PaymentResult> get() = _paymentResult
    
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    
    /**
     * Processes a payment with the provided details and authentication token.
     */
    fun processPayment(amount: Double, merchant: String, token: String) {
        _loading.value = true
        
        viewModelScope.launch {
            try {
                // Extract device ID from token (in real app, would be properly parsed)
                val deviceId = "device123" // Simplified for demo
                
                val request = PaymentRequest(
                    amount = amount,
                    merchant = merchant,
                    irisToken = token,
                    deviceId = deviceId
                )
                
                val response = apiService.processPayment(request)
                
                _paymentResult.value = PaymentResult(
                    isSuccess = true,
                    transactionId = response.transactionId,
                    amount = response.amount,
                    errorMessage = null
                )
                
            } catch (e: IOException) {
                _paymentResult.value = PaymentResult(
                    isSuccess = false,
                    transactionId = null,
                    amount = null,
                    errorMessage = "Network error: ${e.message}"
                )
            } catch (e: Exception) {
                _paymentResult.value = PaymentResult(
                    isSuccess = false,
                    transactionId = null,
                    amount = null,
                    errorMessage = "Payment failed: ${e.message}"
                )
            } finally {
                _loading.value = false
            }
        }
    }
}

/**
 * Represents the result of a payment operation.
 */
data class PaymentResult(
    val isSuccess: Boolean,
    val transactionId: String?,
    val amount: Double?,
    val errorMessage: String?
)
