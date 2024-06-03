package serverClasses;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;


public class Server {
    private static ArrayList<Connection> connections = new ArrayList<>();
    private static Lobby lobby;

    public static void main(String[] args) throws IOException {
        System.out.println("server");
        lobby = new Lobby();
        ServerSocket serverSocket = new ServerSocket(1234);
        while (true) {
            Socket socket = serverSocket.accept();
            Connection connection = new Connection(socket);
            System.out.println("found connection!");
            connections.add(connection);
        }
    }

    public static void disconnect(Connection connection) {
        System.out.println("Client " + connection.getUsername() + " disconnected");
        connections.remove(connection);
        lobby.removePlayer(connection);
    }

    public static void broadcastProgress() {
        Map<String, Double> progressMap = lobby.getProgressMap();
        for (Connection connection : connections) {
            connection.writeProgress(progressMap);
        }
    }
}

class Connection {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private String username = null;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread thread = new Thread(this::receiveData);
        thread.start();
    }

    private void receiveData() {
        try {
            while (socket.isConnected()) {
                String line = reader.readLine();
                if (line == null)
                    break;
                System.out.println("Server: got " + line);

                if (username == null) {
                    if (line.equals(""))
                        writer.write("Je moet wel een username invullen\n");
                    else {
                        username = line;
                        writer.write("Je bent nu " + line + "\n");
                        Server.broadcastProgress();
                    }
                }
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Server.disconnect(this);
    }

    public String getUsername() {
        return this.username;
    }

    public void writeProgress(Map<String, Double> progressMap) {
        try {
            StringBuilder sb = new StringBuilder("PROGRESS_LIST");
            for (Map.Entry<String, Double> entry : progressMap.entrySet()) {
                sb.append(" ").append(entry.getKey()).append(":").append(entry.getValue());
            }
            sb.append("\n");
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}