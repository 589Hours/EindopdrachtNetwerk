package serverClasses;

import java.util.ArrayList;
import java.util.HashMap;

public class Lobby {
    private ArrayList<Connection> players = new ArrayList<>();
    private HashMap<String, Double> progressMap = new HashMap<>();
    //todo game


    public Lobby(){
        new Thread();
    }

    public boolean addPlayer(Connection connection){
        if (players.size() >= 5){
            return false;
        }

        players.add(connection);
        return true;
    }
    public boolean removePlayer(Connection connection){
        if (players.isEmpty()){
            return false;
        }

        players.remove(connection);
        return true;
    }

    public HashMap<String, Double> getProgressMap() {
        return progressMap;
    }

    public void updateProgress(String username, double progress) {
        progressMap.put(username, progress);
        Server.broadcastProgress();
    }
}
