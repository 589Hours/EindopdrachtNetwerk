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
    private TextField userNameTextField;
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
    private Label playerCountLabel;
    private BorderPane lobbyPane = new BorderPane();
    private ListView<Lobby> listView = new ListView<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.mainPane = new BorderPane();

        VBox topBar = new VBox();
        topBar.getChildren().add(new Label("Username: "));
        this.userNameTextField = new TextField();
        topBar.getChildren().add(this.userNameTextField);

        Button playButton = new Button("Play");
        playButton.setOnAction(e -> new Thread(this::handleConnection).start());

        this.mainPane.setCenter(playButton);
        this.mainPane.setTop(topBar);
        Scene scene = new Scene(this.mainPane);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.setScene(scene);
        primaryStage.setWidth(750);
        primaryStage.setHeight(500);
        primaryStage.show();
    }

    private void writeString(String message) {
        try {
            writer.write(message + "\n");
            writer.flush();
            System.out.println("Client sent: " + message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleConnection() {
        try (Socket socket = new Socket("localhost", 1234)) {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());

            System.out.println("Reading data from socket...");

            writeString(userNameTextField.getText());
            System.out.println(userNameTextField.getText());

            while (socket.isConnected()) {
                String line = reader.readLine();
                System.out.println("Client: got " + line);

                switch (line) {
                    case "Deze username bestaat al":
                        userNameTextField.clear();
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setContentText("Deze username bestaat al");
                            alert.showAndWait();
                        });
                        break;
                    case "Welkom!":
                        writeString("send lobbies");
                        lobbies = (ArrayList<Lobby>) inputStream.readObject();
                        System.out.println(lobbies);

                        Platform.runLater(() -> {
                            ObservableList<Lobby> observableList = FXCollections.observableArrayList(lobbies);
                            listView.setItems(observableList);

                            listView.setOnMouseClicked(event -> {
                                Lobby selectedLobby = listView.getSelectionModel().getSelectedItem();
                                String connectArgument = "connectTo:" + selectedLobby.getLobbyName();
                                writeString(connectArgument);
                            });

                            lobbyPane.setTop(listView);
                            primaryStage.getScene().setRoot(lobbyPane);
                        });
                        break;
                    case "full":
                        // TODO: Display server is full to user
                        break;
                    case "accepted":
                        Platform.runLater(() -> {
                            BorderPane waitPane = new BorderPane();
                            countDownLabel = new Label("Waiting for players to start countdown");
                            playerCountLabel = new Label("Players in lobby: 1");

                            Button readyButton = new Button("Not ready");
                            readyButton.setStyle("-fx-background-color: Red");

                            Button backButton = new Button("Terug");

                            VBox vbox = new VBox(10, countDownLabel, playerCountLabel, readyButton, backButton);
                            waitPane.setCenter(vbox);
                            primaryStage.getScene().setRoot(waitPane);

                            readyButton.setOnAction(event -> {
                                ready = !ready;
                                if (ready) {
                                    writeString("ready:true");
                                    readyButton.setStyle("-fx-background-color: MediumSeaGreen");
                                    readyButton.setText("Ready");
                                } else {
                                    writeString("ready:false");
                                    readyButton.setStyle("-fx-background-color: Red");
                                    readyButton.setText("Not ready");
                                }
                            });

                            backButton.setOnAction(event -> {
                                returnToChooseLobby();
                                writeString("terug");
                            });
                        });
                        break;
                    case "start game":
                        Platform.runLater(() -> startGame(primaryStage));
                        break;
                    default:
                        if (line.matches("\\d+")) {
                            Platform.runLater(() -> countDownLabel.setText("Starting game in " + line + " seconds"));
                        } else if (line.startsWith("leaderboard:")) {
                            String leaderboard = line.substring(line.indexOf(":") + 1);
                            if (raceTyper != null) {
                                raceTyper.updateLeaderboard(leaderboard);
                            }
                        } else if (line.startsWith("playerCount:")) {
                            String playerCount = line.split(":")[1];
                            Platform.runLater(() -> playerCountLabel.setText("Players in lobby: " + playerCount));
                        }
                        break;
                    case "terug":
                        returnToChooseLobby();
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void startGame(Stage primaryStage) {
        raceTyper = new RaceTyper(writer);
        Scene gameScene = new Scene(raceTyper.createContent(), 800, 400);
        primaryStage.setScene(gameScene);
        raceTyper.start(primaryStage);
    }

    private void returnToChooseLobby() {
        Platform.runLater(() -> {
            primaryStage.getScene().setRoot(lobbyPane);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}