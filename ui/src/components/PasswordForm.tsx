import React, {useState} from 'react';
import {useParams} from 'react-router-dom';
import {FormInput} from './FormInput';
import {usePasswordValidation} from '../hooks/usePasswordValidation';
import {ApiMessage, processApiResponse} from '../utils/apiHelpers';

export const PasswordForm: React.FC = () => {
    // Retrieve resetId from the URL
    const {resetId} = useParams<{ resetId: string }>();

    // Initialize form with newPassword and confirmPassword fields.
    const {formData, errors, handleChange, validateForm, setFormData} = usePasswordValidation({
        newPassword: '',
        confirmPassword: '',
    });

    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [warningMessage, setWarningMessage] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        // Reset previous messages
        setSuccessMessage(null);
        setErrorMessage(null);
        setWarningMessage(null);

        if (validateForm()) {
            setIsSubmitting(true);
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
                .then((response) =>
                    processApiResponse(
                        response,
                        'Failed to reset password. Please try again.',
                        {
                            status410: {
                                message: 'Reset link expired. Please request a new reset link.',
                                level: 'warning',
                            },
                        }
                    )
                )
                .then(() => {
                    setSuccessMessage('Password reset successfully!');
                    setFormData({
                        newPassword: '',
                        confirmPassword: '',
                    });
                })
                .catch((apiMessage: ApiMessage) => {
                    if (apiMessage.level === 'warning') {
                        setWarningMessage(apiMessage.message);
                    } else {
                        setErrorMessage(apiMessage.message);
                    }
                })
                .finally(() => {
                    setIsSubmitting(false);
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
                <fieldset disabled={isSubmitting}>
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
                    <button type="submit" disabled={isSubmitting}>
                        Reset Password
                    </button>
                </fieldset>
            </form>
        </div>
    );
};