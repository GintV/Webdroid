package citrusfresh.webdroid;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

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

    @Nullable
    static public Packet fromJSON(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, Packet.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}