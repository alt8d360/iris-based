
# Iris Pay System

A biometric payment system with iris authentication support, comprehensive web dashboard, and secure API backend.

## Project Components

### Web Dashboard
- React-based dashboard for viewing transaction history
- Responsive design with authentication and user profile
- Transaction filtering and visualization

### Python Backend (Mock Implementation)
- Structured API with auth, payments, and transaction endpoints
- JWT authentication and security best practices
- Mock implementation with FastAPI

### Android Client (Structure)
- Iris authentication with BiometricPrompt API
- Secure token generation using hardware-backed keystore
- Transaction history with filtering and pagination

## Getting Started

### Web Dashboard

1. Clone the repository
2. Install dependencies:
```bash
npm install
```
3. Run the development server:
```bash
npm run dev
```
4. Access the dashboard at http://localhost:8080

### Login Credentials

Use the following credentials to log in:
- Email: any email address
- Password: any password (demo mode)

## Features

### Web Dashboard Features
- User authentication and profile management
- Transaction history with filtering
- Interactive charts for spending analysis
- Responsive design for mobile and desktop

### Security Implementations
- JWT-based authentication
- Hardware-backed biometric tokens
- No storage of raw biometric data

## Project Structure

```
src/
├── pages/                     # React pages
│   ├── Index.tsx              # Login page
│   ├── Dashboard.tsx          # Main dashboard
│   ├── Transactions.tsx       # Transaction history
│   └── Profile.tsx            # User profile
│
├── components/                # React components
│   └── DashboardLayout.tsx    # Shared dashboard layout
│
├── backend/                   # Mock Python backend
│   ├── mock_api.py           # FastAPI implementation
│   └── requirements.txt       # Python dependencies
│
└── android/                   # Android client structure
    ├── BiometricManager.kt    # Iris auth implementation
    ├── PaymentActivity.kt     # Payment flow UI
    └── TransactionsActivity.kt # Transaction history UI
```

## Notes

This is a prototype implementation that demonstrates the structure and approach. In a production environment:

1. The backend would be a separate Python application with proper database integration
2. The Android client would be a complete Gradle project with proper packaging
3. Additional security measures would be implemented for production use
