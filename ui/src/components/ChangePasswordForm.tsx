import React, {useState} from 'react';
import {FormInput} from './FormInput';
import {usePasswordValidation} from '../hooks/usePasswordValidation';
import {ApiMessage, processApiResponse} from '../utils/apiHelpers';
import {FormProps} from "../types.ts";

export const ChangePasswordForm: React.FC<FormProps> = ({csrfToken}) => {
    const {formData, errors, handleChange, validateForm, setFormData} = usePasswordValidation({
        userId: '',
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
    });

    const [successMessage, setSuccessMessage] = useState<string | null>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [warningMessage, setWarningMessage] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setSuccessMessage(null);
        setErrorMessage(null);
        setWarningMessage(null);

        if (validateForm()) {
            setIsSubmitting(true);
            // eslint-disable-next-line @typescript-eslint/no-unused-vars
            const {confirmPassword: _unused, ...payload} = formData;
            // Create the Basic auth header using userId and oldPassword
            const authHeader = `Basic ${btoa(`${formData.userId}:${formData.oldPassword}`)}`;

            fetch('/api/change_password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': authHeader,
                    'X-CSRF-TOKEN': csrfToken,
                },
                body: JSON.stringify(payload),
            })
                .then((response) =>
                    processApiResponse(
                        response,
                        'Failed to change password. Please try again.',
                        {
                            status401: {
                                message: 'Authentication failed. Please check your credentials.',
                                level: 'error',
                            },
                        }
                    )
                )
                .then(() => {
                    setSuccessMessage('Password changed successfully!');
                    // Reset the form.
                    setFormData({
                        userId: '',
                        oldPassword: '',
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
        <div>
            <h3>Change Password</h3>
            {successMessage && <p className="message success-message">{successMessage}</p>}
            {warningMessage && <p className="message warning-message">{warningMessage}</p>}
            {errorMessage && <p className="message error-message">{errorMessage}</p>}
            <form onSubmit={handleSubmit}>
                <fieldset disabled={isSubmitting}>
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
                    <button type="submit" disabled={isSubmitting}>
                        Change Password
                    </button>
                </fieldset>
            </form>
        </div>
    );
};