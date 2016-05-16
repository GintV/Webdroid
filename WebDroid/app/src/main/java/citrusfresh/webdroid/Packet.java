package citrusfresh.webdroid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Created by Gintaras on 2016.05.15.
 */
public class Packet {
    public static final String TYPE_NEW_PLAYER = "newPlayer";
    public static final String TYPE_PLAYER_INFO_CHANGE = "changePlayer";
    public static final String TYPE_PLAYER_POSITION = "joystick";
    public static final String TYPE_NEW_CONNECTION = "newConnection";

    private String type;
    private Object data;

    public Packet() {

    }

    public Packet(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public void setType(String type){
        this.type = type;
    }

    public void setData(Object data){
        this.data = data;
    }

    public String getType(){
        return type;
    }

    public Object getData(){ return data; }

    public String toJSON() {
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = null;

        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

}