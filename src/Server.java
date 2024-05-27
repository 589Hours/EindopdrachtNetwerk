
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
    private static ArrayList<Connection> connections = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("server");
        ServerSocket serverSocket = new ServerSocket(2002);
        while (true) {
            Socket socket = serverSocket.accept();
            Connection connection = new Connection(socket);
            connections.add(connection);


        }
    }
}

class Connection {
    private final Socket socket;
    private final DataInputStream reader;
    private final DataOutputStream writer;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            this.reader = new DataInputStream(socket.getInputStream());
            this.writer = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void write(){

    }

}