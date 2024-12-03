import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ChatFrame extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private PrintWriter out;
    private boolean isCollapsed = false; // Trạng thái collapse
    private JButton collapseButton; // Nút để collapse/expand

    public ChatFrame(String title, Socket socket) throws IOException {
        setTitle(title);
        setSize(250, 400);
        setUndecorated(true);
        // Thiết lập không cho đóng cửa sổ bằng nút "X"
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Khởi tạo các thành phần giao diện
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());

        // Nút collapse
        collapseButton = new JButton("→");
        collapseButton.setFocusable(false);
        collapseButton.addActionListener(e -> toggleCollapse());

        // Thiết lập Layout
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.add(collapseButton);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        // Đặt cửa sổ ở bên phải màn hình
        positionOnRight();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JOptionPane.showMessageDialog(ChatFrame.this,
                        "This window cannot be closed manually.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Kết nối với socket để gửi và nhận tin nhắn
        out = new PrintWriter(socket.getOutputStream(), true);
        new Thread(new ReadMessages(socket)).start();
    }

    // Đặt cửa sổ ở bên phải màn hình
    private void positionOnRight() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - getWidth(); // Đặt ở sát bên phải
        int y = (screenSize.height - getHeight()) / 2; // Căn giữa theo chiều dọc
        setLocation(x, y);
    }

    // Chuyển đổi trạng thái collapse
    private void toggleCollapse() {
        if (isCollapsed) {
            // Mở rộng khung chat
            setSize(250, 400);
            collapseButton.setText("→");
            isCollapsed = false;
        } else {
            // Thu nhỏ khung chat
            setSize(30, 400); // Chỉ giữ lại một thanh nhỏ
            collapseButton.setText("←");
            isCollapsed = true;
        }

        // Cập nhật vị trí để cửa sổ không di chuyển
        positionOnRight();
    }

    // Đọc tin nhắn từ socket
    private class ReadMessages implements Runnable {
        private BufferedReader in;

        public ReadMessages(Socket socket) throws IOException {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("CLOSE_CONNECTION")) {
                        chatArea.append("Partner has left the chat.\n");
                        SwingUtilities.invokeLater(() -> ChatFrame.this.dispose());
                        break;
                    } else {
                        chatArea.append("Partner: " + message + "\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Gửi tin nhắn khi nhấn Enter
    private void sendMessage() {
        String message = inputField.getText();
        if (!message.trim().isEmpty()) {
            chatArea.append("You: " + message + "\n");
            out.println(message);
            inputField.setText("");
        }
    }
}
