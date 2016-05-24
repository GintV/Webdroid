package citrusfresh.webdroid;

import java.util.Random;

/**
 * Created by Gintaras on 2016.05.16.
 */

public class Data {
    private Random random;

    private String sessionID_;
    private String playerName_;
    private String playerInitials_;
    private String playerColor_;
    private double playerPositionX_;
    private double playerPositionY_;
    private boolean playerIsCalibrating_;
    private boolean playerIsReady_;

    private class NewPlayer{
        private String sessionID;
        private String playerName;
        private String playerInitials;
        private String playerColor;

        public String getPlayerColor() {
            return playerColor;
        }

        public String getSessionID() {
            return sessionID;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getPlayerInitials() {
            return playerInitials;
        }

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

        public double getPlayerPositionX() {
            return playerPositionX;
        }

        public double getPlayerPositionY() {
            return playerPositionY;
        }

        public PlayerPosition(){
            this.update();
        }

        public void update(){
            this.playerPositionX = playerPositionX_;
            this.playerPositionY = playerPositionY_;
        }
    }

    private PlayerPosition playerPosition;

    public class PlayerInfoChange {
        private String playerName;
        private String playerInitials;
        private String playerColor;
        private boolean playerIsReady;

        public String getPlayerName() {
            return playerName;
        }

        public String getPlayerInitials() {
            return playerInitials;
        }

        public String getPlayerColor() {
            return playerColor;
        }

        public boolean isPlayerIsReady() {
            return playerIsReady;
        }

        public PlayerInfoChange(){
            this.update();
        }

        public void update(){
            this.playerName = playerName_;
            this.playerInitials = playerInitials_;
            this.playerColor = playerColor_;
            this.playerIsReady = playerIsReady_;
        }
    }

    private PlayerInfoChange playerInfoChange;

    private class NewConnection{
        private String sessionID;

        public String getSessionID() {
            return sessionID;
        }

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
        this.playerInitials_ = " ";
        this.playerColor_ = "random";
        this.playerPositionX_ = 0.0;
        this.playerPositionY_ = 0.0;
        this.playerIsCalibrating_ = false;
        this.playerIsReady_ = false;

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

    public boolean getPlayerIsReady(){
        return this.playerIsReady_;
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

    public void setPlayerInitials(String playerInitials){
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

    public void setPlayerIsReady(boolean isPlayerReady) {
        this.playerIsReady_ = isPlayerReady;
    }
}
