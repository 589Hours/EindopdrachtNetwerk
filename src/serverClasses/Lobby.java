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
    private HashMap<Connection, String> playerProgress = new HashMap<>();



    public Lobby(String name){
        this.lobbyName = name;
        new Thread(this).start(); //thread for the lobby itself
    }

    @Override
    public void run() {
        int countdown = 30;
        boolean started = false;
        while (true) {
            if (!players.isEmpty() && countdown > 0) {
                countdown--;
                try {
                    String countdownText = String.valueOf(countdown);
                    players.forEach(player -> player.writeString(countdownText));
                    Thread.sleep(750);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!started) {
                for (Connection player : players) {
                    player.writeString("start game");
                }
                started = true;
            }
        }
    }

    private boolean playersReady() {
        return false;
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
        // TODO: Notify all others
        // TODO: Update lobby in server list
    }
    public boolean removePlayer(Connection connection){
        if (players.isEmpty()){
            return false;
        }
        //todo notify all others
        //todo update lobby in server list?
        players.remove(connection);
        return true;
    }

    private void updateProgress(){

    }

    public String getLobbyName() {
        return lobbyName;
    }

    public int getAvailableSpots(){
        return maxPlayers - players.size();
    }

    @Override
    public String toString() {
        return lobbyName + "\n" +
                "players in lobby " + players;
    }
}
