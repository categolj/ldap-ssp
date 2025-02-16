import React, {useState} from 'react';
import {FormInput} from './FormInput';
import {usePasswordValidation} from '../hooks/usePasswordValidation';
import {Violation} from '../types';

export const ChangePasswordForm: React.FC = () => {
    const {formData, errors, handleChange, validateForm, setFormData} = usePasswordValidation({
        userId: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
    });

    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setSuccessMessage(null);
        setErrorMessage(null);

        if (validateForm()) {
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            const {confirmPassword: _unused, ...payload} = formData;
            // Create the Basic auth header using userId and oldPassword
            const authHeader = `Basic ${btoa(`${formData.userId}:${formData.oldPassword}`)}`;
            fetch('/api/change_password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': authHeader,
                },
                body: JSON.stringify(payload),
            })
                .then((response) => {
                    if (response.ok) {
                        setSuccessMessage('Password changed successfully!');
                        // Clear the form by resetting all fields
                        setFormData({
                            userId: '',
                            oldPassword: '',
                            newPassword: '',
                            confirmPassword: '',
                        });
                    } else if (response.status === 401) {
                        setErrorMessage('Authentication failed. Please check your credentials.');
                    } else if (response.status === 400) {
                        // Parse the response to check for specific violation messages
                        response.json().then((errorData) => {
                            if (errorData.violations) {
                                if (
                                    errorData.violations.some(
                                        (violation: Violation) => violation.key
                                            === 'container.greaterThanOrEqual'
                                    )
                                ) {
                                    setErrorMessage('Password must be at least 8 characters.');
                                } else if (
                                    errorData.violations.some(
                                        (violation: Violation) => violation.key
                                            === 'password.required'
                                    )
                                ) {
                                    setErrorMessage(
                                        'Password must include alphanumeric characters.');
                                } else {
                                    setErrorMessage('Invalid input. Please check your data.');
                                }
                            } else {
                                setErrorMessage('Invalid input. Please check your data.');
                            }
                        }).catch(() => {
                            setErrorMessage('Invalid input. Please check your data.');
                        });
                    } else {
                        setErrorMessage('Failed to change password. Please try again.');
                    }
                })
                .catch(() => {
                    setErrorMessage('Failed to change password. Please try again.');
                });
        }
    };

    return (
        <div>
            <h3>Change Password</h3>
            {successMessage && <p className="message success-message">{successMessage}</p>}
            {errorMessage && <p className="message error-message">{errorMessage}</p>}
            <form onSubmit={handleSubmit}>
                <FormInput
                    id="userId"
                    label="User ID"
                    value={formData.userId || ''}
                    onChange={handleChange}
                    error={errors.userId}
                />
                <FormInput
                    id="oldPassword"
                    label="Old Password"
                    type="password"
                    value={formData.oldPassword || ''}
                    onChange={handleChange}
                    error={errors.oldPassword}
                />
                <FormInput
                    id="newPassword"
                    label="New Password"
                    type="password"
                    value={formData.newPassword || ''}
                    onChange={handleChange}
                    error={errors.newPassword}
                />
                <FormInput
                    id="confirmPassword"
                    label="Confirm New Password"
                    type="password"
                    value={formData.confirmPassword || ''}
                    onChange={handleChange}
                    error={errors.confirmPassword}
                />
                <button type="submit">Change Password</button>
            </form>
        </div>
    );
};
