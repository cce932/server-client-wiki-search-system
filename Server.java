
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Server extends Thread {
    List<Connection> connections;
    ServerSocket serverSocket;
    int port;
    int maxiumConn;

    public static void main(String args[]) {
        Server server = new Server(20000, 100);
        server.start();
    }

    public Server(int p, int m) {
        port = p;
        maxiumConn = m;
        connections = Collections.synchronizedList(new LinkedList());
        try {
            serverSocket = new ServerSocket(port, maxiumConn);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Listening connections...");
                Socket connSocket = serverSocket.accept();
                if (connections.size() < maxiumConn) {
                    Connection connection = new Connection(connSocket); // Each client connet
                    connections.add(connection);
                    connection.start();
                }
            } catch (IOException x) {
                x.printStackTrace();
            }
        }

    }

    void connectionRemove(Connection connection) {
        connections.remove(connection);
    }

    class Connection extends Thread {
        Socket socket;
        ObjectInputStream input;
        ObjectOutputStream output;
        String searchResult = "";

        public Connection(Socket s) {
            socket = s;
            try {
                input = new ObjectInputStream(socket.getInputStream());
                output = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("connected " + s.getPort() + " successfully");
            } catch (IOException x) {
                x.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String clientInput = (String) input.readObject(); // client pass in
                    System.out.println("\n--- Input from client " + socket.getPort() + ": " + clientInput);

                    output.writeObject(searchWiki(clientInput));
                    output.flush();
                    searchResult = "";
                }
            } catch (ClassNotFoundException x) {
                x.printStackTrace();
                close();
            } catch (IOException x) {
                System.out.println(x);
                close();
            }
        }

        private String searchWiki(String input) throws IOException {

            String[] para = { "python", "-u", "./searching.py", input };
            ProcessBuilder processBuilder = new ProcessBuilder().command(para);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            List<String> resultReader = readProcessOutput(process.getInputStream());
            resultReader.forEach(result -> {
                searchResult += result;
                System.out.println(result);
            });

            // kill the process
            process.destroy();
            return searchResult;
        }

        private List<String> readProcessOutput(InputStream inputStream) throws IOException {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                return bufferedReader.lines().collect(Collectors.toList());
            }
        }

        public void close() {
            try {
                input.close();
                output.close();
                socket.close();
                connectionRemove(this);
                System.out.println("connection " + socket.getPort() + " is closed");
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }
}
