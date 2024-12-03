import java.awt.*;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class App extends JFrame {

    private JTextField ipField;
    private JTextField portField;
    private JPasswordField passwordField; // Thêm ô nhập password cho client
    private JTextField serverPortField;
    private JPasswordField serverPasswordField; // Thêm ô nhập password cho server
    private int serverPort;
    private ServerSocket serverSocket;
    private Socket socketClient;
    private JFrame serverChatFrame;
    private JFrame clientChatFrame;

    public App() {
        setTitle("Remote Desktop Control App");
        setSize(800, 450);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Tạo một JTabbedPane để chuyển đổi giữa client và server
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                // Không thay đổi layout chính của tabbedPane
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
                    boolean isSelected) {
                super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
                if (isSelected) {
                    if (tabIndex == 0) {
                        g.setColor(new Color(76, 175, 80)); // Màu nền khi tab "Server" được chọn
                    } else {
                        g.setColor(new Color(0, 123, 255)); // Màu nền khi tab "Client" được chọn
                    }
                } else {
                    g.setColor(new Color(240, 240, 240)); // Màu nền khi tab không được chọn
                }
                g.fillRoundRect(x, y, w, h, 10, 10); // Viền bo góc cho tab
            }

            

        });

        // Giao diện server
        JPanel serverPanel = createServerPanel();
        tabbedPane.addTab("Server", serverPanel);

        // Giao diện client
        JPanel clientPanel = createClientPanel();
        tabbedPane.addTab("Client", clientPanel);

        // tabbedPane.setForegroundAt(0, Color.BLUE); // Màu chữ của tab "Server"
        // tabbedPane.setForegroundAt(1, Color.RED); // Màu chữ của tab "Client"

        add(tabbedPane);


        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        System.out.println("serverSocket closed");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private JPanel createServerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Thêm khoảng cách xung quanh
        panel.setBackground(new Color(247, 249, 252)); // Nền nhạt
    
        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        // Tiêu đề
        JLabel titleLabel = new JLabel("Server Configuration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(76, 175, 80)); // Màu xanh lá
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
    
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Khoảng cách sau tiêu đề
    
        // IP Address
        JLabel ipLabel = new JLabel("Your IP: " + localIp);
        ipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(ipLabel);
    
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách
    
        // Server Port
        JLabel portLabel = new JLabel("Server Port:");
        portLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(portLabel);
    
        serverPortField = new JTextField("5000"); // Giá trị mặc định là 5000
        serverPortField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serverPortField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(serverPortField);
    
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách
    
        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordLabel);
    
        serverPasswordField = new JPasswordField();
        serverPasswordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serverPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(serverPasswordField);
    
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Khoảng cách
    
        // Nút Start Server
        JButton startServerButton = new JButton("Start Server");
        startServerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        startServerButton.setBackground(new Color(76, 175, 80)); // Màu xanh lá
        startServerButton.setForeground(Color.WHITE); // Chữ trắng
        startServerButton.setFocusPainted(false);
        startServerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        startServerButton.addActionListener(e -> {
            String password = new String(serverPasswordField.getPassword());
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                serverPort = Integer.parseInt(serverPortField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            new Thread(() -> {
                try {
                    RemoteDesktopServer.startServer(serverPort, password);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
    
            new Thread(() -> {
                try {
                    // Khởi động SocketServer và mở Chat Frame
                    serverSocket = new ServerSocket(serverPort + 1); // Cổng +1 để tránh trùng cổng với remote desktop
                    while (true) {
                        Socket socket = serverSocket.accept();
                        SwingUtilities.invokeLater(() -> {
                            try {
                                JFrame newServerChatFrame = new ChatFrame("Server Chat", socket);
                                newServerChatFrame.setVisible(true);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        });
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        });
    
        panel.add(startServerButton);
    
        return panel;
    }
    
    private JPanel createClientPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Thêm khoảng cách xung quanh
        panel.setBackground(new Color(247, 249, 252)); // Nền nhạt
    
        // Tiêu đề
        JLabel titleLabel = new JLabel("Client Connection Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(33, 150, 243)); // Màu xanh dương
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
    
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Khoảng cách sau tiêu đề
    
        // Partner's IP
        JLabel ipLabel = new JLabel("Partner's IP:");
        ipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(ipLabel);
    
        ipField = new JTextField();
        ipField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ipField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(ipField);
    
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách
    
        // Partner's Port
        JLabel portLabel = new JLabel("Partner's Port:");
        portLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(portLabel);
    
        portField = new JTextField();
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(portField);
    
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách
    
        // Server Password
        JLabel passwordLabel = new JLabel("Server Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordLabel);
    
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(passwordField);
    
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Khoảng cách
    
        // Nút Connect
        JButton connectButton = new JButton("Connect");
        connectButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        connectButton.setBackground(new Color(33, 150, 243)); // Màu xanh dương
        connectButton.setForeground(Color.WHITE); // Chữ trắng
        connectButton.setFocusPainted(false);
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        connectButton.addActionListener(e -> connectToServer());
        panel.add(connectButton);
    
        return panel;
    }
    

    // Phương thức kết nối với server của partner
    private void connectToServer() {
        String partnerIp = ipField.getText();
        int partnerPort;
        String password = new String(passwordField.getPassword()); // Lấy mật khẩu từ client

        try {
            partnerPort = Integer.parseInt(portField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Đảm bảo kết nối client được thực hiện trong một luồng riêng
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Tạo và khởi tạo RemoteDesktopClient
                    RemoteDesktopClient client = new RemoteDesktopClient(partnerIp, partnerPort, password);
                    JFrame screenFrame = client.getScreenFrame();
                    screenFrame.setVisible(true);
                    Insets insets = screenFrame.getInsets(); // Lấy thông tin về viền và thanh tiêu đề
                    screenFrame.setSize(
                        screenFrame.getWidth(),
                        screenFrame.getHeight() + insets.top
                    );

                    // Đóng kết nối khi đóng
                    screenFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                            client.disconnect();
                            System.out.println("Disconnected from partner's server");

                            // Gửi thông báo tới Server
                            try {
                                if (socketClient != null && !socketClient.isClosed()) {
                                    PrintWriter out = new PrintWriter(socketClient.getOutputStream(), true);
                                    out.println("CLOSE_CONNECTION");
                                    out.flush();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Đóng socketClient
                            try {
                                if (socketClient != null && !socketClient.isClosed()) {
                                    socketClient.close();
                                    System.out.println("socketClient closed");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Đóng clientChatFrame
                            if (clientChatFrame != null) {
                                clientChatFrame.dispose();
                                System.out.println("clientChatFrame disposed");
                            }
                        }
                    });

                    // Khởi động SocketClient để chat (mở Frame Chat)
                    socketClient = new Socket(partnerIp, partnerPort + 1); // Cổng +1 để tránh trùng cổng với server
                    SwingUtilities.invokeLater(() -> {
                        try {
                            clientChatFrame = new ChatFrame("Client Chat", socketClient);
                            clientChatFrame.setVisible(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(App.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                System.out.println("Connection process completed.");
            }
        };

        worker.execute();
    }

    public static void main(String[] args) throws Exception {
        EventQueue.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}
