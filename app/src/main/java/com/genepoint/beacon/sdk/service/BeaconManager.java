package com.genepoint.beacon.sdk.service;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.genepoint.beacon.interfaces.BeaconManagerListener;
import com.genepoint.beacon.interfaces.IBle;
import com.genepoint.beacon.sdk.bean.Beacon;
import com.genepoint.beacon.sdk.bean.BeaconThrowable;
import com.genepoint.beacon.sdk.bean.BleRegion;
import com.genepoint.beacon.sdk.bean.RecordUtils;
import com.genepoint.beacon.utils.L;
import com.genepoint.blelocate.LogDebug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BeaconManager {
    public static final String tag = "BeaconManager";
    public static final int BLE_NOT_SUPPORTED = -1;
    public static final int BLE_NO_BT_ADAPTER = -2;
    public static final int BLE_STATUS_ABNORMAL = 1;
    public static long EXPIRATION_MILLIS = 8000L;//统计范围时间
    private static long SCAN_TIME = 1000L;//蓝牙扫描结果回调周期
    public static int BLE_SUPPORTED_STATUS = 1;

    private static BeaconManager beaconManager;
    private Context mContext;

    private BeaconService mService;//服务

    private IBle mBle;//蓝牙接口

    private final ServiceConnection mServiceConnection;
    private final ConcurrentHashMap<String, Beacon> beaconsFoundInScanCycle;//蓝牙扫描结果
    public BeaconManagerListener mBeaconManagerListener;//监听器
    private Thread mProcessThread;
    private Handler mHandler;
    private boolean mScanning;
    private BleRegion brtRegion;
    private BroadcastReceiver bluetoothBroadcastReceiver;

    private ScanService mMonitorScanner = new ScanService();
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    /****
     * 单例
     * @param context
     * @return
     */
    public static synchronized BeaconManager getInstance(Context context) {
        if (beaconManager == null) {
            beaconManager = new BeaconManager(context);
        }
        return beaconManager;
    }


    private BeaconManager(Context context) {
        this.mContext = context;
        this.mServiceConnection = new BRTServiceConnection();
        this.beaconsFoundInScanCycle = new ConcurrentHashMap();
        this.mHandler = new Handler();
        this.bluetoothBroadcastReceiver = createBluetoothBroadcastReceiver();
        this.mContext.registerReceiver(this.bluetoothBroadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        this.mMonitorScanner.setScanMode(1);
    }

    private BroadcastReceiver createBluetoothBroadcastReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                if (state == 10) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            L.i("Bluetooth is OFF: stopping scanning");
                            scanLeDevice(false);
                            beaconsFoundInScanCycle.clear();
                            mScanning = false;
                        }
                    });
                } else if (state == 12) {
                    mHandler.post(new Runnable() {
                        public void run() {
                            if (!isStop) {
                                L.i("Bluetooth is ON: start scanning");
                                startRanging();
                            }
                        }
                    });
                }
            }
        };
    }

    private class BRTServiceConnection implements ServiceConnection {
        private BRTServiceConnection() {
        }

        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            Log.d("BeaconManager", "onServiceConnected");
            try {
                BeaconService.LocalBinder binder = (BeaconService.LocalBinder) rawBinder;
                mService = binder.getService();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mService != null) {
                mBle = mService.getBle();
            }
            if ((mBle != null) && (!mBle.adapterEnabled())) {
                if (mBeaconManagerListener != null) {
                    mBeaconManagerListener.onError(new BeaconThrowable("蓝牙未打开，请检查设备蓝牙是否开启", -1));
                }
                L.e("蓝牙未开启");
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            Log.e("BeaconManager", "onServiceDisconnected");
            mService = null;
        }
    }

    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
           // LogDebug.w("BeaconManager","action:"+action);
            int m = -1;
            switch (action.hashCode()) {
                case 1791014106:
                    if (action.equals("com.powerlbs.blesdk.no_bt_adapter")) {
                        m = 2;
                    }
                    break;
                case 1283082620:
                    if (action.equals("com.powerlbs.blesdk.not_supported")) {
                        m = 0;
                    }
                    break;
                case 6698463:
                    if (action.equals("com.powerlbs.blesdk.device_found")) {
                        m = 1;
                    }
            }
            switch (m) {
                case 0:
                    BeaconManager.BLE_SUPPORTED_STATUS = -1;
                    Log.e("BeaconManager", "Ble not support");
                    if (mBeaconManagerListener != null) {
                        mBeaconManagerListener.onError(new BeaconThrowable("该设备不支持BLE", -1));
                    }
                    break;
                case 1:
                    Bundle extras = intent.getExtras();
                    BluetoothDevice device = (BluetoothDevice) extras.getParcelable("DEVICE");
                    Beacon beacon = RecordUtils.beaconFromLeScan(device, extras.getInt("RSSI"), extras.getByteArray("SCAN_RECORD"));
                    if (beacon != null) {
//                       if ((brtRegion != null) && (!RecordUtils.isBeaconInRegion(beacon, brtRegion))) {
//                            return;
//                        }
                        long now = System.currentTimeMillis();
                        beacon.setMillsTime(now);

                        if ((!beaconsFoundInScanCycle.containsKey(beacon.getMacAddress())) &&
                                (mBeaconManagerListener != null)) {
                            beaconsFoundInScanCycle.put(beacon.getMacAddress(), beacon);
                        }


                    }
                    break;
                case 2:
                    BeaconManager.BLE_SUPPORTED_STATUS = -2;
                    Log.e("BeaconManager", "No bluetooth adapter");
                    if (mBeaconManagerListener != null) {
                        mBeaconManagerListener.onError(new BeaconThrowable("蓝牙未打开", -2));
                    }

            }

        }
    };


    private Runnable mProcessRunnable = new Runnable() {
        public void run() {
            Log.d("BeaconManager", "processBeacon thread start");
            try {
                while (mScanning) {
                    Thread.sleep(BeaconManager.SCAN_TIME);
                    processBeacon();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d("BeaconManager", "processBeacon thread exception");
            }
            Log.d("BeaconManager", "processBeacon thread stop");
        }
    };

    private void startProcessThread() {
        clearProcessThread();
        this.mScanning = true;
        //启动消费者工作任务
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this.mProcessRunnable);
        Log.d("BeaconManager","startProcessThread");
//        this.mProcessThread = new Thread();
//        this.mProcessThread.start();
    }

    private void clearProcessThread() {
        Log.d("BeaconManager","clearProcessThread");
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
        this.mScanning = false;
    }


    protected void processBeacon() {
        if (!this.mScanning) {
            return;
        }
        Set<Map.Entry<String, Beacon>> beacons = this.beaconsFoundInScanCycle.entrySet();
        ArrayList<Beacon> beaconsUpdate = new ArrayList();
        Iterator<Map.Entry<String, Beacon>> iterator = beacons.iterator();
        Long now = Long.valueOf(System.currentTimeMillis());

        while (iterator.hasNext()) {
            Map.Entry<String, Beacon> entry = (Map.Entry) iterator.next();
            Beacon beacon = (Beacon) entry.getValue();
            Long time = Long.valueOf(beacon.getMillsTime());

            if (now.longValue() - time.longValue() > 5000L) {
                iterator.remove();
            } else {
                beaconsUpdate.add(beacon);
            }
        }

        Collections.sort(beaconsUpdate, RecordUtils.BEACON_RSSI_COMPARATOR);
        if (this.mBeaconManagerListener != null) {
            this.mBeaconManagerListener.onUpdateBeacon(beaconsUpdate);
        }
    }


    private void scanLeDevice(boolean enable) {
        if (getIBle() == null) {
            Log.e("BeaconManager", "scanLeDevice:BRTBeaconService服务未开启，请先调用startService方法");
            if (this.mBeaconManagerListener != null) {
                this.mBeaconManagerListener.onError(new BeaconThrowable("BRTBeaconService服务未开启，请先调用startService方法", -3));
            }
            return;
        }
        if (enable) {
            this.mScanning = true;
            if (this.mBle != null) {
                startProcessThread();
                this.mBle.startScan();
            }
        } else {
            this.mScanning = false;
            if (this.mBle != null) {
                clearProcessThread();
                this.mBle.stopScan();
                this.beaconsFoundInScanCycle.clear();
            }
        }
    }

    public IBle getIBle() {
        return this.mBle;
    }


    public boolean hasBluetoothle() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le");
    }


    public boolean isBluetoothEnabled() {
        return this.mBle != null ? this.mBle.adapterEnabled() : false;
    }


    public void startService() {
        this.mContext.registerReceiver(this.mBleReceiver, BeaconService.getIntentFilter());
        Intent bindIntent = new Intent(this.mContext, BeaconService.class);
        this.mContext.bindService(bindIntent, this.mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    public void stopService() {
        this.mContext.unregisterReceiver(this.mBleReceiver);
        try {
            this.mContext.unregisterReceiver(this.bluetoothBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopRanging();
        Intent bindIntent = new Intent(this.mContext, BeaconService.class);
        this.mContext.stopService(bindIntent);
    }

    public void setRegion(BleRegion region) {
        this.brtRegion = region;
    }

    public void setRangingTime(long time) {
        SCAN_TIME = time;
    }

    public long getRangingTime() {

        return SCAN_TIME;
    }

    public void setExpirationTime(long time) {
        if (time < 0L) {
            time = 0L;
        }

        EXPIRATION_MILLIS = time;
    }

    public long getExpirationTime() {
        return EXPIRATION_MILLIS;
    }


    public void startRanging() {
        if (getIBle() == null) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (getIBle() == null) {
                        Log.e("BeaconManager", "startRanging:BRTBeaconService服务未开启，请先调用startService方法");
                        if (mBeaconManagerListener != null) {
                            mBeaconManagerListener.onError(new BeaconThrowable("BRTBeaconService服务未开启，请先调用startService方法", -3));
                        }
                    } else if (BeaconManager.BLE_SUPPORTED_STATUS != 1) {
                        Log.e("BeaconManager", "扫描失败，Error：BLE_SUPPORTED_STATUS=" + BeaconManager.BLE_SUPPORTED_STATUS);
                        if (mBeaconManagerListener != null) {
                            mBeaconManagerListener.onError(new BeaconThrowable("扫描异常", -4));
                        }
                    } else {
                        scanLeDevice(true);
                    }
                }

            }, 500L);
        } else {
            if (BLE_SUPPORTED_STATUS != 1) {
                Log.e("BeaconManager", "扫描失败，Error：BLE_SUPPORTED_STATUS=" + BLE_SUPPORTED_STATUS);
                if (this.mBeaconManagerListener != null) {
                    this.mBeaconManagerListener.onError(new BeaconThrowable("扫描异常", -4));
                }
                return;
            }
            scanLeDevice(true);
        }
    }


    boolean isStop = false;


    public void stopRanging() {
        if (getIBle() == null) {
            Log.e("BeaconManager", "BRTBeaconService服务未开启，请先调用startService方法");
            if (this.mBeaconManagerListener != null) {
                this.mBeaconManagerListener.onError(new BeaconThrowable("BRTBeaconService服务未开启，请先调用startService方法", -3));
            }
        } else {
            this.isStop = true;
            scanLeDevice(false);
        }
    }


    public void setBRTBeaconManagerListener(BeaconManagerListener listener) {
        this.mBeaconManagerListener = listener;
    }

}
