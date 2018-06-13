package br.ufrn.dimap.dim0863.domain;

public class CarInfo {

    private int id;
    private String licensePlate;
    private int speed;
    private int rpm;

    public CarInfo(int id, String licensePlate, int speed, int rpm) {
        this.id = id;
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
