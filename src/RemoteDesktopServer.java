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
    private String serverPassword; // Thêm biến lưu trữ mật khẩu
    
    protected RemoteDesktopServer(String password) throws RemoteException {
        super();
        this.serverPassword = password; // Gán mật khẩu cho server
        try {
            robot = new Robot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Phương thức để xác thực mật khẩu
    public boolean authenticate(String password) {
        return this.serverPassword.equals(password);
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
        robot.mousePress(button);
        robot.mouseRelease(button);
    }

    @Override
    public void typeKey(int keyCode) throws RemoteException {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    @Override
    public void scrollMouse(int scrollAmount) throws RemoteException {
        robot.mouseWheel(scrollAmount);
    }

    // Phương thức khởi động server
    public static void startServer(int port, String password) {
        try {
            RemoteDesktopServer server = new RemoteDesktopServer(password);
            Registry registry = LocateRegistry.createRegistry(port);
            registry.rebind("RemoteDesktop", server);
            System.out.println("Server started on port " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
