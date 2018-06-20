package br.ufrn.dimap.dim0863.domain;

import java.util.Date;

import br.ufrn.dimap.dim0863.util.DateUtil;

public class Location {

    private int id;
    private Date date;
    private double lat;
    private double lon;

    public Location(int id, Date date, double lat, double lon) {
        this(date, lat, lon);
        this.id = id;
    }

    public Location(Date date, double lat, double lon) {
        this.date = date;
        this.lat = lat;
        this.lon = lon;
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    @Override
    public String toString() {
        return String.format("%s | Location = (%f, %f)", DateUtil.convertToString(date), lat, lon);
    }

}
