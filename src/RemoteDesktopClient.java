import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class RemoteDesktopClient {
    private RemoteDesktopInterface remoteDesktop;
    private JFrame screenFrame;
    private JLabel screenLabel;
    private JTextArea chatArea;  // Khu vực hiển thị chat
    private JTextField chatInput; // Khu vực nhập chat
    private String password;  // Trường lưu mật khẩu

    public RemoteDesktopClient(String serverIp, int serverPort, String password) {
        this.password = password;  // Lưu mật khẩu

        try {
            // Kết nối đến registry RMI với server
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            remoteDesktop = (RemoteDesktopInterface) registry.lookup("RemoteDesktop");

            // Kiểm tra mật khẩu
            if (!authenticate(password)) {
                throw new Exception("Authentication failed: Invalid password.");
            }

            System.out.println("Connected to server at " + serverIp + ":" + serverPort);

            // Tạo frame hiển thị màn hình từ server
            screenFrame = new JFrame("Remote Desktop - Screen View");
            screenFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
            screenFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            screenLabel = new JLabel();
            screenFrame.add(screenLabel, BorderLayout.CENTER);

            // Tạo khu vực chat
            JPanel chatPanel = new JPanel(new BorderLayout());
            chatArea = new JTextArea(10, 30);
            chatArea.setEditable(false);
            chatInput = new JTextField();
            JButton sendButton = new JButton("Send");

            // Thêm các phần chat vào giao diện
            chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
            chatPanel.add(chatInput, BorderLayout.SOUTH);
            chatPanel.add(sendButton, BorderLayout.EAST);
            screenFrame.add(chatPanel, BorderLayout.EAST);

            // Xử lý sự kiện gửi tin nhắn
            sendButton.addActionListener(e -> sendMessage());

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

            // Xử lý sự kiện chuột và bàn phím (như cũ)
            setupMouseAndKeyboardListeners();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Phương thức xác thực mật khẩu
    private boolean authenticate(String password) {
        try {
            // Kiểm tra mật khẩu với server
            return remoteDesktop != null && remoteDesktop.authenticate(password);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;  // Trả về false nếu có lỗi kết nối hoặc ngoại lệ
        }
    }

    // Gửi tin nhắn
    private void sendMessage() {
        String message = chatInput.getText();
        if (!message.isEmpty()) {
            try {
                remoteDesktop.sendMessage(message);
                chatArea.append("Me: " + message + "\n");
                chatInput.setText("");  // Xóa text field sau khi gửi
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // Nhận tin nhắn từ server
    private void updateChat() {
        try {
            String message = remoteDesktop.receiveMessage();
            if (message != null) {
                chatArea.append("Server: " + message + "\n");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // Cập nhật màn hình từ server
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
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Thiết lập các sự kiện chuột và bàn phím (như cũ)
    private void setupMouseAndKeyboardListeners() {
        // Các sự kiện chuột và bàn phím (dùng cho điều khiển máy tính từ xa)
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
        screenLabel.addMouseWheelListener(e -> {
            try {
                if (remoteDesktop != null) {
                    remoteDesktop.scrollMouse(e.getWheelRotation());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public JFrame getScreenFrame() {
        return screenFrame;
    }

    public void disconnect() {
        screenFrame.dispose();
        screenFrame = null;
        remoteDesktop = null;
    }
}
