package serverClasses;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
    private static ArrayList<Connection> connections = new ArrayList<>();
    public static ArrayList<String> usernames = new ArrayList<>();
    private static ArrayList<Lobby> lobbies = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("server");
        ServerSocket serverSocket = new ServerSocket(1234);
        createTestLobbies();
        usernames.add("test");
        while (true) {
            Socket socket = serverSocket.accept();
            Connection connection = new Connection(socket);
            System.out.println("found connection!");
            connections.add(connection);
        }
    }

    private static void createTestLobbies() {
        lobbies.add(new Lobby("test lobby 1"));
        lobbies.add(new Lobby("test lobby 2"));
        lobbies.add(new Lobby("test lobby 3"));
    }

    public static void disconnect(Connection connection) {
        System.out.println("Client " + connection.getUsername() + " disconnected");
        connections.remove(connection);
    }

    public static void writeToAllExcept(String msg, String nickName) {
        for (Connection connection : connections) {
            if (!nickName.equals(connection.getUsername())) {
                connection.writeString(msg);
            }
        }
    }

    public static void sendLobbies(Connection connection) {
        connection.writeObject(lobbies);
    }

    public static Lobby getLobbyToConnectTo(String lobbyName) {
        return lobbies.stream().filter(lobby -> lobby.getLobbyName().equals(lobbyName)).findFirst().orElse(null);
    }

    public static void updatePlayerProgress(String username, int wpm) {
        Connection connection = connections.stream().filter(conn -> conn.getUsername().equals(username)).findFirst().orElse(null);
        if (connection != null) {
            Lobby lobby = lobbies.stream().filter(l -> l.getPlayers().contains(connection)).findFirst().orElse(null);
            if (lobby != null) {
                lobby.updatePlayerProgress(connection, wpm);
            }
        }
    }
}

class Connection implements Serializable {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private String username = null;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.inputStream = new ObjectInputStream(socket.getInputStream());
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
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
                if (line == null) break;
                System.out.println("Server: got " + line);

                if (username == null) {
                    handleNewUser(line);
                } else {
                    handleClientRequest(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server.disconnect(this);
    }

    private void handleNewUser(String line) throws IOException {
        if (line.isEmpty()) {
            writeString("Je moet wel een username invullen");
        } else if (!Server.usernames.contains(line)) {
            username = line;
            Server.usernames.add(username);
            writeString("Welkom!");
        } else {
            writeString("Deze username bestaat al");
        }
    }

    private void handleClientRequest(String line) throws IOException {
        if (line.equals("send lobbies")) {
            Server.sendLobbies(this);
        } else if (line.startsWith("connectTo")) {
            String[] info = line.split(":");
            String lobbyName = info[1];
            Lobby lobby = Server.getLobbyToConnectTo(lobbyName);
            if (lobby != null) {
                if (lobby.getAvailableSpots() > 0) {
                    lobby.addPlayer(this);
                } else {
                    writeString("full");
                }
            }
        } else if (line.startsWith("wpm:")) {
            int wpm = Integer.parseInt(line.split(":")[1]);
            Server.updatePlayerProgress(username, wpm);
        } else if (line.startsWith("finished:")) {
            int wpm = Integer.parseInt(line.split(":")[1]);
            Server.updatePlayerProgress(username, wpm);
        }
    }

    public String getUsername() {
        return this.username;
    }

    public void writeString(String message) {
        try {
            writer.write(message + "\n");
            writer.flush();
            System.out.println("Server: sent " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeObject(ArrayList<Lobby> lobbies) {
        try {
            outputStream.writeObject(lobbies);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
