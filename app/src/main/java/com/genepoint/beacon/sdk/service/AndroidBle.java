package com.genepoint.beacon.sdk.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.genepoint.beacon.interfaces.IBle;
import com.genepoint.beacon.interfaces.IBleRequestHandler;
import com.genepoint.beacon.utils.L;
import com.genepoint.blelocate.LogDebug;

import org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressLint({"NewApi"})
public class AndroidBle implements IBle, IBleRequestHandler {
    private BeaconService mService;
    private BluetoothAdapter mBtAdapter;
    private Map<String, BluetoothGatt> mBluetoothGatts;

    private LeScanCallback mLeScanCallback = new LeScanCallback() {
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mService.bleDeviceFound(device, rssi, scanRecord, 0);
        }
    };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String address = gatt.getDevice().getAddress();
            L.d("onConnectionStateChange " + address + " status " + status + " newState " + newState);
            if (status != 0) {
                disconnect(address);
                mService.bleGattDisConnected(address, status);
                return;
            }

            if (newState == 2) {
                mService.bleGattConnected(gatt.getDevice());
                mService.addBleRequest(new BleRequest(BleRequest.RequestType.DISCOVER_SERVICE, address));
            } else if (newState == 0) {
                mService.bleGattDisConnected(address, newState);
                disconnect(address);
            }
        }

        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            String address = gatt.getDevice().getAddress();
            L.d("onServicesDiscovered " + address + " status " + status);
            if (status != 0) {
                mService.bleServiceDiscovered(gatt.getDevice().getAddress(), status, false);
                mService.requestProcessed(address, BleRequest.RequestType.DISCOVER_SERVICE, false);
                return;
            }
            mService.bleServiceDiscovered(gatt.getDevice().getAddress(), status, true);
        }

        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String address = gatt.getDevice().getAddress();
            L.d("onCharacteristicRead " + address + " status " + status);
            if (status != 0) {
                mService.requestProcessed(address, BleRequest.RequestType.READ_CHARACTERISTIC, false);
                mService.bleCharacteristicRead(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), status, characteristic.getValue());
                return;
            }
            Log.d("blelib", new String(Hex.encodeHex(characteristic.getValue())));
            mService.bleCharacteristicRead(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), status, characteristic.getValue());
        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String address = gatt.getDevice().getAddress();
            L.d("onCharacteristicChanged " + address);
            L.d(new String(Hex.encodeHex(characteristic.getValue())));
            mService.bleCharacteristicChanged(address, characteristic.getUuid().toString(), characteristic.getValue());
        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            String address = gatt.getDevice().getAddress();
            L.d("onCharacteristicWrite " + address + " status " + status);
            if (status != 0) {
                mService.requestProcessed(address, BleRequest.RequestType.WRITE_CHARACTERISTIC, false);
                Log.d("blelib", new String(Hex.encodeHex(characteristic.getValue())));
                mService.bleCharacteristicWrite(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), characteristic.getValue(), status);
                return;
            }
            mService.bleCharacteristicWrite(gatt.getDevice().getAddress(), characteristic.getUuid().toString(), characteristic.getValue(), status);
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            String address = gatt.getDevice().getAddress();
            L.d("onDescriptorWrite " + address + " status " + status);
            BleRequest request = mService.getCurrentRequest();
            if ((request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) || (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) || (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION)) {
                if (status != 0) {
                    mService.requestProcessed(address, BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, false);
                    return;
                }
                if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
                    mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), true, status);
                } else if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
                    mService.bleCharacteristicIndication(address, descriptor.getCharacteristic().getUuid().toString(), status);
                } else {
                    mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), false, status);
                }
                return;
            }
        }
    };

    public AndroidBle(BeaconService service) {
        this.mService = service;

        if (!this.mService.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            this.mService.bleNotSupported();
            return;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) this.mService.getSystemService(Context.BLUETOOTH_SERVICE);

        this.mBtAdapter = bluetoothManager.getAdapter();
        if (this.mBtAdapter == null) {
            this.mService.bleNoBtAdapter();
        }
        this.mBluetoothGatts = new HashMap();
    }

    public void startScan() {
        if (null != this.mLeScanCallback) {
            this.mBtAdapter.startLeScan(this.mLeScanCallback);
        } else {
            BluetoothManager bluetoothManager = (BluetoothManager) this.mService.getSystemService(Context.BLUETOOTH_SERVICE);
            this.mBtAdapter = bluetoothManager.getAdapter();
        }
    }

    public void stopScan() {
        if (null != this.mLeScanCallback) {
            this.mBtAdapter.stopLeScan(this.mLeScanCallback);
        } else {
            BluetoothManager bluetoothManager = (BluetoothManager) this.mService.getSystemService(Context.BLUETOOTH_SERVICE);
            this.mBtAdapter = bluetoothManager.getAdapter();
        }
        this.mBtAdapter.stopLeScan(this.mLeScanCallback);
    }

    public boolean adapterEnabled() {
        if (this.mBtAdapter != null) {
            return this.mBtAdapter.isEnabled();
        }
        return false;
    }

    public boolean connect(String address) {
        BluetoothDevice device = this.mBtAdapter.getRemoteDevice(address);
        BluetoothGatt gatt = device.connectGatt(this.mService, false, this.mGattCallback);
        if (gatt == null) {
            this.mBluetoothGatts.remove(address);
            return false;
        }

        this.mBluetoothGatts.put(address, gatt);
        return true;
    }


    public void disconnect(String address) {
        if (this.mBluetoothGatts.containsKey(address)) {
            BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.remove(address);
            this.mService.clearTimeoutThread();
            if (gatt != null) {
                gatt.disconnect();
                gatt.close();
            }
        }
    }

    public ArrayList<BleGattService> getServices(String address) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if (gatt == null) {
            return null;
        }

        ArrayList<BleGattService> list = new ArrayList();
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService s : services) {
            BleGattService service = new BleGattService(s);

            list.add(service);
        }
        return list;
    }

    public boolean requestReadCharacteristic(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if ((gatt == null) || (characteristic == null)) {
            return false;
        }

        this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.READ_CHARACTERISTIC, gatt.getDevice().getAddress(), characteristic));
        return true;
    }

    public boolean readCharacteristic(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if (gatt == null || characteristic == null) {
            return false;
        }
        return gatt.readCharacteristic(characteristic.getGattCharacteristicA());
    }

    public boolean discoverServices(String address) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if (gatt == null) {
            return false;
        }

        boolean ret = gatt.discoverServices();
        if (!ret) {
            disconnect(address);
        }
        return ret;
    }

    public BleGattService getService(String address, UUID uuid) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if (gatt == null) {
            return null;
        }

        BluetoothGattService service = gatt.getService(uuid);
        if (service == null) {
            return null;
        }
        return new BleGattService(service);
    }


    public boolean requestCharacteristicNotification(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if ((gatt == null) || (characteristic == null)) {
            return false;
        }

        this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, gatt.getDevice().getAddress(), characteristic));
        return true;
    }

    public boolean characteristicNotification(String address, BleGattCharacteristic characteristic) {
        BleRequest request = this.mService.getCurrentRequest();
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if ((gatt == null) || (characteristic == null)) {
            return false;
        }

        boolean enable = true;
        if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION) {
            enable = false;
        }
        BluetoothGattCharacteristic c = characteristic.getGattCharacteristicA();
        if (!gatt.setCharacteristicNotification(c, enable)) {
            return false;
        }

        BluetoothGattDescriptor descriptor = c.getDescriptor(BeaconService.DESC_CCC);
        if (descriptor == null) {
            return false;
        }

        byte[] val_set = null;
        if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
            val_set = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
            val_set = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        } else {
            val_set = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }
        if (!descriptor.setValue(val_set)) {
            return false;
        }

        return gatt.writeDescriptor(descriptor);
    }

    public boolean requestWriteCharacteristic(String address, BleGattCharacteristic characteristic, String remark) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if ((gatt == null) || (characteristic == null)) {
            return false;
        }

        this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.WRITE_CHARACTERISTIC, gatt.getDevice().getAddress(), characteristic, remark));
        return true;
    }

    public boolean writeCharacteristic(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if (gatt == null) {
            return false;
        }

        Log.d("blelib", new String(Hex.encodeHex(characteristic.getGattCharacteristicA().getValue())));
        gatt.writeCharacteristic(characteristic.getGattCharacteristicA());
        return true;
    }

    public boolean requestConnect(String address) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if ((gatt != null) && (gatt.getServices().size() == 0)) {
            return false;
        }

        this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.CONNECT_GATT, address));
        return true;
    }

    public String getBTAdapterMacAddr() {
        if (this.mBtAdapter != null) {
            return this.mBtAdapter.getAddress();
        }
        return null;
    }

    public boolean requestIndication(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if ((gatt == null) || (characteristic == null)) {
            return false;
        }

        this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.CHARACTERISTIC_INDICATION, gatt.getDevice().getAddress(), characteristic));
        return true;
    }

    public boolean requestStopNotification(String address, BleGattCharacteristic characteristic) {
        BluetoothGatt gatt = (BluetoothGatt) this.mBluetoothGatts.get(address);
        if ((gatt == null) || (characteristic == null)) {
            return false;
        }

        this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, gatt.getDevice().getAddress(), characteristic));
        return true;
    }
}
