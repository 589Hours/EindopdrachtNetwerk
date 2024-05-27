package serverClasses;

import java.util.ArrayList;

public class Lobby {
    private ArrayList<Connection> players = new ArrayList<>();
    //todo game


    public Lobby(){
        new Thread(); //thread for the lobby itself
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

    private void updateProgress(){

    }
}
