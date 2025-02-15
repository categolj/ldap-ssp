import {useState} from 'react';
import {FormErrors, PasswordFormData} from '../types';

export const usePasswordValidation = (initialData: Partial<PasswordFormData>) => {
    const [formData, setFormData] = useState<Partial<PasswordFormData>>(initialData);
    const [errors, setErrors] = useState<FormErrors>({});

    const validateForm = (): boolean => {
        const newErrors: FormErrors = {};

        // Validate required fields
        Object.keys(formData).forEach((key) => {
            if (!formData[key as keyof PasswordFormData]) {
                newErrors[key] = `${key.charAt(0).toUpperCase() + key.slice(1)} is required`;
            }
        });

        // Validate password match
        if (formData.newPassword && formData.confirmPassword &&
            formData.newPassword !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Passwords must match';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData((prev) => ({
            ...prev,
            [e.target.id]: e.target.value,
        }));
    };

    return {formData, errors, handleChange, validateForm, setFormData};
};