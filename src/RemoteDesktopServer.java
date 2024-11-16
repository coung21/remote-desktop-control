
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.imageio.ImageIO;


public class RemoteDesktopServer extends UnicastRemoteObject implements RemoteDesktopInterface {
    private Robot robot;
    protected  RemoteDesktopServer() throws RemoteException {
        super();
        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public byte[] captureScreen() throws RemoteException {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(screenCapture, "JPEG", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void mouseMove(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }

    @Override
    public void clickMouse(int button) throws RemoteException {
        // Nhấn và nhả nút chuột
        robot.mousePress(button);
        robot.mouseRelease(button);
    }

    @Override
    public void typeKey(int keyCode) throws RemoteException {
        // Nhấn và nhả phím với mã phím (keyCode) cụ thể
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    @Override
    public void scrollMouse(int scrollAmount) throws RemoteException {
        // Lăn chuột: scrollAmount là số bước lăn, có thể âm hoặc dương
        robot.mouseWheel(scrollAmount);
    }

    public static void startServer(int port){
        try {
            RemoteDesktopServer server = new RemoteDesktopServer();
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("RemoteDesktop", server);
            System.out.println("Server started on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
