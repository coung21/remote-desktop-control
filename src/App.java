import java.awt.EventQueue;
import java.awt.GridLayout;
import java.net.InetAddress;
import java.util.Scanner;
import javax.swing.*;

public class App extends JFrame {

    private JTextField ipField;
    private JTextField portField;
    private int serverPort;

    public App(){
        setTitle("Remtoe Desktop Control App");
        setSize(800, 450);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(6,1));

        // hiển thị ip và port của máy local

        String localIp = "localhost";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter port for server: ");
            serverPort = scanner.nextInt();
        }
        JLabel ipLabel = new JLabel("Your IP: " + localIp);
        JLabel portLabel = new JLabel("Your Port: " + serverPort);
        add(ipLabel);
        add(portLabel);
    
        // trường nhâp ip và port của partner

        ipField = new JTextField();
        portField = new JTextField();

        add(new JLabel("Partner's IP: "));
        add(ipField);
        add(new JLabel("Partner's Port: "));
        add(portField);

        // nút connect
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connectToServer());
        add(connectButton);


        // Khởi động server
        new Thread(() -> {
            RemoteDesktopServer.startServer(serverPort);
        }).start();
    }

    // phương thức kết nối với server của partner
    private void connectToServer() {
        String partnerIp = ipField.getText();
        int partnerPort;
        try {
            partnerPort = Integer.parseInt(portField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // Khởi động client và hiển thị màn hình
        EventQueue.invokeLater(() -> {
            RemoteDesktopClient client = new RemoteDesktopClient(partnerIp, partnerPort);
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