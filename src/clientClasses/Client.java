package clientClasses;

import Game.RaceTyper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    private RaceTyper raceTyper;

    private ArrayList<Lobby> lobbies;
    private Boolean ready = false;
    
    private Label countDownLabel;

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
            writer.write(message + "\n");
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
            writeString(userNameTextField.getText());
            System.out.println(userNameTextField.getText());

            while (socket.isConnected()) {
                String line = reader.readLine();
                System.out.println("client: got " + line);

                if (line.equals("Deze username bestaat al")){
                    userNameTextField.clear();
                    Platform.runLater(() ->
                    {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("Deze username bestaat al");
                        alert.showAndWait();
                    });
                } else if (line.equals("Welkom!")){
                    writeString("send lobbies");

                    lobbies = (ArrayList<Lobby>) inputStream.readObject();
                    System.out.println(lobbies);

                    Platform.runLater(() ->
                    {
                        BorderPane newPane = new BorderPane();
                        ListView<Lobby> listView = new ListView();
                        ObservableList<Lobby> observableList = FXCollections.observableArrayList(lobbies);
                        listView.setItems(observableList);

                        listView.setOnMouseClicked(event -> {
                            Lobby selectedLobby = listView.getSelectionModel().getSelectedItem();
//                            System.out.println("clicked on " + selectedLobby);

                            String connectArgument = "connectTo:" + selectedLobby.getLobbyName();
                            writeString(connectArgument);
                        });

                        newPane.setTop(listView);
                        primaryStage.getScene().setRoot(newPane);
                    });

                } else if (line.equals("full")){
                    //todo display server is full to user

                } else if (line.equals("accepted")){
                    Platform.runLater(() ->{
                        BorderPane waitpane = new BorderPane();
                        countDownLabel = new Label();
                        countDownLabel.setText("Waiting for players to start countdown");
                        Button readyButton = new Button();
                        readyButton.setStyle("-fx-background-color: Red");
                        readyButton.setText("Not ready");

                        waitpane.setTop(countDownLabel);
                        waitpane.setCenter(readyButton);
                        primaryStage.getScene().setRoot(waitpane);

                        readyButton.setOnAction(event -> {
                            ready = !ready;
                            if (ready) {
                                writeString("ready");
                                readyButton.setStyle("-fx-background-color: MediumSeaGreen");
                                readyButton.setText("Ready");
                                System.out.println("ready");
                            } else {
                                writeString("not ready");
                                readyButton.setStyle("-fx-background-color: Red");
                                readyButton.setText("Not ready");
                                System.out.println("not ready");
                            }
                        });
                    });
                } else if (line.equals("start game")){
                    raceTyper = new RaceTyper();
                    raceTyper.start(new Stage());
                } else if(line.matches("\\d")){
                    Platform.runLater(() -> {
                        countDownLabel.setText("Starting game in " + line + " seconds");
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