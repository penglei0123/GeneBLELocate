package com.genepoint.datapack;

//
import java.io.Serializable;
import java.util.ArrayList;
import android.R.string;

public class Building implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3618665179689701629L;
	public String builingName;// 建筑名
	public String buildingID;// 建筑编号
	public String address;// 建筑地址
	public string buildingType;
	public int floorCount;

	public double leftBottomX;
	public double leftBottomY;
	public double pixelLeftTopX;
	public double rightTopX;
	public double rightTopY;
	public String builingMapPath;//地图路径
	
	public String datailaddress;// 详细地址
	public double latitude;// 经 度
	public double longitude;// 纬度
	public String province;// 省
	public String district;// 地区
	public String city;// 城市
	public ArrayList<Floor_fromJSON> floors = new ArrayList<Floor_fromJSON>();// 楼层
	public Building(){
		
	}
	public  Building(String name,String id) {
		this.builingName=name;
		this.buildingID=id;
		
	}
	public String getBuilingMapPath() {
		return builingMapPath;
	}
	public void setBuilingMapPath(String builingMapPath) {
		this.builingMapPath = builingMapPath;
	}
	public String getBuilingName() {
		return builingName;
	}
	public void setBuilingName(String builingName) {
		this.builingName = builingName;
	}
	public String getBuildingID() {
		return buildingID;
	}
	public void setBuildingID(String buildingID) {
		this.buildingID = buildingID;
	}	
	public void add(Floor_fromJSON floor){
		this.floors.add(floor);
		
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public string getBuildingType() {
		return buildingType;
	}
	public void setBuildingType(string buildingType) {
		this.buildingType = buildingType;
	}
	public int getFloorCount() {
		return floorCount;
	}
	public void setFloorCount(int floorCount) {
		this.floorCount = floorCount;
	}
	public double getLeftBottomX() {
		return leftBottomX;
	}
	public void setLeftBottomX(double leftBottomX) {
		this.leftBottomX = leftBottomX;
	}
	public double getLeftBottomY() {
		return leftBottomY;
	}
	public void setLeftBottomY(double leftBottomY) {
		this.leftBottomY = leftBottomY;
	}
	public double getPixelLeftTopX() {
		return pixelLeftTopX;
	}
	public void setPixelLeftTopX(double pixelLeftTopX) {
		this.pixelLeftTopX = pixelLeftTopX;
	}
	public double getRightTopX() {
		return rightTopX;
	}
	public void setRightTopX(double rightTopX) {
		this.rightTopX = rightTopX;
	}
	public double getRightTopY() {
		return rightTopY;
	}
	public void setRightTopY(double rightTopY) {
		this.rightTopY = rightTopY;
	}
	public String getDatailaddress() {
		return datailaddress;
	}
	public void setDatailaddress(String datailaddress) {
		this.datailaddress = datailaddress;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getDistrict() {
		return district;
	}
	public void setDistrict(String district) {
		this.district = district;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public ArrayList<Floor_fromJSON> getFloors() {
		return floors;
	}
	public void setFloors(ArrayList<Floor_fromJSON> floors) {
		this.floors = floors;
	}
	
}
