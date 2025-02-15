import {ChangePasswordForm} from './ChangePasswordForm';
import {RequestResetForm} from './RequestResetForm';

export const Home: React.FC = () => (
    <div className="form-container">
        <ChangePasswordForm/>
        <RequestResetForm/>
    </div>
);