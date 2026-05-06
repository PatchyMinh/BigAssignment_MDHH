package Exception;

public class PasswordStrengthCheck extends RuntimeException {
    public PasswordStrengthCheck()
    {
        super("Mật khẩu không đủ mạnh!");
    }
}
