 package com.genepoint.beacon.sdk.service;

 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothProfile;
 import android.util.Log;

 import com.genepoint.beacon.interfaces.IBle;
 import com.genepoint.beacon.interfaces.IBleRequestHandler;
 import com.samsung.android.sdk.bt.gatt.BluetoothGatt;
 import com.samsung.android.sdk.bt.gatt.BluetoothGattAdapter;
 import com.samsung.android.sdk.bt.gatt.BluetoothGattCallback;
 import com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic;
 import com.samsung.android.sdk.bt.gatt.BluetoothGattDescriptor;
 import com.samsung.android.sdk.bt.gatt.BluetoothGattService;

 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.UUID;

 public class SamsungBle implements IBle, IBleRequestHandler
 {
   protected static final String TAG = "blelib";
   private BluetoothAdapter mBtAdapter;
   private BeaconService mService;
   private BluetoothGatt mBluetoothGatt;
   private boolean mScanning;
   private final BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback()
   {
     public void onAppRegistered(int status) {}


     public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord)
     {
       SamsungBle.this.mService.bleDeviceFound(device, rssi, scanRecord, 0);
     }

     public void onConnectionStateChange(BluetoothDevice device, int status, int newState)
     {
       if (SamsungBle.this.mBluetoothGatt == null) {
         return;
       }

       if (status != 0) {
         SamsungBle.this.disconnect(device.getAddress());
         SamsungBle.this.mService.bleGattDisConnected(device.getAddress(), status);
         return;
       }

       if (newState == 2) {
         SamsungBle.this.mService.bleGattConnected(device);
         SamsungBle.this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.DISCOVER_SERVICE, device.getAddress()));
       } else if (newState == 0) {
         SamsungBle.this.mService.bleGattDisConnected(device.getAddress(), status);
         SamsungBle.this.disconnect(device.getAddress());
       }
     }

     public void onServicesDiscovered(BluetoothDevice device, int status)
     {
       String address = device.getAddress();
       if (status != 0) {
         SamsungBle.this.disconnect(address);
         SamsungBle.this.mService.bleGattDisConnected(address, status);
         SamsungBle.this.mService.requestProcessed(address, BleRequest.RequestType.DISCOVER_SERVICE, false);
         return;
       }
       SamsungBle.this.mService.bleServiceDiscovered(device.getAddress(), status, true);
     }

     public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status)
     {
       BleRequest request = SamsungBle.this.mService.getCurrentRequest();
       String address = request.address;
       if (status != 0) {
         SamsungBle.this.mService.requestProcessed(address, BleRequest.RequestType.READ_CHARACTERISTIC, false);
         return;
       }
       SamsungBle.this.mService.bleCharacteristicRead(address, characteristic.getUuid().toString(), status, characteristic.getValue());
     }

     public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic)
     {
       Log.d("blelib", "onCharacteristicChanged");
       String address = SamsungBle.this.mService.getNotificationAddress();
       SamsungBle.this.mService.bleCharacteristicChanged(address, characteristic.getUuid().toString(), characteristic.getValue());
     }

     public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status)
     {
       BleRequest request = SamsungBle.this.mService.getCurrentRequest();
       String address = request.address;
       if (status != 0) {
         SamsungBle.this.mService.requestProcessed(address, BleRequest.RequestType.WRITE_CHARACTERISTIC, false);
         return;
       }
       SamsungBle.this.mService.bleCharacteristicWrite(address, characteristic.getUuid().toString(), characteristic.getValue(), status);
     }

     public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status)
     {
       BleRequest request = SamsungBle.this.mService.getCurrentRequest();
       String address = request.address;
       byte[] value = descriptor.getValue();
       byte[] val_set = null;
       if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
         val_set = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
       } else if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
         val_set = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
       } else {
         val_set = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
       }

       if (Arrays.equals(value, val_set)) {
         if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
           SamsungBle.this.mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), true, status);
         } else if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
           SamsungBle.this.mService.bleCharacteristicIndication(address, descriptor.getCharacteristic().getUuid().toString(), status);
         } else {
           SamsungBle.this.mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), false, status);
         }
         return;
       }

       if (!descriptor.setValue(val_set)) {
         SamsungBle.this.mService.requestProcessed(address, request.getRequestType(), false);
       }

       SamsungBle.this.mBluetoothGatt.writeDescriptor(descriptor);
     }

     public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status)
     {
       BleRequest request = SamsungBle.this.mService.getCurrentRequest();
       String address = request.address;
       if ((request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) || (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) || (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION)) {
         if (status != 0) {
           SamsungBle.this.mService.requestProcessed(address, request.getRequestType(), false);
           return;
         }

         if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
           SamsungBle.this.mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), true, status);
         } else if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
           SamsungBle.this.mService.bleCharacteristicIndication(address, descriptor.getCharacteristic().getUuid().toString(), status);
         } else {
           SamsungBle.this.mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), false, status);
         }
         return;
       }
     }
   };


   private final BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener()
   {
     public void onServiceConnected(int profile, BluetoothProfile proxy) {
       SamsungBle.this.mBluetoothGatt = ((BluetoothGatt)proxy);
       SamsungBle.this.mBluetoothGatt.registerApp(SamsungBle.this.mGattCallbacks);
     }

     public void onServiceDisconnected(int profile)
     {
       SamsungBle.this.mBluetoothGatt = null;
     }
   };

   public SamsungBle(BeaconService service) {
     this.mService = service;
     this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
     if (this.mBtAdapter == null) {
       this.mService.bleNoBtAdapter();
       return;
     }
     BluetoothGattAdapter.getProfileProxy(this.mService, this.mProfileServiceListener, 7);
   }

   public void startScan()
   {
     if (this.mScanning) {
       return;
     }

     if (this.mBluetoothGatt == null) {
       this.mScanning = false;
       return;
     }

     this.mScanning = true;
     this.mBluetoothGatt.startScan();
   }

   public void stopScan()
   {
     if ((!this.mScanning) || (this.mBluetoothGatt == null)) {
       return;
     }

     this.mScanning = false;
     this.mBluetoothGatt.stopScan();
   }

   public boolean adapterEnabled()
   {
     if (this.mBtAdapter != null) {
       return this.mBtAdapter.isEnabled();
     }
     return false;
   }

   public boolean connect(String address)
   {
     BluetoothDevice device = this.mBtAdapter.getRemoteDevice(address);
     return this.mBluetoothGatt.connect(device, false);
   }

   public void disconnect(String address)
   {
     BluetoothDevice device = this.mBtAdapter.getRemoteDevice(address);
     this.mBluetoothGatt.cancelConnection(device);
   }

   public ArrayList<BleGattService> getServices(String address)
   {
     ArrayList<BleGattService> list = new ArrayList();
     BluetoothDevice device = this.mBtAdapter.getRemoteDevice(address);
     List<BluetoothGattService> services = this.mBluetoothGatt.getServices(device);
     for (BluetoothGattService s : services) {
       list.add(new BleGattService(s));
     }
     return list;
   }

   public boolean requestReadCharacteristic(String address, BleGattCharacteristic characteristic)
   {
     this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.READ_CHARACTERISTIC, address, characteristic));
     return true;
   }

   public boolean discoverServices(String address)
   {
     return this.mBluetoothGatt.discoverServices(this.mBtAdapter.getRemoteDevice(address));
   }

   public boolean readCharacteristic(String address, BleGattCharacteristic characteristic)
   {
     return this.mBluetoothGatt.readCharacteristic(characteristic.getGattCharacteristicS());
   }

   public BleGattService getService(String address, UUID uuid)
   {
     BluetoothGattService service = this.mBluetoothGatt.getService(this.mBtAdapter.getRemoteDevice(address), uuid);
     if (service == null) {
       return null;
     }
     return new BleGattService(service);
   }


   public boolean requestCharacteristicNotification(String address, BleGattCharacteristic characteristic)
   {
     this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION, address, characteristic));
     return true;
   }

   public boolean characteristicNotification(String address, BleGattCharacteristic characteristic)
   {
     BluetoothGattCharacteristic c = characteristic.getGattCharacteristicS();

     if (!this.mBluetoothGatt.setCharacteristicNotification(c, true)) {
       return false;
     }

     BluetoothGattDescriptor descriptor = c.getDescriptor(BeaconService.DESC_CCC);
     if (descriptor == null) {
       return false;
     }

     return this.mBluetoothGatt.readDescriptor(descriptor);
   }

   public boolean requestWriteCharacteristic(String address, BleGattCharacteristic characteristic, String remark)
   {
     this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.WRITE_CHARACTERISTIC, address, characteristic));
     return true;
   }

   public boolean writeCharacteristic(String address, BleGattCharacteristic characteristic)
   {
     return this.mBluetoothGatt.writeCharacteristic(characteristic.getGattCharacteristicS());
   }

   public boolean requestConnect(String address)
   {
     this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.CONNECT_GATT, address));
     return true;
   }

   public String getBTAdapterMacAddr()
   {
     if (this.mBtAdapter != null) {
       return this.mBtAdapter.getAddress();
     }
     return null;
   }

   public boolean requestIndication(String address, BleGattCharacteristic characteristic)
   {
     this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.CHARACTERISTIC_INDICATION, address, characteristic));
     return true;
   }

   public boolean requestStopNotification(String address, BleGattCharacteristic characteristic)
   {
     this.mService.addBleRequest(new BleRequest(BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION, address, characteristic));
     return true;
   }
 }

