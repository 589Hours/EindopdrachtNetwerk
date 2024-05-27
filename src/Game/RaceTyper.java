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

import java.util.ArrayList;

public class RaceTyper extends Application {

    private ArrayList<String> texts;
    private TextFlow textFlow;
    private TextField inputField;
    private Label countdownLabel, wpmLabel;
    private int countdown;
    private long startTime, endTime;
    private boolean textDone = false;

    @Override
    public void start(Stage primaryStage) {
        texts = new ArrayList<>();
        texts.add("The quick brown fox jumps over the lazy dog.");
        texts.add("Hello, world! Welcome to the TypeRacer game.");
        texts.add("Java programming is fun and interesting.");

        primaryStage.setTitle("TypeRacer Game");

        countdownLabel = new Label("Countdown: 5");
        countdownLabel.setFont(new Font(24));

        textFlow = new TextFlow();
        textFlow.setStyle("-fx-font-size: 24px;");
        setTextFlow(texts.get(0));

        inputField = new TextField();
        inputField.setFont(new Font(24));
        inputField.setEditable(false);
        inputField.setOnKeyReleased(e -> checkInput());

        wpmLabel = new Label("");
        wpmLabel.setFont(new Font(24));

        VBox vbox = new VBox(10, countdownLabel, textFlow, inputField, wpmLabel);
        vbox.setPadding(new Insets(20));

        BorderPane root = new BorderPane(vbox);
        Scene scene = new Scene(root, 800, 300);
        wpmLabel.setText("WPM : 0");

        primaryStage.setScene(scene);
        primaryStage.show();

        countdown = 5;
        startCountdown();
    }

    private void startCountdown() {
        new Thread(() -> {
            try {
                while (countdown > 0) {
                    Platform.runLater(() -> countdownLabel.setText("Countdown: " + countdown));
                    Thread.sleep(1000);
                    countdown--;
                }
                Platform.runLater(() -> {
                    countdownLabel.setText("Go!");
                    startTyping();
                });
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
                if (typedText.charAt(i) == targetText.charAt(i)) {
                    t.setFill(Color.GREEN);
                } else {
                    t.setFill(Color.RED);
                }
            } else {
                t.setFill(Color.BLACK);
            }
        }

        if (!typedText.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            double timeTaken = (currentTime - startTime) / 1000.0 / 60.0;
            int wordCount = typedText.split("\\s+").length;
            double wpm = wordCount / timeTaken;
            wpmLabel.setText("WPM: " + (int) wpm);
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
            double minutes = timeTaken / 1000.0 / 60.0;
            double wpm = wordCount / minutes;
            wpmLabel.setText("WPM: " + (int) wpm);
            textDone = true;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
