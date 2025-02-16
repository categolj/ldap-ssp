import React, {useState} from 'react';
import {FormInput} from './FormInput';
import {usePasswordValidation} from '../hooks/usePasswordValidation';
import {ApiMessage, processApiResponse} from '../utils/apiHelpers';

export const RequestResetForm: React.FC = () => {
    const {formData, errors, handleChange, validateForm, setFormData} = usePasswordValidation({
        email: '',
    });
    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setSuccessMessage(null);
        setErrorMessage(null);
        if (validateForm()) {
            setIsSubmitting(true);
            fetch('/api/reset_password/send_link', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(formData),
            }).then((response) =>
                processApiResponse(
                    response,
                    'Failed to send reset link (Maybe the given email is not found?). Please try again.'
                )
            ).then(() => {
                setSuccessMessage('Reset link sent successfully!');
                setFormData({
                    email: ''
                });
            }).catch((apiMessage: ApiMessage) => {
                setErrorMessage(apiMessage.message);
            }).finally(() => {
                setIsSubmitting(false);
            });
        }
    };

    return (
        <div>
            <h3>Reset Password Link</h3>
            {successMessage && <p className="message success-message">{successMessage}</p>}
            {errorMessage && <p className="message error-message">{errorMessage}</p>}
            <form onSubmit={handleSubmit}>
                <fieldset disabled={isSubmitting}>
                    <FormInput
                        id="email"
                        label="Email Address"
                        type="email"
                        value={formData.email || ''}
                        onChange={handleChange}
                        error={errors.email}
                    />
                    <button type="submit" disabled={isSubmitting}>Send Reset Link</button>
                </fieldset>
            </form>
        </div>
    );
};