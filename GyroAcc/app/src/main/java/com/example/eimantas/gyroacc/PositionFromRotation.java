package com.example.eimantas.gyroacc;

/**
 * Created by Gintaras on 2016.03.31.
 */
public class PositionFromRotation implements Data {
    private static final int MAX_SENSITIVITY = 10;
    private static final int MIN_SENSITIVITY = 1;

    private float xAxis;
    private float yAxis;
    private float zAxis;

    private double xAxisDegrees;
    private double yAxisDegrees;
    private double zAxisDegrees;

    private double xCoordinateMonitor;
    private double yCoordinateMonitor;
    private int sensitivity;

    public PositionFromRotation(float xAxis, float yAxis, float zAxis, int sensitivity) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;

        this.xAxisDegrees = Math.asin(yAxis) * (-2);
        this.yAxisDegrees = Math.asin(zAxis) * (-2);
        this.zAxisDegrees = Math.asin(xAxis) * (-2);

        this.xCoordinateMonitor = 0;
        this.yCoordinateMonitor = 0;

        if(sensitivity < MIN_SENSITIVITY)
            this.sensitivity = MIN_SENSITIVITY;
        else if(sensitivity > MAX_SENSITIVITY)
            this.sensitivity = MAX_SENSITIVITY;
        else
            this.sensitivity = sensitivity;
    }

    public void processRotation (float xRotation, float yRotation, float zRotation) {
        double xRotationDegrees = Math.asin(yRotation) * (-2);
        double yRotationDegrees = Math.asin(zRotation) * (-2);
        double zRotationDegrees = Math.asin(xRotation) * (-2);



    }



}
