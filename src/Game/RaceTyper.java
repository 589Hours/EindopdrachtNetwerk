package Game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RaceTyper extends Application {
    private ArrayList<String> texts;
    private TextFlow textFlow;
    private TextField inputField;
    private Label countdownLabel, wpmLabel, leaderboardLabel;
    private int countdown;
    private long startTime, endTime;
    private boolean textDone = false;
    private BufferedWriter writer;

    public RaceTyper(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(new Scene(createContent(), 800, 400));
        primaryStage.show();
        startCountdown();
    }

    public VBox createContent() {
        texts = new ArrayList<>();
        texts.add("The quick brown fox jumps over the lazy dog.");
        texts.add("Hello, world! Welcome to the TypeRacer game.");
        texts.add("Java programming is fun and interesting.");

        countdownLabel = new Label("Countdown: 5");
        countdownLabel.setFont(new Font(24));

        textFlow = new TextFlow();
        textFlow.setStyle("-fx-font-size: 24px;");
        setTextFlow(texts.get(0));

        inputField = new TextField();
        inputField.setFont(new Font(24));
        inputField.setEditable(false);
        inputField.setOnKeyReleased(e -> checkInput());

        wpmLabel = new Label("WPM: 0");
        wpmLabel.setFont(new Font(24));

        leaderboardLabel = new Label("Leaderboard");
        leaderboardLabel.setFont(new Font(24));

        VBox vbox = new VBox(10, countdownLabel, textFlow, inputField, wpmLabel, leaderboardLabel);
        vbox.setPadding(new Insets(20));

        return vbox;
    }

    private void startCountdown() {
        countdown = 5;
        new Thread(() -> {
            try {
                while (countdown > 0) {
                    Platform.runLater(() -> countdownLabel.setText("Countdown: " + countdown));
                    Thread.sleep(1000);
                    countdown--;
                }
                Platform.runLater(this::startTyping);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setTextFlow(String text) {
        textFlow.getChildren().clear();
        for (char c : text.toCharArray()) {
            Text t = new Text(String.valueOf(c));
            t.setFill(Color.BLACK);
            textFlow.getChildren().add(t);
        }
    }

    private void startTyping() {
        countdownLabel.setText("Go!");
        inputField.setEditable(true);
        inputField.requestFocus();
        startTime = System.currentTimeMillis();
    }

    private void checkInput() {
        String targetText = getTextFlowText();
        String typedText = inputField.getText();
        int len = Math.min(typedText.length(), targetText.length());

        for (int i = 0; i < targetText.length(); i++) {
            Text t = (Text) textFlow.getChildren().get(i);
            if (i < len) {
                t.setFill(typedText.charAt(i) == targetText.charAt(i) ? Color.GREEN : Color.RED);
            } else {
                t.setFill(Color.BLACK);
            }
        }

        if (!typedText.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            double timeTaken = (currentTime - startTime) / 1000.0 / 60.0;
            int wordCount = typedText.split("\\s+").length;
            int wpm = (int) (wordCount / timeTaken);
            wpmLabel.setText("WPM: " + wpm);

            try {
                writer.write("wpm:" + wpm + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            wpmLabel.setText("WPM: 0");
        }

        if (typedText.equals(targetText)) {
            endTime = System.currentTimeMillis();
            inputField.setEditable(false);
            calculateWPM();
        }
    }

    private String getTextFlowText() {
        StringBuilder sb = new StringBuilder();
        for (javafx.scene.Node node : textFlow.getChildren()) {
            sb.append(((Text) node).getText());
        }
        return sb.toString();
    }

    private void calculateWPM() {
        if (!textDone) {
            long timeTaken = endTime - startTime;
            int wordCount = getTextFlowText().split("\\s+").length;
            int wpm = (int) (wordCount / (timeTaken / 1000.0 / 60.0));
            wpmLabel.setText("WPM: " + wpm);
            textDone = true;
            try {
                writer.write("finished:" + wpm + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateLeaderboard(String leaderboard) {
        Platform.runLater(() -> leaderboardLabel.setText("Leaderboard\n" + leaderboard));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
