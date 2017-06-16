package com.genepoint.beacon.sdk.bean;

import android.os.Parcel;
import android.os.Parcelable;


public class Beacon implements Parcelable {
    private static int _supportOption;
    public String uuid = "";
    public String name = "";
    public String macAddress = "";
    public int major;
    public int minor;
    public int measuredPower;
    public int rssi;
    public int battery;
    public int temperature;
    public int light;
    public int led;

    public long millisTime;
    public int advMode;
    public String url = "";
    public String uid = "";

    public byte[] userData;
    public int aswIntervalMillis;
    public int netRestart;
    public int netType;
    public String wifiSsid;
    public String apSsid;
    public int apEnable;
    public int apChannel;
    public int apSecMode;
    public int apEncrypType;


    public Beacon() {
        this.advMode = -1;
        this.url = "";
        this.uid = "";
        this.userData = new byte[4];
        this.netRestart = -1;
        this.netType = -1;
        this.wifiSsid = null;
        this.apSsid = null;
        this.apEnable = 0;
        this.apChannel = 0;
        this.apSecMode = 3;
        this.apEncrypType = 3;
    }


    public Beacon(String uuid, String name, String macAddress, int major, int minor, int measuredPower) {
        this.advMode = -1;
        this.url = "";
        this.uid = "";
        this.userData = new byte[4];
        this.netRestart = -1;
        this.netType = -1;
        this.wifiSsid = null;
        this.apSsid = null;
        this.apEnable = 0;
        this.apChannel = 0;
        this.apSecMode = 3;
        this.apEncrypType = 3;
        this.uuid = RecordUtils.formatProximityUUID(uuid);
        this.name = name;
        this.macAddress = macAddress;
        this.major = major;
        this.minor = minor;
        this.measuredPower = measuredPower;
    }


    public Beacon(String uuid, String name, String macAddress, int major, int minor, int measuredPower, int rssi, int battery, int temperature) {
        this.advMode = -1;
        this.url = "";
        this.uid = "";
        this.userData = new byte[4];
        this.netRestart = -1;
        this.netType = -1;
        this.wifiSsid = null;
        this.apSsid = null;
        this.apEnable = 0;
        this.apChannel = 0;
        this.apSecMode = 3;
        this.apEncrypType = 3;
        this.uuid = RecordUtils.formatProximityUUID(uuid);
        this.name = name;
        this.macAddress = macAddress;
        this.major = major;
        this.minor = minor;
        this.measuredPower = measuredPower;
        this.rssi = rssi;
        this.battery = battery;
        this.temperature = temperature;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public void setMeasuredPower(int measuredPower) {
        this.measuredPower = measuredPower;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return this.uuid;
    }

    public int getLight() {
        return this.light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public int getLed() {
        return this.led;
    }

    public void setLed(int led) {
        this.led = led;
    }

    public String getName() {
        return this.name;
    }

    public String getMacAddress() {
        if(this.macAddress != null && this.macAddress.length() == 12) {
            String mac = this.macAddress.toUpperCase();
            return String.format("%s:%s:%s:%s:%s:%s", new Object[]{mac.substring(0, 2), mac.substring(2, 4), mac.substring(4, 6), mac.substring(6, 8), mac.substring(8, 10), mac.substring(10, 12)});
        } else {
            return this.macAddress;
        }
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getMeasuredPower() {
        return this.measuredPower;
    }

    public int getRssi() {
        return this.rssi;
    }

    public int getBattery() {
        return this.battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getAdvMode() {
        return this.advMode;
    }

    public void setAdvMode(int advMode) {
        this.advMode = advMode;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getAswIntervalMillis() {
        return this.aswIntervalMillis;
    }

    public void setAswIntervalMillis(int aswIntervalMillis) {
        this.aswIntervalMillis = aswIntervalMillis;
    }

    public byte[] getUserData() {
        return this.userData;
    }

    public void setUserData(byte[] userData) {
        this.userData = userData;
    }

    public boolean isSupport(int BrtSupports) {
        return (BrtSupports & this._supportOption) != 0;
    }

    public int getSupportOption() {
        return this._supportOption;
    }

    public long getMillsTime() {
        return this.millisTime;
    }

    public void setMillsTime(long millsTime) {
        this.millisTime = millsTime;
    }


    public String getWifiSsid() {
        return this.wifiSsid;
    }

    public void setWifiSsid(String wifiSsid) {
        this.wifiSsid = wifiSsid;
    }

    public String getApSsid() {
        return this.apSsid;
    }

    public void setApSsid(String apSsid) {
        this.apSsid = apSsid;
    }

    public int getApEnable() {
        return this.apEnable;
    }

    public void setApEnable(int apEnable) {
        this.apEnable = apEnable;
    }

    public int getApChannel() {
        return this.apChannel;
    }

    public void setApChannel(int apChannel) {
        this.apChannel = apChannel;
    }

    public int getApSecMode() {
        return this.apSecMode;
    }

    public void setApSecMode(int apSecMode) {
        this.apSecMode = apSecMode;
    }

    public int getApEncrypType() {
        return this.apEncrypType;
    }

    public void setApEncrypType(int apEncrypType) {
        this.apEncrypType = apEncrypType;
    }

    public static int getBrtSupportsCC254x() {
        return 1;
    }



    public String toString() {
        return "Beacon [uuid=" + this.uuid + ", name=" + this.name + ", macAddress=" + this.macAddress + ", major=" + this.major + ", minor=" + this.minor + ", measuredPower=" + this.measuredPower + ", rssi=" + this.rssi + ", battery=" + this.battery + ", temperature=" + this.temperature + ", light=" + this.light + ", led=" + this.led    +  ", millisTime=" + this.millisTime + "]";
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Beacon other = (Beacon) obj;

        if (this.macAddress == null) {
            if (other.macAddress != null)
                return false;
        } else if (!this.macAddress.equals(other.macAddress)) {
            return false;
        }
        return true;
    }


    public int hashCode() {
        return this.macAddress == null ? 1 : this.macAddress.hashCode();
    }


    public static final Creator<Beacon> CREATOR = new Creator() {
        public Beacon createFromParcel(Parcel source) {
            return new Beacon(source);
        }

        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

    protected Beacon(Parcel in) {
        this.advMode = -1;
        this.url = "";
        this.uid = "";
        this.userData = new byte[4];
        this.netRestart = -1;
        this.netType = -1;
        this.wifiSsid = null;
        this.apSsid = null;
        this.apEnable = 0;
        this.apChannel = 0;
        this.apSecMode = 3;
        this.apEncrypType = 3;
        this._supportOption = in.readInt();
        this.uuid = in.readString();
        this.name = in.readString();
        this.macAddress = in.readString();
        this.major = in.readInt();
        this.minor = in.readInt();
        this.measuredPower = in.readInt();
        this.rssi = in.readInt();
        this.battery = in.readInt();
        this.temperature = in.readInt();
        this.light = in.readInt();
        this.led = in.readInt();
        this.millisTime = in.readLong();
        this.advMode = in.readInt();
        this.url = in.readString();
        this.uid = in.readString();
        this.userData = in.createByteArray();
        this.aswIntervalMillis = in.readInt();
        this.netRestart = in.readInt();
        this.netType = in.readInt();
        this.wifiSsid = in.readString();
        this.apSsid = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this._supportOption);
        dest.writeString(this.uuid);
        dest.writeString(this.name);
        dest.writeString(this.macAddress);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.measuredPower);
        dest.writeInt(this.rssi);
        dest.writeInt(this.battery);
        dest.writeInt(this.temperature);
        dest.writeInt(this.light);
        dest.writeInt(this.led);
        dest.writeLong(this.millisTime);
        dest.writeInt(this.advMode);
        dest.writeString(this.url);
        dest.writeString(this.uid);
        dest.writeByteArray(this.userData);
        dest.writeInt(this.aswIntervalMillis);
        dest.writeInt(this.netRestart);
        dest.writeInt(this.netType);
        dest.writeString(this.wifiSsid);
        dest.writeString(this.apSsid);
    }
}

