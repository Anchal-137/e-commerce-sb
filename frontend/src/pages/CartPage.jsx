import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import './CartPage.css';

const CartPage = () => {
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        fetchCart();
    }, []);

    const fetchCart = async () => {
        try {
            const response = await api.get('/cart');
            setCart(response.data);
        } catch (error) {
            console.error("Failed to fetch cart", error);
        } finally {
            setLoading(false);
        }
    };

    const removeItem = async (itemId) => {
        try {
            await api.delete(`/cart/items/${itemId}`);
            fetchCart();
        } catch (error) {
            alert('Failed to remove item');
        }
    };

    const checkout = async () => {
        console.log("Starting checkout process...");
        try {
            // 1. Create Order
            console.log("Creating order...");
            const orderResponse = await api.post('/orders', {
                shippingAddress: {
                    street: "123 Main St",
                    city: "Test City",
                    state: "TS",
                    zipCode: "10000",
                    country: "Test Country",
                    phoneNumber: "+1234567890"
                },
                paymentMethod: "Credit Card"
            });
            console.log("Order created:", orderResponse.data);
            const orderId = orderResponse.data.id;
            const amount = orderResponse.data.totalAmount;

            // 2. Process Payment
            console.log(`Processing payment for Order ID: ${orderId}, Amount: ${amount}`);
            const paymentResponse = await api.post('/payment/process', {
                orderId: orderId,
                amount: amount,
                currency: "USD"
            });
            console.log("Payment processed:", paymentResponse.data);

            alert('Order placed successfully! Payment processed.');
            navigate('/');
        } catch (error) {
            console.error("Checkout failed:", error);
            if (error.response) {
                console.error("Error response:", error.response.data);
                alert('Checkout failed: ' + (error.response.data.message || 'Unknown error'));
            } else {
                alert('Checkout failed: ' + error.message);
            }
        }
    };

    if (loading) return <div className="text-center mt-4">Loading cart...</div>;
    if (!cart || cart.items.length === 0) return (
        <div className="container text-center mt-4" style={{ padding: '4rem 0' }}>
            <h2 className="mb-4">Your Cart</h2>
            <p className="text-muted mb-4">Your cart is empty.</p>
            <button onClick={() => navigate('/')} className="btn btn-primary">Start Shopping</button>
        </div>
    );

    return (
        <div className="cart-layout">
            <div className="cart-items container">
                <h2 className="mb-4">Shopping Cart</h2>
                <div className="card" style={{ padding: '0' }}>
                    {cart.items.map((item, index) => (
                        <div key={item.id} className="cart-item" style={{ borderBottom: index === cart.items.length - 1 ? 'none' : '1px solid var(--border)' }}>
                            <div className="cart-item-details">
                                <h3>{item.productName}</h3>
                                <p className="text-muted">Quantity: {item.quantity}</p>
                                <p className="text-muted">Price: ${item.price}</p>
                            </div>
                            <div className="cart-item-actions">
                                <p className="item-subtotal">${item.subTotal}</p>
                                <button onClick={() => removeItem(item.id)} className="btn btn-danger btn-sm">Remove</button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="cart-summary container">
                <div className="card sticky-summary">
                    <h3>Order Summary</h3>
                    <div className="flex-between mt-4 mb-4" style={{ fontSize: '1.2rem', fontWeight: '600' }}>
                        <span>Total</span>
                        <span>${cart.totalAmount}</span>
                    </div>
                    <div className="cart-actions">
                        <button onClick={() => navigate('/')} className="btn btn-outline" style={{ width: '100%', marginBottom: '1rem' }}>
                            Continue Shopping
                        </button>
                        <button onClick={checkout} className="btn btn-success" style={{ width: '100%' }}>
                            Checkout
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CartPage;
