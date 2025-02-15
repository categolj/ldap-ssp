import React, {useState} from 'react';
import {useParams} from 'react-router-dom';
import {FormInput} from './FormInput';
import {usePasswordValidation} from '../hooks/usePasswordValidation';

export const PasswordForm: React.FC = () => {
    // Retrieve resetId from the URL
    const {resetId} = useParams<{ resetId: string }>();

    // Initialize form with newPassword and confirmPassword fields.
    // Assume the hook provides setFormData for resetting the form.
    const {formData, errors, handleChange, validateForm, setFormData} = usePasswordValidation({
        newPassword: '',
        confirmPassword: '',
    });

    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [warningMessage, setWarningMessage] = useState<string | null>(null);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        // Reset previous messages
        setSuccessMessage(null);
        setErrorMessage(null);
        setWarningMessage(null);

        if (validateForm()) {
            // Create payload with new password and resetId
            const payload = {
                password: formData.newPassword,
                resetId: resetId,
            };

            fetch('/api/reset_password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            })
                .then((response) => {
                    if (response.ok) {
                        setSuccessMessage('Password reset successfully!');
                        // Clear the form fields after successful reset
                        setFormData({
                            newPassword: '',
                            confirmPassword: '',
                        });
                    } else if (response.status === 410) {
                        setWarningMessage('Reset link expired. Please request a new reset link.');
                    } else if (response.status === 400) {
                        // Parse the JSON response to check for specific violation keys
                        response.json().then((errorData) => {
                            if (errorData.violations) {
                                if (
                                    errorData.violations.some(
                                        (violation: any) => violation.key
                                            === 'container.greaterThanOrEqual'
                                    )
                                ) {
                                    setErrorMessage('Password must be at least 8 characters.');
                                } else if (
                                    errorData.violations.some(
                                        (violation: any) => violation.key === 'password.required'
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
                        setErrorMessage('Failed to reset password. Please try again.');
                    }
                })
                .catch(() => {
                    setErrorMessage('Failed to reset password. Please try again.');
                });
        }
    };

    return (
        <div className="form-container">
            <h3>Reset Password</h3>
            {successMessage && <p className="message success-message">{successMessage}</p>}
            {warningMessage && <p className="message warning-message">{warningMessage}</p>}
            {errorMessage && <p className="message error-message">{errorMessage}</p>}
            <form onSubmit={handleSubmit}>
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
                <button type="submit">Reset Password</button>
            </form>
        </div>
    );
};
