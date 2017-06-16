package com.genepoint.beacon.interfaces;

import com.genepoint.beacon.sdk.service.BleGattCharacteristic;
import com.genepoint.beacon.sdk.service.BleGattService;

import java.util.ArrayList;
import java.util.UUID;

public  interface IBle
{
   String getBTAdapterMacAddr();
  
   void startScan();
  
   void stopScan();
  
   boolean adapterEnabled();
  
   void disconnect(String paramString);
  
   boolean discoverServices(String paramString);
  
   ArrayList<BleGattService> getServices(String paramString);
  
   BleGattService getService(String paramString, UUID paramUUID);
  
   boolean requestConnect(String paramString);
  
   boolean requestReadCharacteristic(String paramString, BleGattCharacteristic paramBleGattCharacteristic);
  
   boolean requestCharacteristicNotification(String paramString, BleGattCharacteristic paramBleGattCharacteristic);
  
   boolean requestStopNotification(String paramString, BleGattCharacteristic paramBleGattCharacteristic);
  
   boolean requestIndication(String paramString, BleGattCharacteristic paramBleGattCharacteristic);
  
   boolean requestWriteCharacteristic(String paramString1, BleGattCharacteristic paramBleGattCharacteristic, String paramString2);
}
