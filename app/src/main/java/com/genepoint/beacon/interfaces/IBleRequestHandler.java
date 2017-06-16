package com.genepoint.beacon.interfaces;

import com.genepoint.beacon.sdk.service.BleGattCharacteristic;

public interface IBleRequestHandler
{
    boolean connect(String paramString);
  
    boolean readCharacteristic(String paramString, BleGattCharacteristic paramBleGattCharacteristic);
  
    boolean characteristicNotification(String paramString, BleGattCharacteristic paramBleGattCharacteristic);
  
    boolean writeCharacteristic(String paramString, BleGattCharacteristic paramBleGattCharacteristic);
}