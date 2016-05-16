package citrusfresh.webdroid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Gintaras on 2016.05.15.
 */
public class Packet {
    private static final String TYPE_NEW_PLAYER = "newPlayer";
    private static final String TYPE_PLAYER_INFO_CHANGE = "changePlayer";
    private static final String TYPE_PLAYER_POSITION = "joystick";
    private static final String TYPE_PLAYER_READY = "readyPlayer";
    private static final String TYPE_NEW_CONNECTION = "newConnection";

    private String type;
    private Object data;

    public void setType(String type){
        this.type = type;
    }

    public void setData(Object data){
        this.data = data;
    }

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