import {BrowserRouter as Router, Link, Navigate, Route, Routes} from 'react-router-dom';
import {Home} from './components/Home';
import {PasswordForm} from './components/PasswordForm';
import './App.css';

export default function App() {
    return (
        <Router>
            <div className="app-container">
                <h2><Link to={'/'}>Self Service Portal</Link></h2>
                <Routes>
                    <Route path="/" element={<Home/>}/>
                    <Route path="/reset_password/:resetId" element={<PasswordForm/>}/>
                    <Route path="*" element={<Navigate to="/"/>}/>
                </Routes>
            </div>
        </Router>
    );
}