import {BrowserRouter as Router, Link, Navigate, Route, Routes} from 'react-router-dom';
import {Home} from './components/Home';
import {PasswordForm} from './components/PasswordForm';
import './App.css';
import {useEffect, useState} from "react";

export default function App() {
    const [csrfToken, setCsrfToken] = useState<string>('');
    useEffect(() => {
        fetch('/api/csrf', {method: 'GET'})
            .then(response => {
                return response.json();
            })
            .then(data => {
                setCsrfToken(data.csrfToken);
            })
            .catch(error => {
                console.error('Error fetching user data:', error);
            });
    }, []);
    return (
        <Router>
            <div className="app-container">
                <h2><Link to={'/'}>Self Service Portal</Link></h2>
                <Routes>
                    <Route path="/" element={<Home csrfToken={csrfToken}/>}/>
                    <Route path="/reset_password/:resetId"
                           element={<PasswordForm csrfToken={csrfToken}/>}/>
                    <Route path="*" element={<Navigate to="/"/>}/>
                </Routes>
            </div>
        </Router>
    );
}