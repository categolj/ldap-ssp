import {ChangePasswordForm} from './ChangePasswordForm';
import {RequestResetForm} from './RequestResetForm';
import {FormProps} from "../types.ts";

export const Home: React.FC<FormProps> = ({csrfToken}) => (
    <div className="form-container">
        <ChangePasswordForm csrfToken={csrfToken}/>
        <RequestResetForm csrfToken={csrfToken}/>
    </div>
);