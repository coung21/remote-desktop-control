import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.imageio.ImageIO;
import java.util.concurrent.LinkedBlockingQueue;

public class RemoteDesktopServer extends UnicastRemoteObject implements RemoteDesktopInterface {
    private Robot robot;
    private String serverPassword;
    private LinkedBlockingQueue<String> chatQueue; // Hàng đợi chat

    protected RemoteDesktopServer(String password) throws RemoteException {
        super();
        this.serverPassword = password;
        try {
            robot = new Robot();
            chatQueue = new LinkedBlockingQueue<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate(String password) {
        return this.serverPassword.equals(password);
    }

    @Override
    public byte[] captureScreen() throws RemoteException {
        try {
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenCapture = robot.createScreenCapture(screenRect);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(screenCapture, "PNG", outputStream);

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

    @Override
    public void sendMessage(String message) throws RemoteException {
        chatQueue.offer(message); // Thêm tin nhắn vào hàng đợi
    }

    @Override
    public String receiveMessage() throws RemoteException {
        return chatQueue.poll(); // Lấy tin nhắn từ hàng đợi (nếu có)
    }

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
