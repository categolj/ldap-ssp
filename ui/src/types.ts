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

export interface Violation {
    key: string;
    args: unknown[];
    defaultMessage: string;
}

export interface ErrorResponse {
    violations?: Violation[];
}

export interface FormProps {
    csrfToken: string
}