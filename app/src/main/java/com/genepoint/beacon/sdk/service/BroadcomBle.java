 package com.genepoint.beacon.sdk.service;

 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothProfile;
 import android.bluetooth.BluetoothProfile.ServiceListener;

 import com.broadcom.bt.gatt.BluetoothGatt;
 import com.broadcom.bt.gatt.BluetoothGattAdapter;
 import com.broadcom.bt.gatt.BluetoothGattCallback;
 import com.broadcom.bt.gatt.BluetoothGattCharacteristic;
 import com.broadcom.bt.gatt.BluetoothGattDescriptor;
 import com.broadcom.bt.gatt.BluetoothGattService;
 import com.genepoint.beacon.interfaces.IBle;
 import com.genepoint.beacon.interfaces.IBleRequestHandler;

 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.UUID;


 public class BroadcomBle implements IBle, IBleRequestHandler
 {
   private BluetoothAdapter mBtAdapter;
   private BeaconService mService;
   private BluetoothGatt mBluetoothGatt;
   private boolean mScanning;
   private String mAddress;
   private final BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback()
   {
     public void onAppRegistered(int status) {}


     public void onScanResult(BluetoothDevice device, int rssi, byte[] scanRecord)
     {
       BroadcomBle.this.mService.bleDeviceFound(device, rssi, scanRecord, 0);
     }

     public void onConnectionStateChange(BluetoothDevice device, int status, int newState)
     {
       if (BroadcomBle.this.mBluetoothGatt == null) {
         return;
       }

       if (newState == 2) {
         BroadcomBle.this.mService.bleGattConnected(device);
         BroadcomBle.this.mBluetoothGatt.discoverServices(device);
         BroadcomBle.this.mAddress = device.getAddress();
       } else if (newState == 0) {
         BroadcomBle.this.mService.bleGattDisConnected(device.getAddress(), status);
         BroadcomBle.this.mAddress = null;
       }
     }

     public void onServicesDiscovered(BluetoothDevice device, int status)
     {
       BroadcomBle.this.mService.bleServiceDiscovered(device.getAddress(), status, true);
     }

     public void onCharacteristicRead(BluetoothGattCharacteristic characteristic, int status)
     {
       if (status == 0) {
         BroadcomBle.this.mService.bleCharacteristicRead(BroadcomBle.this.mAddress, characteristic.getUuid().toString(), status, characteristic.getValue());
       }
     }

     public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic)
     {
       String address = BroadcomBle.this.mService.getNotificationAddress();
       BroadcomBle.this.mService.bleCharacteristicChanged(address, characteristic.getUuid().toString(), characteristic.getValue());
     }

     public void onDescriptorRead(BluetoothGattDescriptor descriptor, int status)
     {
       BleRequest request = BroadcomBle.this.mService.getCurrentRequest();
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
           BroadcomBle.this.mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), true, status);
         } else if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
           BroadcomBle.this.mService.bleCharacteristicIndication(address, descriptor.getCharacteristic().getUuid().toString(), status);
         } else {
           BroadcomBle.this.mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), false, status);
         }
         return;
       }

       if (!descriptor.setValue(val_set)) {
         BroadcomBle.this.mService.requestProcessed(address, request.getRequestType(), false);
       }

       BroadcomBle.this.mBluetoothGatt.writeDescriptor(descriptor);
     }

     public void onDescriptorWrite(BluetoothGattDescriptor descriptor, int status)
     {
       BleRequest request = BroadcomBle.this.mService.getCurrentRequest();
       String address = request.address;
       if ((request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) || (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) || (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION)) {
         if (status != 0) {
           BroadcomBle.this.mService.requestProcessed(address, request.getRequestType(), false);
           return;
         }

         if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_NOTIFICATION) {
           BroadcomBle.this.mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), true, status);
         } else if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_INDICATION) {
           BroadcomBle.this.mService.bleCharacteristicIndication(address, descriptor.getCharacteristic().getUuid().toString(), status);
         } else {
           BroadcomBle.this.mService.bleCharacteristicNotification(address, descriptor.getCharacteristic().getUuid().toString(), false, status);
         }
         return;
       }
     }
   };

   private final ServiceListener mProfileServiceListener = new ServiceListener()
   {
     public void onServiceConnected(int profile, BluetoothProfile proxy) {
       BroadcomBle.this.mBluetoothGatt = ((BluetoothGatt)proxy);
       BroadcomBle.this.mBluetoothGatt.registerApp(BroadcomBle.this.mGattCallbacks);
     }

     public void onServiceDisconnected(int profile)
     {
       for (BluetoothDevice d : BroadcomBle.this.mBluetoothGatt.getConnectedDevices()) {
         BroadcomBle.this.mBluetoothGatt.cancelConnection(d);
       }
       BroadcomBle.this.mBluetoothGatt = null;
     }
   };

   public BroadcomBle(BeaconService service) {
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
     return true;
   }

   public boolean readCharacteristic(String address, BleGattCharacteristic characteristic)
   {
     if (characteristic.getGattCharacteristicB() != null) {
       return this.mBluetoothGatt.readCharacteristic(characteristic.getGattCharacteristicB());
     }
     return false;
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
     BleRequest request = this.mService.getCurrentRequest();
     BluetoothGattCharacteristic b = characteristic.getGattCharacteristicB();

     boolean enable = true;
     if (request.getRequestType() == BleRequest.RequestType.CHARACTERISTIC_STOP_NOTIFICATION) {
       enable = false;
     }
     if (!this.mBluetoothGatt.setCharacteristicNotification(b, enable)) {
       return false;
     }

     BluetoothGattDescriptor descriptor = b.getDescriptor(BeaconService.DESC_CCC);
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
     return this.mBluetoothGatt.writeCharacteristic(characteristic.getGattCharacteristicB());
   }

   public boolean requestConnect(String address)
   {
     if (this.mAddress != null) {
       return false;
     }
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
