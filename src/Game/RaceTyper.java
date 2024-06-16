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
import java.util.Random;

public class RaceTyper extends Application {
    private ArrayList<String> texts;
    private TextFlow textFlow;
    private TextField inputField;
    private Label countdownLabel, wpmLabel, leaderboardLabel;
    private int countdown;
    private long startTime, endTime;
    private boolean textDone = false;
    private BufferedWriter writer;
    private Random random;
    private String text;

    public RaceTyper(BufferedWriter writer, String text) {
        this.writer = writer;
        this.text = text;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(new Scene(createContent(), 800, 400));
        primaryStage.show();
        startCountdown();
    }

    public VBox createContent() {
        texts = new ArrayList<>();
        texts.add("There are 10 types of people in the world: those who understand binary and those who don’t.");
        texts.add("Why do Java developers wear glasses? Because they don’t see sharp.");
        texts.add("Debugging: Being the detective in a crime movie where you are also the murderer.");
        texts.add("Why do programmers prefer dark mode? Because lights attracts bugs!");
        texts.add("A SQL query walks into a bar, walks up to two tables and asks, 'Can I join you?");
        texts.add("Programming is like writing a book... except if you miss out a single comma on page 126, the whole thing makes no sense.");
        texts.add("Why do programmers hate nature? It has too many bugs.");
        texts.add("In order to understand recursion, you must first understand recursion.");

        countdownLabel = new Label("Countdown: 5");
        countdownLabel.setFont(new Font(24));

        textFlow = new TextFlow();
        textFlow.setStyle("-fx-font-size: 24px;");
        setTextFlow(text);

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

    public void updateLeaderboard(String[] playerDataList) {
        System.out.println("updating leaderboard");
        Platform.runLater(() -> {
            StringBuilder leaderboardText = new StringBuilder("Leaderboard\n");

            for (String playerData : playerDataList) {
                leaderboardText.append(playerData).append("\n");
                System.out.println(leaderboardText);
            }

            String textToShow = leaderboardText.toString();
            System.out.println(textToShow);
            leaderboardLabel.setText(textToShow);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
