package serverClasses;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
    private static ArrayList<Connection> connections = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("server");
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
    }

    public static void WriteToAllExcept(String msg, String nickName) {
        for (Connection connection : connections)
        {
            if(nickName.equals(connection.getUsername())) {
                continue;
            }
                connection.writeString(msg);
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
                if(line == null)
                    break;
                System.out.println("Server: got " + line);

                if(username == null) {
                    if(line.equals(""))
                        writer.write("Je moet wel een username invullen\n");
                    else {
                        username = line;
                        writer.write("Je bent nu " + line + "\n");
                        Server.WriteToAllExcept(line + " is net verbonden\n", username);
                    }
                }
                writer.flush();
            }
        }catch(Exception e) {

        }
        Server.disconnect(this);
    }

    public String getUsername() {
        return this.username;
    }

    public void writeString(String message){
        try{
            writer.write(message);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeDouble (Double message) {

    }
}