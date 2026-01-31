import { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import AuthContext from '../context/AuthContext';
import './HomePage.css';

const HomePage = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const { user } = useContext(AuthContext);
    const navigate = useNavigate();

    useEffect(() => {
        fetchProducts();
    }, []);

    const fetchProducts = async () => {
        try {
            const response = await api.get('/products');
            setProducts(response.data.content || []);
        } catch (error) {
            console.error("Failed to fetch products", error);
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async (productId) => {
        if (!user) {
            navigate('/login');
            return;
        }
        try {
            await api.post('/cart/items', { productId, quantity: 1 });
            alert('Added to cart');
        } catch (error) {
            alert('Failed to add to cart: ' + (error.response?.data?.message || 'Unknown error'));
        }
    };

    if (loading) return <div className="text-center mt-4">Loading products...</div>;

    return (
        <div className="home-container">
            <header className="hero-section text-center mb-4">
                <h1>Discover Premium Audio</h1>
                <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>High-quality electronics delivered to your door.</p>
            </header>

            <div className="product-grid">
                {products.map(product => (
                    <div key={product.id} className="card product-card">
                        <div className="product-image-placeholder">
                            {/* Placeholder for now, or actual image if available */}
                            <div style={{ background: '#f1f5f9', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#cbd5e1' }}>
                                Product Image
                            </div>
                        </div>
                        <div className="product-info">
                            <h3>{product.name}</h3>
                            <p className="product-desc">{product.description}</p>
                            <div className="flex-between mt-4">
                                <span className="product-price">${product.price}</span>
                                <button onClick={() => addToCart(product.id)} className="btn btn-primary btn-sm">
                                    Add to Cart
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default HomePage;
