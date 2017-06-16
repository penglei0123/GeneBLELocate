package com.genepoint.blelocate.core;

import java.io.Serializable;

public class LocPoint implements Serializable {
    public static final long serialVersionUID = 1L;

    public LocPoint() {

    }

    public LocPoint(String floor, double xcor, double ycor) {
        this.Floor = floor;
        this.Xcor = xcor;
        this.Ycor = ycor;
        this.timeStamp = System.currentTimeMillis();
    }

    public LocPoint(String building, String floor, double xcor, double ycor) {
        this.building = building;
        this.Floor = floor;
        this.Xcor = xcor;
        this.Ycor = ycor;
        this.timeStamp = System.currentTimeMillis();
        this.status = 0;
    }

    public LocPoint(int status) {
        this.building = null;
        this.Floor = null;
        this.status = status;
    }

    @Override
    public String toString() {
        return "LocPoint{" +
                "Floor='" + Floor + '\'' +
                ", Xcor=" + Xcor +
                ", Ycor=" + Ycor +
                ", status=" + status +
                '}';
    }

    public String Floor;
    public double Xcor;
    public double Ycor;
    public String building;
    public long timeStamp;//时间戳
    public int status = -1;//结果状态码
}
