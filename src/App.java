import java.awt.*;
import java.net.InetAddress;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public class App extends JFrame {

    private JTextField ipField;
    private JTextField portField;
    private JPasswordField passwordField;  // Thêm ô nhập password cho client
    private JTextField serverPortField;
    private JPasswordField serverPasswordField;  // Thêm ô nhập password cho server
    private int serverPort;

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
                protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                    super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
                    if (isSelected) {
                        g.setColor(new Color(0, 123, 255)); // Màu nền khi tab được chọn
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
        // tabbedPane.setForegroundAt(1, Color.RED);  // Màu chữ của tab "Client"

        add(tabbedPane);
    }

    private JPanel createServerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Hiển thị ip và port của máy local
        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel ipLabel = new JLabel("Your IP: " + localIp);
        JLabel portLabel = new JLabel("Server Port: ");
        JLabel passwordLabel = new JLabel("Password: ");
        
        // Trường nhập port và password cho server
        serverPortField = new JTextField("5000");  // Giá trị mặc định là 5000
        serverPasswordField = new JPasswordField();

        panel.add(ipLabel);
        panel.add(portLabel);
        panel.add(serverPortField);
        panel.add(passwordLabel);
        panel.add(serverPasswordField);

        // Khởi động server
        JButton startServerButton = new JButton("Start Server");
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
                RemoteDesktopServer.startServer(serverPort, password); // Gửi mật khẩu khi khởi động server
            }).start();
        });
        panel.add(startServerButton);

        return panel;
    }

    private JPanel createClientPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Trường nhập IP và Port của partner
        ipField = new JTextField();
        portField = new JTextField();
        passwordField = new JPasswordField();  // Thêm trường nhập password cho client
        
        panel.add(new JLabel("Partner's IP: "));
        panel.add(ipField);
        panel.add(new JLabel("Partner's Port: "));
        panel.add(portField);
        panel.add(new JLabel("Server Password: "));  // Thêm label cho mật khẩu
        panel.add(passwordField);
        
        // Nút Connect
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToServer());
        panel.add(connectButton);
        
        return panel;
    }

    // Phương thức kết nối với server của partner
    private void connectToServer() {
        String partnerIp = ipField.getText();
        int partnerPort;
        String password = new String(passwordField.getPassword());  // Lấy mật khẩu từ client
        
        try {
            partnerPort = Integer.parseInt(portField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Gửi mật khẩu cùng với kết nối đến server
        EventQueue.invokeLater(() -> {
            RemoteDesktopClient client = new RemoteDesktopClient(partnerIp, partnerPort, password);  // Thêm mật khẩu vào client
            JFrame screenFrame = client.getScreenFrame();
            screenFrame.setVisible(true);

            // Đóng kết nối khi đóng
            screenFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    client.disconnect();
                    System.out.println("Disconnected from partner's server");
                }
            });
        });
    }

    public static void main(String[] args) throws Exception {
        EventQueue.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}
