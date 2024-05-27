package clientClasses;

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

        Button playButton = new Button();
        playButton.setOnAction(e ->{
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
            Socket socket = new Socket("localhost", 2002);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Reading data from socket...");
            try{
                this.writer.write(userName.getText());
                System.out.println(userName.getText());
                this.writer.flush();
            } catch (IOException exception){
                exception.printStackTrace();
            }
            while (socket.isConnected()) {
                String line = reader.readLine();
                System.out.println(line);
                Platform.runLater(() ->
                {
                    //todo do stuff with received data
                });
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}