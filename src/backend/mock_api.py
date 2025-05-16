
"""
Mock implementation of the Iris Pay FastAPI backend.

This is a simplified version that demonstrates the structure of the API.
In a production environment, this would be a proper FastAPI application
with database integration, proper JWT handling, and security best practices.
"""

from fastapi import FastAPI, Depends, HTTPException, Header, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, EmailStr, Field
from typing import List, Optional
from datetime import datetime, timedelta
import jwt
import hashlib
import uuid
import json
import time
from decimal import Decimal

# Initialize FastAPI app
app = FastAPI(title="Iris Pay API")

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify allowed origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Secret key for JWT encoding/decoding - in production, use environment variable
SECRET_KEY = "your-secret-key-here"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60

# Mock database
mock_users = {
    "demo@example.com": {
        "id": 1,
        "email": "demo@example.com",
        "password_hash": hashlib.sha256("password123".encode()).hexdigest(),
        "name": "Demo User"
    }
}

mock_transactions = []

# Generate some mock transactions
for i in range(1, 51):
    # Random date in the past 90 days
    days_ago = i % 30
    txn_date = datetime.now() - timedelta(days=days_ago)
    
    # Random merchant and amount
    merchants = ["Coffee Shop", "Grocery Store", "Gas Station", "Online Store", "Restaurant"]
    merchant = merchants[i % len(merchants)]
    amount = round(Decimal(5 + (i * 2.5) % 200), 2)
    
    # Random status
    statuses = ["completed", "processing", "failed"]
    status = statuses[i % len(statuses)]
    
    mock_transactions.append({
        "id": i,
        "user_id": 1,
        "transaction_id": f"TXN{1000000 + i}",
        "amount": amount,
        "merchant": merchant,
        "status": status,
        "payment_method": "iris" if i % 3 != 0 else "pin",
        "created_at": txn_date.isoformat()
    })

# Sort transactions by date, newest first
mock_transactions.sort(key=lambda x: x["created_at"], reverse=True)

# Stored iris tokens
mock_iris_tokens = {
    "device123": {
        "user_id": 1,
        "token_reference": "iris_token_ref_123",
        "is_active": True
    }
}

# Pydantic models
class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class TokenResponse(BaseModel):
    access_token: str
    token_type: str
    expires_at: int

class UserResponse(BaseModel):
    id: int
    email: str
    name: str

class PaymentRequest(BaseModel):
    amount: float = Field(..., gt=0)
    merchant: str
    iris_token: str
    device_id: str

class PaymentResponse(BaseModel):
    transaction_id: str
    status: str
    amount: float
    processed_at: str

class TransactionResponse(BaseModel):
    transaction_id: str
    amount: float
    merchant: str
    status: str
    payment_method: str
    created_at: str

class TransactionsListResponse(BaseModel):
    items: List[TransactionResponse]
    total: int
    page: int
    page_size: int

# Helper functions
def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt, int(expire.timestamp())

def get_current_user(authorization: str = Header(...)):
    try:
        token = authorization.replace("Bearer ", "")
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email = payload.get("sub")
        if email is None:
            raise HTTPException(status_code=401, detail="Invalid authentication token")
        
        user = mock_users.get(email)
        if user is None:
            raise HTTPException(status_code=401, detail="User not found")
            
        return user
    except jwt.PyJWTError:
        raise HTTPException(status_code=401, detail="Invalid authentication token")

def verify_iris_token(token: str, device_id: str, user_id: int):
    # In a real implementation, this would validate the JWT/HMAC token
    # against the device's public key or shared secret.
    
    # Check if we have a record of this device
    if device_id not in mock_iris_tokens:
        return False
    
    # Check if the token is active and belongs to this user
    device_record = mock_iris_tokens[device_id]
    if not device_record["is_active"] or device_record["user_id"] != user_id:
        return False
    
    # In reality, we'd do cryptographic verification here
    
    return True

# API endpoints
@app.post("/login", response_model=TokenResponse)
def login(request: LoginRequest):
    user = mock_users.get(request.email)
    if not user:
        # In production, use constant-time comparison to avoid timing attacks
        raise HTTPException(status_code=401, detail="Invalid email or password")
    
    password_hash = hashlib.sha256(request.password.encode()).hexdigest()
    if password_hash != user["password_hash"]:
        raise HTTPException(status_code=401, detail="Invalid email or password")
    
    # Create access token
    token, expires_at = create_access_token({"sub": user["email"]})
    
    return {
        "access_token": token,
        "token_type": "bearer",
        "expires_at": expires_at
    }

@app.get("/user", response_model=UserResponse)
def get_user(current_user: dict = Depends(get_current_user)):
    return {
        "id": current_user["id"],
        "email": current_user["email"],
        "name": current_user["name"]
    }

@app.post("/pay", response_model=PaymentResponse)
def process_payment(
    payment: PaymentRequest,
    current_user: dict = Depends(get_current_user)
):
    # Verify iris token
    if not verify_iris_token(payment.iris_token, payment.device_id, current_user["id"]):
        raise HTTPException(status_code=401, detail="Invalid iris authentication")
    
    # In a real implementation, we would:
    # 1. Call Stripe API to process the payment
    # 2. Store the transaction in the database
    # 3. Return the transaction details
    
    transaction_id = f"TXN{int(time.time())}"
    now = datetime.now().isoformat()
    
    new_transaction = {
        "id": len(mock_transactions) + 1,
        "user_id": current_user["id"],
        "transaction_id": transaction_id,
        "amount": payment.amount,
        "merchant": payment.merchant,
        "status": "completed",
        "payment_method": "iris",
        "created_at": now
    }
    
    mock_transactions.insert(0, new_transaction)
    
    return {
        "transaction_id": transaction_id,
        "status": "completed",
        "amount": payment.amount,
        "processed_at": now
    }

@app.get("/transactions", response_model=TransactionsListResponse)
def get_transactions(
    current_user: dict = Depends(get_current_user),
    page: int = Query(1, ge=1),
    page_size: int = Query(10, ge=1, le=100),
    from_date: Optional[str] = None,
    to_date: Optional[str] = None
):
    # Filter transactions by user
    user_transactions = [tx for tx in mock_transactions if tx["user_id"] == current_user["id"]]
    
    # Apply date filters
    if from_date:
        user_transactions = [tx for tx in user_transactions if tx["created_at"] >= from_date]
    
    if to_date:
        user_transactions = [tx for tx in user_transactions if tx["created_at"] <= to_date]
    
    # Calculate pagination
    total = len(user_transactions)
    start_idx = (page - 1) * page_size
    end_idx = start_idx + page_size
    page_items = user_transactions[start_idx:end_idx]
    
    return {
        "items": [
            {
                "transaction_id": item["transaction_id"],
                "amount": item["amount"],
                "merchant": item["merchant"],
                "status": item["status"],
                "payment_method": item["payment_method"],
                "created_at": item["created_at"]
            }
            for item in page_items
        ],
        "total": total,
        "page": page,
        "page_size": page_size
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
