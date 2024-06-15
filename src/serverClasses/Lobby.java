package serverClasses;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Lobby implements Serializable, Runnable {
    private String lobbyName;
    private final int maxPlayers = 5;
    private List<Connection> players = Collections.synchronizedList(new ArrayList<>());
    private HashMap<Connection, Integer> playerProgress = new HashMap<>();
    private boolean countdownStarted = false;

    public Lobby(String name) {
        this.lobbyName = name;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            if (countdownStarted && players.size() > 0) {
                int countdown = 10;
                while (countdown > 0) {
                    try {
                        String countdownText = String.valueOf(countdown);
                        players.forEach(player -> player.writeString(countdownText));
                        Thread.sleep(1000);
                        countdown--;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                players.forEach(player -> player.writeString("start game"));
                countdownStarted = false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        player.writeString("accepted");
        if (!countdownStarted) {
            countdownStarted = true;
        }
        // TODO: Notify all others
        // TODO: Update lobby in server list
    }

    public boolean removePlayer(Connection connection) {
        if (players.isEmpty()) {
            return false;
        }
        // TODO: Notify all others
        // TODO: Update lobby in server list
        players.remove(connection);
        return true;
    }

    public void updatePlayerProgress(Connection player, int wpm) {
        playerProgress.put(player, wpm);
        updateLeaderboard();
    }

    private void updateLeaderboard() {
        StringBuilder leaderboard = new StringBuilder();
        playerProgress.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .forEach(entry -> leaderboard.append(entry.getKey().getUsername())
                        .append(": ").append(entry.getValue()).append(" WPM\n"));
        String leaderboardText = "leaderboard:" + leaderboard.toString();
        players.forEach(player -> player.writeString(leaderboardText));
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
        return lobbyName + "\n" +
                "players in lobby " + players;
    }
}
