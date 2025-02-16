import {ErrorResponse, Violation} from '../types';

export interface ApiMessage {
    message: string;
    level: 'error' | 'warning';
}

interface ExtraHandlers {
    status401?: ApiMessage;
    status410?: ApiMessage;
}

export function processApiResponse(
    response: Response,
    defaultError: string,
    handlers: ExtraHandlers = {}
): Promise<void> {
    if (response.ok) {
        return Promise.resolve();
    }
    if (handlers.status401 && response.status === 401) {
        return Promise.reject(handlers.status401);
    }
    if (handlers.status410 && response.status === 410) {
        return Promise.reject(handlers.status410);
    }
    if (response.status === 400) {
        return response
            .json()
            .then((errorData: ErrorResponse) => {
                if (errorData.violations) {
                    if (errorData.violations.some(
                        (v: Violation) => v.key === 'container.greaterThanOrEqual')) {
                        return Promise.reject({
                            message: 'Password must be at least 8 characters.',
                            level: 'error',
                        } as ApiMessage);
                    }
                    if (errorData.violations.some(
                        (v: Violation) => v.key === 'password.required')) {
                        return Promise.reject({
                            message: 'Password must include alphanumeric characters.',
                            level: 'error',
                        } as ApiMessage);
                    }
                }
                return Promise.reject({
                    message: 'Invalid input. Please check your data.',
                    level: 'error',
                } as ApiMessage);
            });
    }
    return Promise.reject({message: defaultError, level: 'error'} as ApiMessage);
}
