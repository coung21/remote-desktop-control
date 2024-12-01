import java.io.*;
import java.net.*;

public class SocketClient {
    public SocketClient(String serverIp, int serverPort) {
        try {
            Socket socket = new Socket(serverIp, serverPort);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

            // Tạo các luồng đọc và ghi
            Thread readThread = new Thread(new ReadHandler(in));
            Thread writeThread = new Thread(new WriteHandler(out, console));

            // Bắt đầu các luồng
            readThread.start();
            writeThread.start();

            // Đợi luồng kết thúc
            readThread.join();
            writeThread.join();

            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Luồng đọc tin nhắn từ server
    static class ReadHandler implements Runnable {
        private BufferedReader in;

        public ReadHandler(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println("Server: " + serverMessage);
                    if (serverMessage.equalsIgnoreCase("exit")) {
                        System.out.println("Server has disconnected");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Luồng ghi tin nhắn từ client
    static class WriteHandler implements Runnable {
        private PrintWriter out;
        private BufferedReader console;

        public WriteHandler(PrintWriter out, BufferedReader console) {
            this.out = out;
            this.console = console;
        }

        @Override
        public void run() {
            try {
                String clientMessage;
                while (true) {
                    System.out.print("Client: ");
                    clientMessage = console.readLine();
                    out.println(clientMessage);

                    if (clientMessage.equalsIgnoreCase("exit")) {
                        System.out.println("Client has disconnected");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
