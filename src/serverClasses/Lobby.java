package serverClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Lobby implements Serializable, Runnable {
    private String lobbyName;
    private boolean started;
    private final int maxPlayers = 5;
    private ArrayList<Connection> players = new ArrayList<>();
    private HashMap<Connection, String> playerProgress = new HashMap<>();
    //todo game


    public Lobby(String name){
        this.lobbyName = name;
        new Thread(this); //thread for the lobby itself
    }

    @Override
    public void run() {
        while (true){
            if (!playersReady()){
                continue;
            }
            if (!started){
                for (Connection player : players) {
                    player.writeString("start countdown");
                }
                started = true;
            }

        }

    }

    private boolean playersReady() {
        return false;
    }

    public boolean addPlayer(Connection connection){
        if (players.size() >= maxPlayers){
            return false;
        }
        if (players.contains(connection)){
            return false;
        }

        players.add(connection);
        //todo notify all others
        //todo update lobby in server list?
        return true;
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
