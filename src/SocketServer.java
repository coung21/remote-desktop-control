import java.io.*;
import java.net.*;

public class SocketServer {
    public SocketServer(int serverPort) {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Server is running on port " + serverPort);

            Socket clientSocket = serverSocket.accept(); // Chấp nhận kết nối từ client

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
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

            clientSocket.close();
            serverSocket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Luồng đọc tin nhắn từ client
    static class ReadHandler implements Runnable {
        private BufferedReader in;

        public ReadHandler(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    System.out.println("Client: " + clientMessage);
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

    // Luồng ghi tin nhắn từ server
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
                String serverMessage;
                while (true) {
                    System.out.print("Server: ");
                    serverMessage = console.readLine();
                    out.println(serverMessage);

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
}
