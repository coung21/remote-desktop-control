import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteDesktopInterface extends Remote {
    // Các phương thức điều khiển màn hình và chuột
    byte[] captureScreen() throws RemoteException;
    void mouseMove(int x, int y) throws RemoteException;
    void clickMouse(int button) throws RemoteException;
    void typeKey(int keyCode) throws RemoteException;
    void scrollMouse(int scrollAmount) throws RemoteException;

    // Phương thức xác thực mật khẩu
    boolean authenticate(String password) throws RemoteException;  // Thêm phương thức xác thực mật khẩu
}
