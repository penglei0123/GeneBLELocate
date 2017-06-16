package com.genepoint.beacon.sdk.bean;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.genepoint.beacon.utils.EddyStone;
import com.genepoint.beacon.utils.L;
import com.genepoint.beacon.utils.Preconditions;
import com.genepoint.beacon.utils.Util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

@SuppressLint({"NewApi"})
public class RecordUtils {
    private static ConcurrentHashMap<String, Beacon> mBeaconMap = new ConcurrentHashMap();
    private static ConcurrentHashMap<String, Beacon> mSimpleBeaconMap = new ConcurrentHashMap();

    public static void clearCacheBeaconExtra() {
        Iterator var0 = mBeaconMap.values().iterator();

        while(var0.hasNext()) {
            Beacon beacon = (Beacon)var0.next();
            if(beacon != null) {
                beacon.setUid("");
                beacon.setUrl("");
            }
        }

    }

    public static Beacon beaconFromLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        int battery = 0;
        int temperature = 0;
        int hardwareType = -1;
        int firmwareNum = 0;
        int light = 0;
        int deviceMode = -10000;
        int autoSleep = 0;
        boolean isBB = false;
        byte[] userData = null;
        Beacon cacheBeacon = (Beacon)mBeaconMap.get(device.getAddress());
        String deviceName = device.getName();
        if(deviceName != null) {
            deviceName = Util.MatchCode(deviceName);
        }

        if (EddyStone.isEddyStone(scanRecord)) {
            int calibratedTxPower = scanRecord[12];

            Beacon proximityUUID1 = new Beacon();
            proximityUUID1.setName(deviceName);
            proximityUUID1.setMacAddress(device.getAddress());
            proximityUUID1.setMeasuredPower(calibratedTxPower);
            boolean major1 = true;
            String minor1 = null;
            String measuredPower1 = null;

            if (EddyStone.isUidPacket(scanRecord)) {
                proximityUUID1.setAdvMode(2);
                minor1 = scanRecordtoString(EddyStone.of(scanRecord, 13, 16)).toString();
                proximityUUID1.setUid(minor1);
            }else{
                if(!EddyStone.isUrlPacket(scanRecord)) {
                    return null;
                }
                proximityUUID1.setAdvMode(3);
                int beacon4 = byte2Int(new byte[]{scanRecord[7]});
                measuredPower1 = Util.MatchCode(EddyStone.urlV2(EddyStone.of(scanRecord, 13, beacon4 - 5)));
                proximityUUID1.setUrl(measuredPower1);

            }
            if(cacheBeacon != null) {
                cacheBeacon.setName(deviceName);
                cacheBeacon.setRssi(rssi);
                cacheBeacon.setMeasuredPower(proximityUUID1.getMeasuredPower());
                if(minor1 != null) {
                    cacheBeacon.setUid(minor1);
                }

                if(measuredPower1 != null) {
                    cacheBeacon.setUrl(measuredPower1);
                }

                if(cacheBeacon.getAdvMode() != 1) {
                    cacheBeacon.setAdvMode(proximityUUID1.getAdvMode());
                }

                return cacheBeacon;
            } else {
                mBeaconMap.put(device.getAddress(), proximityUUID1);
                return proximityUUID1;
            }

        }
       else  if (!EddyStone.isiBeacon(scanRecord)) {
            if (cacheBeacon == null) {
                Beacon beacon2 = new Beacon("00000000000000000000000000000000", deviceName, device.getAddress(), 0, 0, rssi);
                mBeaconMap.put(beacon2.getMacAddress(), beacon2);
                return beacon2;
            } else {
                cacheBeacon.setName(deviceName);
                cacheBeacon.setRssi(rssi);
                return cacheBeacon;
            }
        }else {
            String scanRecordAsHex =scanRecordtoString(scanRecord);
            String proximityUUID = String.format("%s-%s-%s-%s-%s", new Object[]{scanRecordAsHex.substring(18, 26), scanRecordAsHex.substring(26, 30), scanRecordAsHex.substring(30, 34), scanRecordAsHex.substring(34, 38), scanRecordAsHex.substring(38, 50)});
            int major = unsignedByteToInt(scanRecord[25]) * 256 + unsignedByteToInt(scanRecord[26]);
            int minor = unsignedByteToInt(scanRecord[27]) * 256 + unsignedByteToInt(scanRecord[28]);
            int measuredPower = scanRecord[29];
            battery = scanRecord[46];
            Beacon beacon1= new Beacon(!TextUtils.isEmpty(proximityUUID) ? proximityUUID.toUpperCase() : null, !TextUtils.isEmpty(device.getName()) ? Util.MatchCode(device.getName().trim()) : null, device.getAddress(), major, minor, measuredPower, rssi, battery,
                    temperature);
            beacon1.setAdvMode(1);
            if(cacheBeacon == null) {
                if(userData != null) {
                    beacon1.setUserData(userData);
                }

                mBeaconMap.put(beacon1.getMacAddress(), beacon1);
                return beacon1;
            } else {
                cacheBeacon.setAdvMode(1);
                cacheBeacon.setRssi(rssi);
                cacheBeacon.setUuid(beacon1.getUuid());
                cacheBeacon.setName(deviceName);
                cacheBeacon.setMacAddress(device.getAddress());
                cacheBeacon.setMajor(beacon1.getMajor());
                cacheBeacon.setMinor(beacon1.getMinor());
                cacheBeacon.setMeasuredPower(beacon1.getMeasuredPower());
                cacheBeacon.setBattery(beacon1.getBattery());
                cacheBeacon.setTemperature(beacon1.getTemperature());
                cacheBeacon.setLight(beacon1.getLight());
                if(userData != null) {
                    cacheBeacon.setUserData(userData);
                }
                return cacheBeacon;
            }
        }
    }


    public static Beacon simpleBeaconFromLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Beacon cacheBeacon = (Beacon)mSimpleBeaconMap.get(device.getAddress());
        String deviceName = device.getName();
        if(deviceName != null) {
            deviceName = Util.MatchCode(deviceName);
        }

        if(!EddyStone.isiBeacon(scanRecord)) {
            if(cacheBeacon != null) {
                cacheBeacon.setName(deviceName);
                cacheBeacon.setRssi(rssi);
                return cacheBeacon;
            } else {
                return null;
            }
        } else {
            int battery = 0;
            int temperature = 0;
            String scanRecordAsHex = scanRecordtoString(scanRecord);
            String proximityUUID = String.format("%s-%s-%s-%s-%s", new Object[]{scanRecordAsHex.substring(18, 26), scanRecordAsHex.substring(26, 30), scanRecordAsHex.substring(30, 34), scanRecordAsHex.substring(34, 38), scanRecordAsHex.substring(38, 50)});
            int major = unsignedByteToInt(scanRecord[25]) * 256 + unsignedByteToInt(scanRecord[26]);
            int minor = unsignedByteToInt(scanRecord[27]) * 256 + unsignedByteToInt(scanRecord[28]);
            byte measuredPower = scanRecord[29];
            byte[] userData = null;
            Beacon beacon = new Beacon(!TextUtils.isEmpty(proximityUUID)?proximityUUID.toUpperCase():null, deviceName, device.getAddress(), major, minor, measuredPower, rssi, battery, temperature);
            beacon.setAdvMode(1);
            if(cacheBeacon == null) {
                if(userData != null) {
                    beacon.setUserData(userData);
                }
                mSimpleBeaconMap.put(beacon.getMacAddress(), beacon);
                return beacon;
            } else {
                cacheBeacon.setRssi(rssi);
                cacheBeacon.setUuid(beacon.getUuid());
                cacheBeacon.setName(deviceName);
                cacheBeacon.setMacAddress(device.getAddress());
                cacheBeacon.setMajor(beacon.getMajor());
                cacheBeacon.setMinor(beacon.getMinor());
                cacheBeacon.setMeasuredPower(beacon.getMeasuredPower());
                cacheBeacon.setBattery(beacon.getBattery());
                cacheBeacon.setTemperature(beacon.getTemperature());
                cacheBeacon.setLight(beacon.getLight());
                if(userData != null) {
                    cacheBeacon.setUserData(userData);
                }
                return cacheBeacon;
            }
        }
    }

    public static String formatProximityUUID(String proximityUUID) {
        if (proximityUUID == null) {
            return "";
        }
        String withoutDashes = proximityUUID.replace("-", "").toLowerCase();
        Preconditions.checkArgument(withoutDashes.length() == 32, "Proximity UUID must be 32 characters without dashes");

        return String.format("%s-%s-%s-%s-%s", new Object[]{withoutDashes.substring(0, 8), withoutDashes.substring(8, 12), withoutDashes.substring(12, 16), withoutDashes.substring(16, 20), withoutDashes.substring(20, 32)});
    }

    public static boolean isAvailableUUID(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return false;
        }
        String withoutDashes = uuid.replace("-", "").toLowerCase();
        return withoutDashes.length() == 32;
    }

    public static boolean isBeaconInRegion(Beacon beacon, BleRegion region) {
        if ((region.getMacAddress() != null) && (!beacon.getMacAddress().replaceAll(":", "").toLowerCase().equals(region.getMacAddress().replaceAll(":", "").toLowerCase()))) {
            return false;
        }
        if ((region.getUuid() != null) && (!beacon.getUuid().replaceAll("-", "").toLowerCase().equals(region.getUuid().replaceAll("-", "").toLowerCase()))) {
            return false;
        }
        if ((region.getMajor() != null) && (beacon.getMajor() != region.getMajor().intValue())) {
            return false;
        }
        if ((region.getMinor() != null) && (beacon.getMinor() != region.getMinor().intValue())) {
            return false;
        }
        return true;
    }

    public static double computeAccuracy(Beacon beacon) {
        if ((beacon.getRssi() >= 0) || (beacon.getMeasuredPower() >= 0)) {
            return -1.0D;
        }
        double ratio = Double.parseDouble(String.valueOf(beacon.getRssi())) / beacon.getMeasuredPower();
        double rssiCorrection = 0.96D + Math.pow(Math.abs(beacon.getRssi()), 3.0D) % 10.0D / 150.0D;

        if (ratio <= 1.0D) {
            return Math.pow(ratio, 9.98D) * rssiCorrection;
        }
        double distance = Math.max(0.0D, (0.103D + 0.89978D * Math.pow(ratio, 7.5D)) * rssiCorrection);

        if (0.0D / 0.0== distance){
            return -1.0D;
        }
        return distance;
    }

    public static byte[] inttobyte(int value) {
        byte[] buf = new byte[2];
        buf[0] = ((byte) (value >> 8 & 0xFF));
        buf[1] = ((byte) (value & 0xFF));
        return buf;
    }

    public static byte[] inttobyte4(int value) {
        byte[] buf = new byte[4];
        buf[3] = ((byte) (value & 0xFF));
        buf[2] = ((byte) (value >> 8 & 0xFF));
        buf[1] = ((byte) (value >> 16 & 0xFF));
        buf[0] = ((byte) (value >> 24 & 0xFF));
        return buf;
    }

    public static byte[] string2HexStr(String string) {
        if (TextUtils.isEmpty(string)) {
            return new byte[0];
        }
        byte[] bytes = new byte[string.length() / 2];
        for (int i = 0; i < string.length(); i += 2) {
            if (i + 1 >= string.length()) {
                break;
            }
            int ch1 = decode(string.charAt(i)) << 4;
            int ch2 = decode(string.charAt(i + 1));
            bytes[(i / 2)] = ((byte) (ch1 + ch2));
        }
        return bytes;
    }

    private static int decode(char ch) {
        if ((ch >= '0') && (ch <= '9')) {
            return ch - '0';
        }
        if ((ch >= 'a') && (ch <= 'f')) {
            return ch - 'a' + 10;
        }
        if ((ch >= 'A') && (ch <= 'F')) {
            return ch - 'A' + 10;
        }
        throw new IllegalArgumentException("Illegal hexadecimal character: " + ch);
    }

    public static void restartBluetooth(Context context, final RestartCompletedListener listener) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();

        IntentFilter intentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    L.w("restartBluetooth, status: " + state);
                    if (state == 10) {
                        adapter.enable();
                    } else if (state == 12) {
                        context.unregisterReceiver(this);
                        listener.onRestartCompleted();
                    }
                }
            }
        }, intentFilter);

        adapter.disable();
    }



    public static int unsignedByteToInt(byte value) {
        return value & 0xFF;
    }


    public static Comparator<Beacon> BEACON_MAJOR_COMPARATOR =new Comparator<Beacon>() {
        public int compare(Beacon lhs, Beacon rhs) {
            int compare = Double.compare(rhs.getMajor(), lhs.getMajor());
            if (compare == 0) {
                return Double.compare(rhs.getMinor(), lhs.getMinor());
            }
            return compare;
        }
    };

    public static Comparator<Beacon> BEACON_RSSI_COMPARATOR =new Comparator<Beacon>() {
        public int compare(Beacon lhs, Beacon rhs) {
            return Double.compare(rhs.getRssi(), lhs.getRssi());
        }
    };

    public static int byte2Int(byte[] bytes) {
        if (bytes == null) {
            return 0;
        }
        if (bytes.length == 1) {
            return bytes[0] & 0xFF;
        }
        if (bytes.length == 2) {
            return bytes[1] & 0xFF | (bytes[0] & 0xFF) << 8;
        }
        if (bytes.length == 3) {
            return bytes[2] & 0xFF | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF) << 16;
        }
        if (bytes.length >= 4) {
            return bytes[3] & 0xFF | (bytes[2] & 0xFF) << 8 | (bytes[1] & 0xFF) << 16 | (bytes[0] & 0xFF) << 24;
        }
        return 0;
    }

    public  interface RestartCompletedListener {
       void onRestartCompleted();
    }
    public static String scanRecordtoString(byte[] scanRecord) {
        StringBuffer sb = new StringBuffer();
        byte[] arrayOfByte = scanRecord;
        int j = scanRecord.length;
        for (int i = 0; i < j; i++) {
            byte b = arrayOfByte[i];
            sb.append(String.format("%02X", new Object[]{Byte.valueOf(b)}));
        }
        return sb.toString();
    }
}
