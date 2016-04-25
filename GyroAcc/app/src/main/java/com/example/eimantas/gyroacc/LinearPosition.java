package com.example.eimantas.gyroacc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Gintaras on 2016.04.24.
 */

public class LinearPosition implements Data{
    private static final double FREE_FALL_ACCELERATION = 9.80665;
    private static final double TOLERANCE = 2.0;
    private static final double MAX_CURRENT_SPEED = 5.0;

    private static final double MAX_MOTION = 1.0;
    private static final double MIN_MOTION = -1.0;

    private double linearMotion;
    private double currentSpeed;
    private double linearAcceleration;
    private long timeStamp;

    private int trigger;

    private class Pointer {
        public double x;
        public double y = 0.0;
        //public double sensitivity;
        //public boolean pointerType;
        //public int pointerOrientation;
    }

    private Pointer pointer;

    public LinearPosition(long timeStamp) {
        this.linearMotion = 0.0;
        this.currentSpeed = 0.0;
        this.linearAcceleration = 0.0;
        this.timeStamp = timeStamp;
        this.trigger = 1;

        this.pointer = new Pointer();
    }

    public double getLinearMotion() {
        return this.linearMotion;
    }
    public double getCurrentSpeed() {
        return this.currentSpeed;
    }
    public double getLinearAcceleration() {
        return this.linearAcceleration;
    }

    public void processLinearMotion(double accX, double accY, double accZ, long currentTimeStamp) {
        linearAcceleration = Math.sqrt(Math.pow(accX, 2) +
                Math.pow(accY, 2) + Math.pow(accZ, 2)) - FREE_FALL_ACCELERATION;

        if(linearAcceleration > TOLERANCE)
            this.currentSpeed += (linearAcceleration * (currentTimeStamp - this.timeStamp))/4;

        if(this.currentSpeed > MAX_CURRENT_SPEED)
            this.currentSpeed = MAX_CURRENT_SPEED;

        this.linearMotion += (this.trigger * this.currentSpeed * (currentTimeStamp - this.timeStamp))/1000000000.0;

        if(this.linearMotion > MAX_MOTION) {
            this.linearMotion = 1.0;
            this.currentSpeed = 0.0;
            this.trigger = -1;
        }
        else if(this.linearMotion < MIN_MOTION) {
            this.linearMotion = -1.0;
            this.currentSpeed = 0.0;
            this.trigger = 1;
        }

        this.timeStamp = currentTimeStamp;
        this.pointer.x = this.linearMotion;
    }

    public String toJSON() {
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonString = null;

        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.pointer);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}
