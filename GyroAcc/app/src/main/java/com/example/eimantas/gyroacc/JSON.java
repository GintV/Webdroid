package com.example.eimantas.gyroacc;


import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by Gintaras on 2016.03.22.
 */

/**
 * Wasted class (on 2016.04.18)
 */
public class JSON {
    private String json;

    public JSON() {
        this.json = "{}";
    }
    public JSON(String type, String data) {
        this.json = "{\n " +
                "\"type\": \"" + StringEscapeUtils.escapeJava(type) + " \", \n" +
                "\"data\": \"" + StringEscapeUtils.escapeJava(data) + " \", \n" +
                "}";
    }

    public String getJSON() {
        return this.json;
    }

    public void setJSON(String json) {
        this.json = json;
    }
}
