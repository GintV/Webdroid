package citrusfresh.webdroid;

import java.util.Random;

/**
 * Created by Gintaras on 2016.05.16.
 */

public class Data{
    private Random random;

    private String sessionID_;
    private String playerName_;
    private char playerInitials_;
    private String playerColor_;
    private double playerPositionX_;
    private double playerPositionY_;
    private boolean playerIsCalibrating_;

    private class NewPlayer{
        private String sessionID;
        private String playerName;
        private char playerInitials;
        private String playerColor;

        public NewPlayer(){
            this.update();
        }

        public void update(){
            this.sessionID = sessionID_;
            this.playerName = playerName_;
            this.playerInitials = playerInitials_;
            this.playerColor = playerColor_;
        }
    }

    private NewPlayer newPlayer;

    private class PlayerPosition{
        private double playerPositionX;
        private double playerPositionY;

        public PlayerPosition(){
            this.update();
        }

        public void update(){
            this.playerPositionX = playerPositionX_;
            this.playerPositionY = playerPositionY_;
        }
    }

    private PlayerPosition playerPosition;

    private class PlayerInfoChange{
        private String playerName;
        private char playerInitials;
        private String playerColor;

        public PlayerInfoChange(){
            this.update();
        }

        public void update(){
            this.playerName = playerName_;
            this.playerInitials = playerInitials_;
            this.playerColor = playerColor_;
        }
    }

    private PlayerInfoChange playerInfoChange;

    private class NewConnection{
        private String sessionID;

        public NewConnection(){
            this.update();
        }

        public void update(){
            this.sessionID = sessionID_;
        }
    }

    private NewConnection newConnection;


    public Data(){
        this.random = new Random();

        this.sessionID_ = "";
        this.playerName_ = "player" + random.nextInt(9999);
        this.playerInitials_ = ' ';
        this.playerColor_ = "random";
        this.playerPositionX_ = 0.0;
        this.playerPositionY_ = 0.0;
        this.playerIsCalibrating_ = false;

        this.newPlayer = new NewPlayer();
        this.playerPosition = new PlayerPosition();
        this.playerInfoChange = new PlayerInfoChange();
        this.newConnection = new NewConnection();

    }

    public NewPlayer getNewPlayer(){
        return this.newPlayer;
    }

    public PlayerPosition getPlayerPosition(){
        return this.playerPosition;
    }

    public PlayerInfoChange getPlayerInfoChange(){
        return this.playerInfoChange;
    }

    public NewConnection getNewConnection(){
        return this.newConnection;
    }

    public void setSessionID(String sessionID){
        this.sessionID_ = sessionID;
        this.newPlayer.update();
        this.newConnection.update();
    }

    public void setPlayerName(String playerName){
        this.playerName_ = playerName;
        this.newPlayer.update();
        this.playerInfoChange.update();
    }

    public void setPlayerInitials(char playerInitials){
        this.playerInitials_ = playerInitials;
        this.newPlayer.update();
        this.playerInfoChange.update();
    }

    public void setPlayerColor(String playerColor){
        this.playerColor_ = playerColor;
        this.newPlayer.update();
        this.playerInfoChange.update();
    }

    public void setPlayerPosition(double playerPositionX, double playerPositionY){
        this.playerPositionX_ = playerPositionX;
        this.playerPositionY_ = playerPositionY;
        this.playerPosition.update();
    }

    public void setPlayerIsCalibrating(boolean playerIsCalibrating){
        this.playerIsCalibrating_ = playerIsCalibrating;
    }
}
