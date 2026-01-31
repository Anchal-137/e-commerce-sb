import { createContext, useState, useEffect } from 'react';
import api from '../api/axios';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (token) {
            // In a real app, verify token or fetch user profile here
            // For now, assume if token exists, user is logged in
            api.get('/users/me')
                .then(response => setUser(response.data))
                .catch(() => logout())
                .finally(() => setLoading(false));
        } else {
            setLoading(false);
        }
    }, [token]);

    const login = async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        const { accessToken: token } = response.data; // Fix: API returns accessToken
        localStorage.setItem('token', token);
        setToken(token);
        // Fetch user details immediately
        try {
            const userResponse = await api.get('/users/me');
            setUser(userResponse.data);
        } catch (error) {
            console.error("Failed to fetch user details:", error);
            // Optionally decide if we should logout if user fetch fails, 
            // but for now just logging is enough to debug.
        }
    };

    const register = async (username, email, password) => {
        await api.post('/auth/register', { username, email, password, role: 'USER' });
    };

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, register, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};

export default AuthContext;
