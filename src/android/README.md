
# Iris Pay Android Client

This directory contains the structure for the Android client app for the Iris Pay system. In a real implementation, this would be a complete Kotlin or Java Android application with full iris authentication capabilities.

## Key Components

### Iris Authentication Module

- Uses AndroidX BiometricPrompt API to interact with iris hardware on supported devices
- Fallback mechanism for devices without iris hardware (PIN or fingerprint)
- Secure token generation using hardware-backed keystore

### Network Layer

- Retrofit/OkHttp for API communication
- Certificate pinning for enhanced security
- Interceptors for authentication headers

### UI Components

- Login/Registration activities
- Payment flow with iris authentication
- Transaction history with RecyclerView and pagination

## Directory Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/irispay/
│   │   │   ├── authentication/
│   │   │   │   ├── BiometricManager.kt
│   │   │   │   ├── TokenManager.kt
│   │   │   │   └── AuthRepository.kt
│   │   │   ├── network/
│   │   │   │   ├── ApiService.kt
│   │   │   │   ├── NetworkModule.kt
│   │   │   │   └── models/
│   │   │   ├── ui/
│   │   │   │   ├── login/
│   │   │   │   ├── payment/
│   │   │   │   └── transactions/
│   │   │   └── utils/
│   │   └── res/
│   │       ├── layout/
│   │       ├── values/
│   │       └── drawable/
│   └── test/
└── build.gradle
```

## Implementation Notes

### Iris Authentication

- On devices with iris hardware, the app will use the BiometricPrompt API with BIOMETRIC_STRONG authentication
- The authentication will generate a cryptographic key stored in the device's secure enclave
- This key will sign a challenge from the server to prove user identity

### Secure Communication

- All network requests use HTTPS with TLS 1.3
- Certificate pinning prevents man-in-the-middle attacks
- Response data is verified for integrity

### Transaction History

- The app uses a paged RecyclerView for efficient loading of transaction history
- Pull-to-refresh functionality for latest transactions
- Infinite scroll with pagination
