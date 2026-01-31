import { useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    return (
        <nav className="navbar">
            <div className="container nav-container">
                <Link to="/" className="nav-logo">ShopPremium</Link>

                <div className="nav-links">
                    <Link to="/" className="nav-item">Products</Link>
                    {user ? (
                        <>
                            <span className="nav-user">Hi, {user.username}</span>
                            <button onClick={() => navigate('/cart')} className="btn btn-outline nav-btn">
                                Cart
                            </button>
                            <button onClick={logout} className="btn btn-primary nav-btn">
                                Logout
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="btn btn-outline nav-btn">Login</Link>
                            <Link to="/register" className="btn btn-primary nav-btn">Register</Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
