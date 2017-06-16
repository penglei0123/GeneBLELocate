 package com.genepoint.beacon.sdk.service;

 import android.annotation.SuppressLint;

 import org.json.JSONException;
 import org.json.JSONObject;

 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;


 @SuppressLint({"NewApi"})
 public class BleGattService
 {
   private BeaconService.BLESDK mBleSDK;
   private com.samsung.android.sdk.bt.gatt.BluetoothGattService mGattServiceS;
   private com.broadcom.bt.gatt.BluetoothGattService mGattServiceB;
   private android.bluetooth.BluetoothGattService mGattServiceA;
   private String mName;

   public BleGattService(com.samsung.android.sdk.bt.gatt.BluetoothGattService s)
   {
     this.mBleSDK = BeaconService.BLESDK.SAMSUNG;
     this.mGattServiceS = s;
     initInfo();
   }

   public BleGattService(com.broadcom.bt.gatt.BluetoothGattService s) {
     this.mBleSDK = BeaconService.BLESDK.BROADCOM;
     this.mGattServiceB = s;
     initInfo();
   }

   public BleGattService(android.bluetooth.BluetoothGattService s) {
     this.mBleSDK = BeaconService.BLESDK.ANDROID;
     this.mGattServiceA = s;
     initInfo();
   }

   private void initInfo() {
     this.mName = "Unknown Service";
   }

   public UUID getUuid() {
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM)
       return this.mGattServiceB.getUuid();
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG)
       return this.mGattServiceS.getUuid();
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID) {
       return this.mGattServiceA.getUuid();
     }

     return null;
   }

   public List<BleGattCharacteristic> getCharacteristics() {
     ArrayList<BleGattCharacteristic> list = new ArrayList();
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       for (com.broadcom.bt.gatt.BluetoothGattCharacteristic c : this.mGattServiceB.getCharacteristics()) {
         list.add(new BleGattCharacteristic(c));
       }
     } else if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG) {
       for (Object o : this.mGattServiceS.getCharacteristics()) {
         com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic c = (com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic)o;
         list.add(new BleGattCharacteristic(c));
       }
     } else if (this.mBleSDK == BeaconService.BLESDK.ANDROID) {
       for (android.bluetooth.BluetoothGattCharacteristic c : this.mGattServiceA.getCharacteristics()) {
         list.add(new BleGattCharacteristic(c));
       }
     }

     return list;
   }

   public BleGattCharacteristic getCharacteristic(UUID uuid) {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID) {
       android.bluetooth.BluetoothGattCharacteristic c = this.mGattServiceA.getCharacteristic(uuid);
       if (c != null) {
         return new BleGattCharacteristic(c);
       }
     } else if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG) {
       com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic c = this.mGattServiceS.getCharacteristic(uuid);
       if (c != null) {
         return new BleGattCharacteristic(c);
       }
     } else if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       com.broadcom.bt.gatt.BluetoothGattCharacteristic c = this.mGattServiceB.getCharacteristic(uuid);
       if (c != null) {
         return new BleGattCharacteristic(c);
       }
     }

     return null;
   }

   public void setInfo(JSONObject info) {
     if (info == null) {
       return;
     }
     try
     {
       setName(info.getString("name"));
     } catch (JSONException e) {
       e.printStackTrace();
     }
   }

   public String getName() {
     return this.mName;
   }

   public void setName(String mName) {
     this.mName = mName;
   }
 }
