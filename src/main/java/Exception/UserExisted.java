package Exception;

public class UserExisted extends RuntimeException {
    public UserExisted()
    {
        super("Tên người dùng đã tồn tại!");
    }
}
