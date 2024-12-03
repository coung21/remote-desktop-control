import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

public class ChatFrame extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private PrintWriter out;

    public ChatFrame(String title, Socket socket) throws IOException {
        setTitle(title);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Khởi tạo các thành phần giao diện
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        
        inputField = new JTextField();
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Thiết lập Layout
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        // Kết nối với socket để gửi và nhận tin nhắn
        out = new PrintWriter(socket.getOutputStream(), true);
        new Thread(new ReadMessages(socket)).start();
    }

    // Đọc tin nhắn từ socket
    private class ReadMessages implements Runnable {
        private BufferedReader in;
        private Socket socket;
    
        public ReadMessages(Socket socket) throws IOException {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.socket = socket;
        }
    
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("CLOSE_CONNECTION")) {
                        chatArea.append("Partner has left the chat.\n");
                        // Đóng socket
                        socket.close();
    
                        // Đóng cửa sổ chat
                        SwingUtilities.invokeLater(() -> {
                            ChatFrame.this.dispose();
                        });
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
