import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteDesktopInterface extends Remote {
    byte[] captureScreen() throws RemoteException;
    void mouseMove(int x, int y) throws RemoteException;
}