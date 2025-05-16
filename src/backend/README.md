
# Iris Pay Backend

This directory contains the backend for the Iris Pay system. In a real implementation, this would be a Python FastAPI application with database integration, JWT authentication, and secure handling of biometric tokens.

## Backend API Structure

### Auth Endpoints

- **POST /login**: Authenticates a user with email and password
  - Returns a JWT token with 1-hour expiry

- **GET /user**: Gets user profile information
  - Requires valid JWT token

### Payment Endpoints

- **POST /pay**: Process an iris-authenticated payment
  - Requires both user JWT and iris token
  - Verifies the hardware-signed biometric token
  - Processes payment via Stripe

### Transaction Endpoints

- **GET /transactions**: Get user transaction history
  - Supports pagination, date filtering
  - Returns transaction details with status

## Security Features

- Never stores raw biometric data
- Only accepts hardware-signed tokens from secure enclave
- JWT-based authentication with short expiry times
- HTTPS/TLS 1.3 for all API communications
- Rate limiting and input validation
- Database encryption for sensitive fields

## Database Schema

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    merchant VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE iris_tokens (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    device_id VARCHAR(255) NOT NULL,
    token_reference VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP WITH TIME ZONE,
    UNIQUE(user_id, device_id)
);
```
