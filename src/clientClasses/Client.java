package clientClasses;

import Game.RaceTyper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class Client extends Application {
    private String playerID;
    private TextField userName;
    private String progress;
    private BufferedReader reader;
    private BufferedWriter writer;

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane mainPane = new BorderPane();

        VBox topBar = new VBox();
        topBar.getChildren().add(new Label("Username: "));
        userName = new TextField();
        topBar.getChildren().add(userName);

        Button playButton = new Button("Play");
        playButton.setOnAction(e -> {
            new Thread(this::handleConnection).start();
        });

        mainPane.setCenter(playButton);
        mainPane.setTop(topBar);
        Scene scene = new Scene(mainPane);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleConnection() {
        try {
            Socket socket = new Socket("localhost", 1234);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Reading data from socket...");

            this.writer.write(userName.getText() + "\n");
            System.out.println(userName.getText());
            this.writer.flush();

            Platform.runLater(() -> startRaceTyper(writer));

            while (socket.isConnected()) {
                String line = reader.readLine();
                System.out.println(line);
                Platform.runLater(() -> {
                    if (line.startsWith("PROGRESS_LIST")) {
                        updateProgressList(line);
                    } else if (line.startsWith("PROGRESS")) {
                    } else {
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateProgressList(String message) {
        String[] parts = message.split(" ");
        Map<String, Double> progressMap = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            String[] userProgress = parts[i].split(":");
            if (userProgress.length == 2) {
                progressMap.put(userProgress[0], Double.parseDouble(userProgress[1]));
            }
        }
    }

    private void startRaceTyper(BufferedWriter writer) {
        try {
            new RaceTyper(writer).start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}