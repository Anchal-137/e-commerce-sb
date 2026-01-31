# API Request Bodies for Testing

This document contains sample JSON request bodies for all the API endpoints in the project. You can use these bodies to test the APIs using Postman, cURL, or any other API client.

## 1. Authentication (`/api/auth`)

### Register
**POST** `/api/auth/register`
```json
{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "password123",
  "role": "USER"
}
```
*Note: `role` can be `USER`, `SELLER`, or `ADMIN`.*

### Login
**POST** `/api/auth/login`
```json
{
  "email": "testuser@example.com",
  "password": "password123"
}
```

---

## 2. Products (`/api/products`)

### Create Product (Admin/Seller)
**POST** `/api/products`
```json
{
  "name": "Wireless Headphones",
  "description": "Premium noise-cancelling wireless headphones with 30-hour battery life.",
  "price": 199.99,
  "stock": 50,
  "category": "Electronics",
  "imageUrls": [
    "https://example.com/images/headphones-1.jpg",
    "https://example.com/images/headphones-2.jpg"
  ]
}
```

### Update Product (Admin/Seller)
**PUT** `/api/products/{id}`
*(Same body as Create Product)*

---

## 3. Cart (`/api/cart`)

### Add to Cart
**POST** `/api/cart/items`
```json
{
  "productId": "697e4466a7f5e34c64e7e5ab",
  "quantity": 1
}
```

### Update Cart Item Quantity
**PUT** `/api/cart/items`
```json
{
  "productId": "697e4466a7f5e34c64e7e5ab",
  "quantity": 3
}
```

---

## 4. Orders (`/api/orders`)

### Place Order
**POST** `/api/orders`
```json
{
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA",
    "phoneNumber": "+1234567890"
  },
  "paymentMethod": "Credit Card",
  "notes": "Leave at front door"
}
```

---

## 5. Payment (`/api/payment`)

### Process Payment
**POST** `/api/payment/process`
```json
{
  "orderId": "ORDER_ID_HERE",
  "amount": 199.99,
  "currency": "USD"
}
```

---

## 6. Coupons (`/api/coupons`)

### Create Coupon (Admin)
**POST** `/api/coupons`
```json
{
  "code": "SUMMER2024",
  "description": "Summer Sale Discount",
  "discountType": "PERCENTAGE", 
  "discountValue": 20.0,
  "minimumOrderAmount": 50.0,
  "maximumDiscount": 100.0,
  "usageLimit": 1000,
  "usageLimitPerUser": 1,
  "validFrom": "2024-06-01T00:00:00Z",
  "validUntil": "2024-08-31T23:59:59Z"
}
```
*Note: `discountType` can be `PERCENTAGE` or `FIXED_AMOUNT`.*

### Validate Coupon (User)
**POST** `/api/coupons/validate`
*(Query params: `orderAmount=100.0`)*
```json
{
  "code": "SUMMER2024"
}
```

---

## 7. Reviews (`/api/reviews`)

### Add/Update Review
**POST** `/api/reviews` / **PUT** `/api/reviews/{id}`
```json
{
  "productId": "PRODUCT_ID_HERE",
  "rating": 5,
  "title": "Great Product!",
  "comment": "I absolutely love this product. The quality is amazing and it arrived very quickly."
}
```

---

## 8. Wishlist (`/api/wishlist`)

### Add to Wishlist
**POST** `/api/wishlist/items/{productId}`
*(No body required)*

---

## 9. File Upload (`/api/files`)

### Upload File
**POST** `/api/files/upload`
*Content-Type: `multipart/form-data`*
- Key: `file`
- Value: (Select a file)

---

## 10. Analytics (Admin)

### Get Dashboard Data
**GET** `/api/analytics/dashboard`
*(No body required)*

### Get Sales Analytics
**GET** `/api/analytics/sales?startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z`
*(No body required)*
