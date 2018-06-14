package br.ufrn.dimap.dim0863.domain;

import java.util.Date;

public class CarInfo {

    private int id;
    private Date date;
    private String licensePlate;
    private int speed;
    private int rpm;

    public CarInfo(int id, Date date, String licensePlate, int speed, int rpm) {
        this(date, licensePlate, speed, rpm);
        this.id = id;
    }

    public CarInfo(Date date, String licensePlate, int speed, int rpm) {
        this.date = date;
        this.licensePlate = licensePlate;
        this.speed = speed;
        this.rpm = rpm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm) {
        this.rpm = rpm;
    }
}
