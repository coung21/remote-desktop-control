import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteDesktopInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    void mouseMove(int x, int y) throws RemoteException;
    void clickMouse(int button) throws RemoteException;
    void typeKey(int keyCode) throws RemoteException;
}