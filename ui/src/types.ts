export interface FormErrors {
    [key: string]: string;
}

export interface PasswordFormData {
    userId?: string;
    oldPassword?: string;
    newPassword: string;
    confirmPassword: string;
    email?: string;
}