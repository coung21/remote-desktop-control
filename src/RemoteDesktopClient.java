
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


public class RemoteDesktopClient {
    private RemoteDesktopInterface remoteDesktop;
    private JFrame screenFrame;
    private JLabel screenLabel;
    
    
    public RemoteDesktopClient(String serverIp, int serverPort) {
        try {
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            remoteDesktop = (RemoteDesktopInterface) registry.lookup("RemoteDesktop");
            System.out.println("Connected to server at " + serverIp + ":" + serverPort);

            // Tạo frame hiển thị màn hình từ server
            screenFrame = new JFrame("Remote Desktop - Screen View");
            screenFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
            screenFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            screenLabel = new JLabel();
            screenFrame.add(screenLabel, BorderLayout.CENTER);

            
            // Cập nhật màn hình
            // Timer timer = new Timer(100, e -> updateScreen());
            // timer.start();
            // Chạy cập nhật màn hình trên một luồng riêng
            new Thread(() -> {
                while (true) {
                    updateScreen();
                    try {
                        Thread.sleep(100); // Cập nhật mỗi 100ms (10 lần mỗi giây)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            
            screenLabel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    try {
                        if (remoteDesktop != null) {
                            remoteDesktop.mouseMove(e.getXOnScreen(), e.getYOnScreen());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            
            screenLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        if (remoteDesktop != null) {
                            int button = e.getButton() == MouseEvent.BUTTON1 ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON3_DOWN_MASK;
                            remoteDesktop.clickMouse(button);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // Sự kiện bàn phím
            screenFrame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    try {
                        if (remoteDesktop != null) {
                            remoteDesktop.typeKey(e.getKeyCode());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });


            // Sự kiện lăn chuột
            screenLabel.addMouseWheelListener((MouseWheelListener) e -> {
                try {
                    if (remoteDesktop != null) {
                        remoteDesktop.scrollMouse(e.getWheelRotation());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public JFrame getScreenFrame() {
        return screenFrame;
    }

    public void disconnect() {
        screenFrame.dispose();
        screenFrame = null;
        remoteDesktop = null;
    }

    public void updateScreen() {
        try {
            if (remoteDesktop != null) {
                 byte[] screenData = remoteDesktop.captureScreen();
                 if (screenData != null) {
                     BufferedImage image = ImageIO.read(new ByteArrayInputStream(screenData));
                     if (image != null) {
                         SwingUtilities.invokeLater(() -> {
                            screenLabel.setIcon(new ImageIcon(image));
                        });
                     } else {
                         System.out.println("Failed to decode screen data to image.");
                     }
                 } else {
                     System.out.println("Received null screen data.");
                 }
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
     }
}
