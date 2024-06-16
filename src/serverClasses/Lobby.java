package serverClasses;
import java.io.Serializable;
import java.util.*;

public class Lobby implements Serializable, Runnable {
    private String lobbyName;
    private final int maxPlayers = 5;
    private List<Connection> players = Collections.synchronizedList(new ArrayList<>());
    private HashMap<Connection, Integer> playerProgress = new HashMap<>();
    private HashMap<Connection, Boolean> playerReadyStatus = new HashMap<>();
    private boolean countdownStarted = false;
    private String gameText;
    private String[] texts = {
            "There are 10 types of people in the world: those who understand binary and those who don't.",
            "Why do Java developers wear glasses? Because they don’t see sharp.",
            "Debugging: Being the detective in a crime movie where you are also the murderer.",
            "Why do programmers prefer dark mode? Because lights attracts bugs!",
            "A SQL query walks into a bar, walks up to two tables and asks, 'Can I join you?",
            "Programming is like writing a book... except if you miss out a single comma on page 126, the whole thing makes no sense.",
            "Why do programmers hate nature? It has too many bugs.",
            "In order to understand recursion, you must first understand recursion"
    };

    public Lobby(String name) {
        this.lobbyName = name;
        this.gameText = getRandomText();
        new Thread(this).start(); // Thread for the lobby itself
    }

    @Override
    public void run() {
        while (true) {
            if (!countdownStarted && players.size() > 0) {
                startCountdown();
                countdownStarted = true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void startCountdown() {
        new Thread(() -> {
            int countdown = 30;
            while (countdown > 0) {
                try {
                    String countdownText = String.valueOf(countdown);
                    players.forEach(player -> player.writeString(countdownText));
                    Thread.sleep(1000);
                    countdown--;

                    if (allPlayersReady()) {
                        break;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            String selectedText = getRandomText();
            players.forEach(player -> player.writeString("start game" + selectedText));
        }).start();
    }

    private String getRandomText() {
        Random random = new Random();
        int randomIndex = random.nextInt(texts.length);
        return texts[randomIndex];
    }

    private boolean allPlayersReady() {
        return players.size() > 0 && playerReadyStatus.values().stream().allMatch(Boolean::booleanValue);
    }

    public void addPlayer(Connection player) {
        if (players.size() >= maxPlayers) {
            player.writeString("full");
            return;
        }
        if (players.contains(player)) {
            return;
        }

        players.add(player);
        playerReadyStatus.put(player, false);
        player.writeString("accepted");
        updatePlayerCount();
    }

    public void removePlayer(Connection connection) {
        if (!players.isEmpty()) {
            players.remove(connection);
            playerReadyStatus.remove(connection);
            updatePlayerCount();
        }
    }

    public void updatePlayerProgress(Connection player, int wpm) {
        playerProgress.put(player, wpm);
        updateLeaderboard();
    }

    public void setPlayerReady(Connection player, boolean isReady) {
        playerReadyStatus.put(player, isReady);

        if (allPlayersReady()) {
            startGame();
        }
    }

    private void updatePlayerCount() {
        String message = "playerCount:" + players.size();
        players.forEach(player -> player.writeString(message));
    }

    private void updateLeaderboard() {
        StringBuilder leaderboard = new StringBuilder();
        playerProgress.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .forEach(entry -> leaderboard.append(entry.getKey().getUsername())
                        .append(": ").append(entry.getValue()).append(" WPM,"));
        String leaderboardText = "leaderboard;" + leaderboard;
        players.forEach(player -> player.writeString(leaderboardText));
    }

    public void startGame() {
        players.forEach(player ->{
            try {
                player.writeString("start game");
                Thread.sleep(10);
                player.writeString(this.gameText);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } );
    }

    public String getLobbyName() {
        return lobbyName;
    }

    public int getAvailableSpots() {
        return maxPlayers - players.size();
    }

    public List<Connection> getPlayers() {
        return players;
    }

    @Override
    public String toString() {
        return lobbyName + " (" + players.size() + "/" + maxPlayers + " players)";
    }
}
