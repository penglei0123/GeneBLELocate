package com.genepoint.blelocate;

import java.io.Serializable;

/**
 * Created by jd on 2016/1/19.
 */
public class BeaconLocation implements Serializable {
    public static final long serialVersionUID = 1L;
    private String mac;
    private String tag;
    private int major;
    private int minor;
    private double posX, posY;
    private String floor;
    //某个位置该Beacon的信号强度
    private int level;

    public BeaconLocation(){}

    public BeaconLocation(int minor,double posX,double posY){
        this.minor = minor;
        this.posX = posX;
        this.posY = posY;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }


    public String getMAC() {
        return mac;
    }

    public void setMAC(String mac) {
        this.mac = mac;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "BeaconLocation{" +
                "mac='" + mac + '\'' +
                ", tag='" + tag + '\'' +
                ", posX=" + posX +
                ", posY=" + posY +
                '}';
    }
}
