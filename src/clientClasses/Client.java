package clientClasses;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.PickResult;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import serverClasses.Lobby;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class Client extends Application {
    private String playerID;
    private TextField userNameTextField;
    private String progress;

    private BufferedReader reader;
    private BufferedWriter writer;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private Stage primaryStage;
    private BorderPane mainPane;

    private ArrayList<Lobby> lobbies;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.mainPane = new BorderPane();

        VBox topBar = new VBox();
        topBar.getChildren().add(new Label("Username: "));
        this.userNameTextField = new TextField();
        topBar.getChildren().add(this.userNameTextField);

        Button playButton = new Button("Play");
        playButton.setOnAction(e ->{
            new Thread(this::handleConnection).start();
        });


        this.mainPane.setCenter(playButton);
        this.mainPane.setTop(topBar);
        Scene scene = new Scene(this.mainPane);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.setScene(scene);
        primaryStage.setWidth(750);
        primaryStage.setHeight(500);
        primaryStage.show();
    }
    public void writeString(String message){
        try {
            writer.write(message);
            writer.flush();
            System.out.println("Client send: " + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleConnection() {
        try {
            Socket socket = new Socket("localhost", 1234);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());

            System.out.println("Reading data from socket...");

            //send username before handling connection
            this.writer.write(userNameTextField.getText() + "\n");
            System.out.println(userNameTextField.getText());
            this.writer.flush();

            while (socket.isConnected()) {
                String line = reader.readLine();
                System.out.println(line);

                if (line.equals("Deze username bestaat al")){
                    userNameTextField.clear();
                    Platform.runLater(() ->
                    {
                        //todo do stuff with received data
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Deze username bestaat al");
                        alert.showAndWait();
                    });
                } else if (line.equals("Welkom!")){
                    writer.write("send lobbies\n");
                    writer.flush();

                    lobbies = (ArrayList<Lobby>) inputStream.readObject();
                    System.out.println(lobbies);

                    Platform.runLater(() ->
                    {
                        BorderPane newPane = new BorderPane();
                        ListView<Lobby> listView = new ListView();
                        primaryStage.getScene().setRoot(newPane);
                        ObservableList<Lobby> observableList = FXCollections.observableArrayList(lobbies);
                        listView.setItems(observableList);
                        listView.setOnMouseClicked(event -> {
                            Lobby selectedLobby = listView.getSelectionModel().getSelectedItem();
//                            System.out.println("clicked on " + selectedLobby);

                            String connectArgument = "connectTo:" + selectedLobby.getLobbyName() + "\n";
                            writeString(connectArgument);
                        });
                        newPane.setTop(listView);
                    });
                }

            }
        } catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}