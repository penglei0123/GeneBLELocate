package com.genepoint.beacon.sdk.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.genepoint.beacon.interfaces.IBle;
import com.genepoint.beacon.interfaces.IBleRequestHandler;
import com.genepoint.beacon.utils.L;
import com.genepoint.blelocate.LogDebug;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class BeaconService extends Service {
    public static final UUID DESC_CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static enum BLESDK {
        NOT_SUPPORTED, ANDROID, SAMSUNG, BROADCOM;
    }

    private final IBinder mBinder = new LocalBinder();
    private BLESDK mBleSDK;
    private IBle mBle;
    private Queue<BleRequest> mRequestQueue = new LinkedList();
    private BleRequest mCurrentRequest = null;

    private static final int REQUEST_TIMEOUT = 300;

    private boolean mCheckTimeout = false;
    private int mElapsed = 0;

    private Thread mRequestTimeout;
    private String mNotificationAddress;

    private Runnable mTimeoutRunnable = new Runnable() {
        public void run() {
            L.d("timeout thread start");
            mElapsed = 0;
            try {
                while (mCheckTimeout) {
                    Thread.sleep(100L);
                    mElapsed += 1;
                    if ((mElapsed > 300) && (mCurrentRequest != null) && (mCurrentRequest.getRequestType() != null)) {
                        L.d("-processrequest type " + mCurrentRequest.getRequestType() + " address " + mCurrentRequest.address + " [timeout]");
                        bleRequestFailed(mCurrentRequest.address, mCurrentRequest.getRequestType(), BleRequest.FailReason.TIMEOUT);
                        bleStatusAbnormal("-processrequest type " + mCurrentRequest.getRequestType() + " address " + mCurrentRequest.address + " [timeout]");


                        new Thread(new Runnable() {
                            public void run() {
                                mCurrentRequest = null;
                                processNextRequest();
                            }
                        }, "th-ble").start();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                L.d("timeout thread exception");
            }
            L.d("timeout thread stop");
        }
    };

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.powerlbs.blesdk.not_supported");//1283082620
        intentFilter.addAction("com.powerlbs.blesdk.no_bt_adapter");//1791014106
        intentFilter.addAction("com.powerlbs.blesdk.status_abnormal");//-885411153
        intentFilter.addAction("com.powerlbs.blesdk.request_failed");
        intentFilter.addAction("com.powerlbs.blesdk.device_found");//6698463
        intentFilter.addAction("com.powerlbs.blesdk.gatt_connected");
        intentFilter.addAction("com.powerlbs.blesdk.gatt_disconnected");
        intentFilter.addAction("com.powerlbs.blesdk.service_discovered");
        intentFilter.addAction("com.powerlbs.blesdk.characteristic_read");
        intentFilter.addAction("com.powerlbs.blesdk.characteristic_notification");
        intentFilter.addAction("com.powerlbs.blesdk.characteristic_write");
        intentFilter.addAction("com.powerlbs.blesdk.characteristic_changed");
        return intentFilter;
    }


    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public BeaconService getService() {
            return BeaconService.this;
        }
    }


    public void onCreate() {
        this.mBleSDK = getBleSDK();
        if (this.mBleSDK == BLESDK.NOT_SUPPORTED) {
            return;
        }

        L.d(" " + this.mBleSDK);
        if (this.mBleSDK == BLESDK.BROADCOM) {
            this.mBle = new BroadcomBle(this);
        } else if (this.mBleSDK == BLESDK.ANDROID) {
            this.mBle = new AndroidBle(this);
        } else if (this.mBleSDK == BLESDK.SAMSUNG) {
            this.mBle = new SamsungBle(this);
        }
    }

    protected void bleNotSupported() {
        Intent intent = new Intent("com.powerlbs.blesdk.not_supported");
        sendBroadcast(intent);
    }

    protected void bleNoBtAdapter() {
        Intent intent = new Intent("com.powerlbs.blesdk.no_bt_adapter");
        sendBroadcast(intent);
    }

    private BLESDK getBleSDK() {
        if (getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            // android 4.3
            return BLESDK.ANDROID;
        }
        ArrayList<String> libraries = new ArrayList<String>();
        for (String i : getPackageManager().getSystemSharedLibraryNames()) {
            libraries.add(i);
        }

        if (android.os.Build.VERSION.SDK_INT >= 17) {
            // android 4.2.2
            if (libraries.contains("com.samsung.android.sdk.bt")) {
                return BLESDK.SAMSUNG;
            } else if (libraries.contains("com.broadcom.bt")) {
                return BLESDK.BROADCOM;
            }
        }
        bleNotSupported();
        return BLESDK.NOT_SUPPORTED;

    }

    public IBle getBle() {
        return this.mBle;
    }

    protected void bleDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord, int source) {
    //    LogDebug.d("BeaconService","[" + new Date().toLocaleString() + "] device found " + device.getAddress());
        Intent intent = new Intent("com.powerlbs.blesdk.device_found");
        intent.putExtra("DEVICE", device);
        intent.putExtra("RSSI", rssi);
        intent.putExtra("SCAN_RECORD", scanRecord);
        intent.putExtra("SOURCE", source);
        sendBroadcast(intent);
    }

    protected void bleGattConnected(BluetoothDevice device) {
        Intent intent = new Intent("com.powerlbs.blesdk.gatt_connected");
        intent.putExtra("DEVICE", device);
        intent.putExtra("ADDRESS", device.getAddress());
        sendBroadcast(intent);
        requestProcessed(device.getAddress(), BleRequest.RequestType.CONNECT_GATT, true);
    }

    protected void bleGattDisConnected(String address, int status) {
        Intent intent = new Intent("com.powerlbs.blesdk.gatt_disconnected");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("STATUS", status);
        sendBroadcast(intent);
        requestProcessed(address, BleRequest.RequestType.CONNECT_GATT, false);
    }

    protected void bleServiceDiscovered(String address, int status, boolean success) {
        Intent intent = new Intent("com.powerlbs.blesdk.service_discovered");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("VALUE", success);
        intent.putExtra("STATUS", status);
        sendBroadcast(intent);
        requestProcessed(address, BleRequest.RequestType.DISCOVER_SERVICE, success);
    }

    protected void requestProcessed(String address, BleRequest.RequestType requestType, boolean success) {
        if ((this.mCurrentRequest != null) && (this.mCurrentRequest.getRequestType() == requestType)) {
            clearTimeoutThread();


            if (!success) {
                bleRequestFailed(this.mCurrentRequest.address, this.mCurrentRequest.getRequestType(), BleRequest.FailReason.RESULT_FAILED);
            }

            new Thread(new Runnable() {
                public void run() {
                    mCurrentRequest = null;
                    processNextRequest();
                }
            }, "th-ble").start();
        }
    }

    void clearTimeoutThread() {
        if ((this.mRequestTimeout != null) && (this.mRequestTimeout.isAlive())) {
            try {
                this.mCheckTimeout = false;
                this.mRequestTimeout.join();
                this.mRequestTimeout = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void bleCharacteristicRead(String address, String uuid, int status, byte[] value) {
        Intent intent = new Intent("com.powerlbs.blesdk.characteristic_read");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("UUID", uuid);
        intent.putExtra("STATUS", status);
        intent.putExtra("VALUE", value);
        sendBroadcast(intent);
        requestProcessed(address, BleRequest.RequestType.READ_CHARACTERISTIC, true);
    }

    protected void addBleRequest(BleRequest request) {
        synchronized (this.mRequestQueue) {
            this.mRequestQueue.add(request);
            processNextRequest();
        }
    }

    private void processNextRequest() {
        if (this.mCurrentRequest != null) {
            return;
        }

        synchronized (this.mRequestQueue) {
            if (this.mRequestQueue.isEmpty()) {
                return;
            }
            this.mCurrentRequest = ((BleRequest) this.mRequestQueue.remove());
        }
        if (this.mCurrentRequest == null) {
            return;
        }
        if (this.mCurrentRequest.getRequestType() == null) {
            return;
        }
        L.d("+processrequest type " + this.mCurrentRequest.getRequestType() + " address " + this.mCurrentRequest.address + " remark " + this.mCurrentRequest.remark);
        boolean ret = false;
        try {
            switch (this.mCurrentRequest.getRequestType()) {
                case CHARACTERISTIC_INDICATION:
                    ret = ((IBleRequestHandler) this.mBle).connect(this.mCurrentRequest.address);
                    break;
                case CHARACTERISTIC_NOTIFICATION:
                    ret = this.mBle.discoverServices(this.mCurrentRequest.address);
                    break;
                case CHARACTERISTIC_STOP_NOTIFICATION:
                case CONNECT_GATT:
                case WRITE_DESCRIPTOR:
                    ret = ((IBleRequestHandler) this.mBle).characteristicNotification(this.mCurrentRequest.address, this.mCurrentRequest.characteristic);
                    break;
                case DISCOVER_SERVICE:
                    ret = ((IBleRequestHandler) this.mBle).readCharacteristic(this.mCurrentRequest.address, this.mCurrentRequest.characteristic);
                    break;
                case READ_RSSI:
                    ret = ((IBleRequestHandler) this.mBle).writeCharacteristic(this.mCurrentRequest.address, this.mCurrentRequest.characteristic);
                    break;
                case READ_CHARACTERISTIC:

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ret) {
            startTimeoutThread();
        } else {
            try {
                L.d("-processrequest type " + this.mCurrentRequest.getRequestType() + " address " + this.mCurrentRequest.address + " [fail start]");
                bleRequestFailed(this.mCurrentRequest.address, this.mCurrentRequest.getRequestType(), BleRequest.FailReason.START_FAILED);
            } catch (Exception localException1) {
            }

            new Thread(new Runnable() {
                public void run() {
                    mCurrentRequest = null;
                    processNextRequest();
                }
            }, "th-ble").start();
        }
    }

    private void startTimeoutThread() {
        clearTimeoutThread();
        this.mCheckTimeout = true;
        this.mRequestTimeout = new Thread(this.mTimeoutRunnable);
        this.mRequestTimeout.start();
    }

    protected BleRequest getCurrentRequest() {
        return this.mCurrentRequest;
    }

    protected void setCurrentRequest(BleRequest mCurrentRequest) {
        this.mCurrentRequest = mCurrentRequest;
    }

    protected void bleCharacteristicNotification(String address, String uuid, boolean isEnabled, int status) {
        Intent intent = new Intent("com.powerlbs.blesdk.characteristic_notification");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("UUID", uuid);
        intent.putExtra("VALUE", isEnabled);
        intent.putExtra("STATUS", status);
        sendBroadcast(intent);
        if (isEnabled) {
            requestProcessed(address, BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, true);
        } else {
            requestProcessed(address, BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION, true);
        }
        setNotificationAddress(address);
    }

    protected void bleCharacteristicIndication(String address, String uuid, int status) {
        Intent intent = new Intent("com.powerlbs.blesdk.characteristic_indication");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("UUID", uuid);
        intent.putExtra("STATUS", status);
        sendBroadcast(intent);
        requestProcessed(address, BleRequest.RequestType.CHARACTERISTIC_INDICATION, true);
        setNotificationAddress(address);
    }

    protected void bleCharacteristicWrite(String address, String uuid, byte[] value, int status) {
        Intent intent = new Intent("com.powerlbs.blesdk.characteristic_write");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("UUID", uuid);
        intent.putExtra("VALUE", value);
        intent.putExtra("STATUS", status);
        sendBroadcast(intent);
        requestProcessed(address, BleRequest.RequestType.WRITE_CHARACTERISTIC, true);
    }

    protected void bleCharacteristicChanged(String address, String uuid, byte[] value) {
        Intent intent = new Intent("com.powerlbs.blesdk.characteristic_changed");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("UUID", uuid);
        intent.putExtra("VALUE", value);
        sendBroadcast(intent);
    }

    protected void bleStatusAbnormal(String reason) {
        Intent intent = new Intent("com.powerlbs.blesdk.status_abnormal");
        intent.putExtra("VALUE", reason);
        sendBroadcast(intent);
    }

    protected void bleRequestFailed(String address, BleRequest.RequestType type, BleRequest.FailReason reason) {
        Intent intent = new Intent("com.powerlbs.blesdk.request_failed");
        intent.putExtra("ADDRESS", address);
        intent.putExtra("REQUEST", type.ordinal());
        intent.putExtra("REASON", reason.ordinal());
        sendBroadcast(intent);
    }

    protected String getNotificationAddress() {
        return this.mNotificationAddress;
    }

    protected void setNotificationAddress(String mNotificationAddress) {
        this.mNotificationAddress = mNotificationAddress;
    }
}

