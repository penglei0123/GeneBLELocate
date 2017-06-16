package com.genepoint.beacon.sdk.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.genepoint.beacon.utils.Preconditions;


public class BleRegion implements Parcelable {
    private final String identifier;
    private final String uuid;
    private final String macAddress;
    private final Integer major;
    private final Integer minor;

    public BleRegion(String identifier, String uuid, String macAddress, Integer major, Integer minor) {
        this.identifier = ((String) Preconditions.checkNotNull(identifier));
        this.uuid = (uuid != null ? RecordUtils.formatProximityUUID(uuid) : uuid);
        this.macAddress = (macAddress != null ? macAddress.replaceAll(":", "").toLowerCase() : null);
        this.major = major;
        this.minor = minor;
    }

    public String getIdentifier() {
        return this.identifier;
    }


    public String getUuid() {
        return this.uuid;
    }


    public Integer getMajor() {
        return this.major;
    }


    public Integer getMinor() {
        return this.minor;
    }


    public String getMacAddress() {
        return this.macAddress;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        BleRegion region = (BleRegion) o;
        if (this.identifier != null ? !this.identifier.equals(region.identifier) : region.identifier != null)
            return false;
        if (this.major != null ? !this.major.equals(region.major) : region.major != null)
            return false;
        if (this.minor != null ? !this.minor.equals(region.minor) : region.minor != null)
            return false;
        if (this.uuid != null ? !this.uuid.equals(region.uuid) : region.uuid != null) {
            return false;
        }
        if (this.macAddress != null ? !this.macAddress.equals(region.macAddress) : region.macAddress != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = this.uuid != null ? this.uuid.hashCode() : 0;
        result = result + this.identifier != null ? this.identifier.hashCode() : 0;
        result = result + this.macAddress != null ? this.macAddress.hashCode() : 0;
        result = 31 * result + (this.major != null ? this.major.hashCode() : 0);
        result = 31 * result + (this.minor != null ? this.minor.hashCode() : 0);
        return result;
    }

    public static final Creator<BleRegion> CREATOR = new Creator() {
        public BleRegion createFromParcel(Parcel source) {
            return new BleRegion(source);
        }

        public BleRegion[] newArray(int size) {
            return new BleRegion[size];
        }
    };

    private BleRegion(Parcel parcel) {
        this.identifier = parcel.readString();
        this.uuid = parcel.readString();
        this.macAddress = parcel.readString();
        Integer majorTemp = Integer.valueOf(parcel.readInt());
        if (majorTemp.intValue() == -1) {
            majorTemp = null;
        }
        this.major = majorTemp;
        Integer minorTemp = Integer.valueOf(parcel.readInt());
        if (minorTemp.intValue() == -1) {
            minorTemp = null;
        }
        this.minor = minorTemp;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.identifier);
        dest.writeString(this.uuid);
        dest.writeString(this.macAddress);
        dest.writeInt(this.major == null ? -1 : this.major.intValue());
        dest.writeInt(this.minor == null ? -1 : this.minor.intValue());
    }
}
