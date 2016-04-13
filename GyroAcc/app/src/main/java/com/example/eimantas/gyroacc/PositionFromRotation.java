package com.example.eimantas.gyroacc;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Gintaras on 2016.03.31.
 */

public class PositionFromRotation implements Data {
    private static final int MAX_SENSITIVITY = 10;
    private static final int MIN_SENSITIVITY = 0;
    private static final boolean LINEAR_POINTER = false;
    private static final boolean TANGENTOIDAL_POINTER = true;

    private float[] invertedCalibratedRotationMatrix;
    private float[] relativeRotationMatrix;

    private class Pointer {
        public double x;
        public double y;
        public double sensitivity;
        public boolean pointerType;
    }

    public Pointer pointer;

    public PositionFromRotation() {
        this.invertedCalibratedRotationMatrix = new float[9];
        this.relativeRotationMatrix = new float[9];
        this.pointer = new Pointer();

        this.pointer.sensitivity = 1;
        this.pointer.pointerType = LINEAR_POINTER;
    }
    public PositionFromRotation(float[] rotationMatrix, int sensitivity, boolean pointerType) {
        this.invertedCalibratedRotationMatrix = new float[9];
        this.relativeRotationMatrix = new float[9];

        this.calibrate(rotationMatrix);
        this.setSensitivity(sensitivity);
        this.setPointerType(pointerType);
    }

    public double getXCoordinateMonitor() {
        return this.pointer.x;
    }
    public double getYCoordinateMonitor() {
        return this.pointer.y;
    }

    private void setXCoordinateMonitor(double xCoordinateMonitor) {
        this.pointer.x = xCoordinateMonitor;
    }
    private void setYCoordinateMonitor(double yCoordinateMonitor) {
        this.pointer.y = yCoordinateMonitor;
    }
    public void setSensitivity(int sensitivity) {
        if(sensitivity < MIN_SENSITIVITY)
            this.pointer.sensitivity = 1;
        else if(sensitivity > MAX_SENSITIVITY)
            this.pointer.sensitivity = 2;
        else
            this.pointer.sensitivity = 1 + (sensitivity / 10);
    }
    public void setPointerType(boolean pointerType) {
        this.pointer.pointerType = pointerType;
    }

    public void calibrate(float[] rotationMatrix) {
        /**
         * Unnecessary calculations
         *
        float det = rotationMatrix[0] * rotationMatrix[4] * rotationMatrix[8] +
                rotationMatrix[2] * rotationMatrix[3] * rotationMatrix[7] +
                rotationMatrix[1] * rotationMatrix[5] * rotationMatrix[6] -
                rotationMatrix[2] * rotationMatrix[4] * rotationMatrix[6] -
                rotationMatrix[1] * rotationMatrix[3] * rotationMatrix[8] -
                rotationMatrix[0] * rotationMatrix[5] * rotationMatrix[7];


        invertedCalibratedRotationMatrix[0] = (rotationMatrix[4] * rotationMatrix[8] - rotationMatrix[5] * rotationMatrix[7]);
        invertedCalibratedRotationMatrix[1] = (rotationMatrix[2] * rotationMatrix[7] - rotationMatrix[1] * rotationMatrix[8]);
        invertedCalibratedRotationMatrix[2] = (rotationMatrix[1] * rotationMatrix[5] - rotationMatrix[2] * rotationMatrix[4]);

        invertedCalibratedRotationMatrix[3] = (rotationMatrix[5] * rotationMatrix[6] - rotationMatrix[3] * rotationMatrix[8]);
        invertedCalibratedRotationMatrix[4] = (rotationMatrix[0] * rotationMatrix[8] - rotationMatrix[2] * rotationMatrix[6]);
        invertedCalibratedRotationMatrix[5] = (rotationMatrix[2] * rotationMatrix[3] - rotationMatrix[0] * rotationMatrix[5]);
         *
         */

        invertedCalibratedRotationMatrix[6] = (rotationMatrix[3] * rotationMatrix[7] - rotationMatrix[4] * rotationMatrix[6]);
        invertedCalibratedRotationMatrix[7] = (rotationMatrix[1] * rotationMatrix[6] - rotationMatrix[0] * rotationMatrix[7]);
        invertedCalibratedRotationMatrix[8] = (rotationMatrix[0] * rotationMatrix[4] - rotationMatrix[1] * rotationMatrix[3]);


        this.pointer.x = 0.0;
        this.pointer.y = 0.0;
    }

    public void processRotation (float[] rotationMatrix) {

        /**
         * Unnecessary calculations
         *
        relativeRotationMatrix[0] = invertedCalibratedRotationMatrix[0] * rotationMatrix[0] + invertedCalibratedRotationMatrix[1] * rotationMatrix[3] + invertedCalibratedRotationMatrix[2] * rotationMatrix[6];
        relativeRotationMatrix[1] = invertedCalibratedRotationMatrix[0] * rotationMatrix[1] + invertedCalibratedRotationMatrix[1] * rotationMatrix[4] + invertedCalibratedRotationMatrix[2] * rotationMatrix[7];
        relativeRotationMatrix[2] = invertedCalibratedRotationMatrix[0] * rotationMatrix[2] + invertedCalibratedRotationMatrix[1] * rotationMatrix[5] + invertedCalibratedRotationMatrix[2] * rotationMatrix[8];

        relativeRotationMatrix[3] = invertedCalibratedRotationMatrix[3] * rotationMatrix[0] + invertedCalibratedRotationMatrix[4] * rotationMatrix[3] + invertedCalibratedRotationMatrix[5] * rotationMatrix[6];
        relativeRotationMatrix[4] = invertedCalibratedRotationMatrix[3] * rotationMatrix[1] + invertedCalibratedRotationMatrix[4] * rotationMatrix[4] + invertedCalibratedRotationMatrix[5] * rotationMatrix[7];
        relativeRotationMatrix[5] = invertedCalibratedRotationMatrix[3] * rotationMatrix[2] + invertedCalibratedRotationMatrix[4] * rotationMatrix[5] + invertedCalibratedRotationMatrix[5] * rotationMatrix[8];
         *
         */

        relativeRotationMatrix[6] = invertedCalibratedRotationMatrix[6] * rotationMatrix[0] + invertedCalibratedRotationMatrix[7] * rotationMatrix[3] + invertedCalibratedRotationMatrix[8] * rotationMatrix[6];
        relativeRotationMatrix[7] = invertedCalibratedRotationMatrix[6] * rotationMatrix[1] + invertedCalibratedRotationMatrix[7] * rotationMatrix[4] + invertedCalibratedRotationMatrix[8] * rotationMatrix[7];
        relativeRotationMatrix[8] = invertedCalibratedRotationMatrix[6] * rotationMatrix[2] + invertedCalibratedRotationMatrix[7] * rotationMatrix[5] + invertedCalibratedRotationMatrix[8] * rotationMatrix[8];

        /**
         * Unnecessary calculations
         *
         double yawn = Math.atan2(relativeRotationMatrix[3], relativeRotationMatrix[0]) * 180 / Math.PI;
         *
         */

        double pitch = Math.atan2(relativeRotationMatrix[7], relativeRotationMatrix[8]) * 180 / Math.PI;
        double roll = Math.atan2(-relativeRotationMatrix[6], Math.sqrt(Math.pow(relativeRotationMatrix[7], 2) + Math.pow(relativeRotationMatrix[8], 2))) * 180 / Math.PI;

        double x;
        double y;

        if(this.pointer.pointerType == LINEAR_POINTER) {
            x = (roll / 45) * this.pointer.sensitivity;
            y = (pitch / 45) * this.pointer.sensitivity;
        }
        else {
            x = Math.tan(Math.toRadians(roll * this.pointer.sensitivity));
            y = Math.tan(Math.toRadians(pitch * this.pointer.sensitivity));
        }

        y *= -1;

        if(x > 1)
            x = 1;
        else if(x < -1)
            x = -1;

        if(y > 1)
            y = 1;
        else if(y < -1)
            y = -1;

        this.setXCoordinateMonitor(-x);
        this.setYCoordinateMonitor(y);
    }

    @Override
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
