package com.genepoint.beacon.sdk.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.genepoint.beacon.interfaces.BeaconManagerListener;
import com.genepoint.beacon.sdk.bean.Beacon;
import com.genepoint.beacon.sdk.bean.BeaconThrowable;
import com.genepoint.beacon.sdk.bean.RecordUtils;
import com.genepoint.blelocate.LogDebug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ScanService {
    private static final int MSG_UPDATE_SCAN_LIST = 1;
    private static final int MSG_BLUETOOTH_OFF = 2;
    public static final long DEFAULT_EXPIRATION_MILLIS = 10000L;
    public static final long DEFAULT_UPDATE_MILLIS = 1000L;
    private boolean mScanFlag = false;
    private Context mContext;
    private BluetoothAdapter mAdapter = null;
    private BeaconManagerListener mListener = null;
    private int mScanMode = 0;
    private long mExpirationMillis = 10000L;
    private long mUpdateMillis = 1000L;
    private ArrayList<Beacon> beaconsUpdate = new ArrayList();
    private ConcurrentHashMap<String, Beacon> beaconsFoundInScanCycle = new ConcurrentHashMap();
    private boolean mSortRssiBeacons = true;
    private LeScanCallback leScanCallback = new LeScanCallback() {
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            // 得到扫描结果
            LogDebug.i("ScanService", "扫描到的设备, name=" + device.getName() + ",address=" + device.toString());
            Beacon beacon;
            if (ScanService.this.mScanMode == 0) {
                beacon = RecordUtils.beaconFromLeScan(device, rssi, scanRecord);
            } else {
                beacon = RecordUtils.simpleBeaconFromLeScan(device, rssi, scanRecord);
            }

            if (beacon != null) {
                long now = System.currentTimeMillis();
                beacon.setMillsTime(now);
                if (!ScanService.this.beaconsFoundInScanCycle.containsKey(beacon.getMacAddress()) && mListener != null) {
                }
                beaconsFoundInScanCycle.put(beacon.getMacAddress(), beacon);
            }

        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    processBeacon();
                    if (mScanFlag) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(1), mUpdateMillis);
                    } else {
                        mHandler.removeMessages(1);
                    }
                    break;
                case 2:
                    if (mListener != null) {
                        mListener.onError(new BeaconThrowable("", 0));
                    }

                    beaconsFoundInScanCycle.clear();
            }

        }
    };
    private BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
            if (state == 12) {
                if (mScanFlag) {
                    _startLeScan();
                }
            } else if (state == 10) {
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }

        }
    };
    private Comparator<? super Beacon> mRssiComparator = new Comparator<Beacon>() {
        public int compare(Beacon lhs, Beacon rhs) {
            return Double.compare((double) rhs.rssi, (double) lhs.rssi);
        }
    };

    public boolean isSortBeacons() {
        return this.mSortRssiBeacons;
    }

    public void setSortBeacons(boolean sortFlag) {
        this.mSortRssiBeacons = sortFlag;
    }

    public ScanService() {
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public ScanService(Context context) {
        this.mContext = context;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public BluetoothAdapter getAdapter() {
        return this.mAdapter;
    }

    public int getScanMode() {
        return this.mScanMode;
    }

    public void setScanMode(int mScanMode) {
        this.mScanMode = mScanMode;
    }

    public long getExpirationMillis() {
        return this.mExpirationMillis;
    }

    public void setExpirationMillis(long expirationMillis) {
        if (expirationMillis < 0L) {
            expirationMillis = 0L;
        }

        this.mExpirationMillis = expirationMillis;
    }

    public long getUpdateMillis() {
        return this.mUpdateMillis;
    }

    public void setUpdateMillis(long updateMillis) {
        if (updateMillis < 0L) {
            updateMillis = 0L;
        }

        this.mUpdateMillis = updateMillis;
    }

    public void setListener(BeaconManagerListener listener) {
        this.mListener = listener;
    }

    public void start() {
        RecordUtils.clearCacheBeaconExtra();
        if (this.mAdapter != null) {
            if (!this.mScanFlag) {
                this.mScanFlag = true;
                if (this.mContext != null) {
                    this.mContext.registerReceiver(this.bluetoothBroadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
                }

                if (this.mAdapter.isEnabled()) {
                    this.mAdapter.startLeScan(this.leScanCallback);
                }

                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), this.mUpdateMillis);
            }
        }
    }

    private void _startLeScan() {
        if (this.mAdapter != null) {
            this.mAdapter.startLeScan(this.leScanCallback);
            LogDebug.i("ScanService", "mAdapter.startLeScan");
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), this.mUpdateMillis);
        }

    }

    private void _stopLeScan() {
        this.mHandler.removeCallbacksAndMessages((Object) null);
        if (this.mAdapter != null) {
            this.mAdapter.stopLeScan(this.leScanCallback);
        }

        this.beaconsFoundInScanCycle.clear();
    }

    public void stop() {
        if (this.mScanFlag) {
            this.mScanFlag = false;
            this._stopLeScan();
            this.mHandler.removeCallbacksAndMessages((Object) null);
            if (this.mContext != null) {
                this.mContext.unregisterReceiver(this.bluetoothBroadcastReceiver);
            }

        }
    }

    protected void processBeacon() {
        this.beaconsUpdate.clear();
        Set beacons = this.beaconsFoundInScanCycle.entrySet();
        Iterator iterator = beacons.iterator();
        Long now = Long.valueOf(System.currentTimeMillis());

        while (true) {
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                Beacon beacon = (Beacon) entry.getValue();
                Long time = Long.valueOf(beacon.getMillsTime());
                if (this.mExpirationMillis > 0L && now.longValue() - time.longValue() > this.mExpirationMillis) {
                    if (this.mListener != null) {
                    }

                    iterator.remove();
                } else {
                    this.beaconsUpdate.add(beacon);
                }
            }

            if (this.mSortRssiBeacons) {
                Collections.sort(this.beaconsUpdate, this.mRssiComparator);
            }

            if (this.mListener != null) {
                this.mListener.onUpdateBeacon(this.beaconsUpdate);
            }

            return;
        }
    }
}

