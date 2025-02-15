import React, {useState} from 'react';
import {FormInput} from './FormInput';
import {usePasswordValidation} from '../hooks/usePasswordValidation';

export const RequestResetForm: React.FC = () => {
    const {formData, errors, handleChange, validateForm} = usePasswordValidation({
        email: '',
    });
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setSuccessMessage(null);
        setErrorMessage(null);
        if (validateForm()) {
            fetch('/api/reset_password/send_link', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            }).then(response => {
                if (response.ok) {
                    setSuccessMessage('Reset link sent successfully!');
                } else {
                    setErrorMessage(
                        'Failed to send reset link (Maybe the given email is not found). Please try again.');
                }
            }).catch(() => {
                setErrorMessage('Failed to send reset link. Please try again.');
            });
        }
    };

    return (
        <div>
            <h3>Reset Password Link</h3>
            {successMessage && <p className="message success-message">{successMessage}</p>}
            {errorMessage && <p className="message error-message">{errorMessage}</p>}
            <form onSubmit={handleSubmit}>
                <FormInput
                    id="email"
                    label="Email Address"
                    type="email"
                    value={formData.email || ''}
                    onChange={handleChange}
                    error={errors.email}
                />
                <button type="submit">Send Reset Link</button>
            </form>
        </div>
    );
};