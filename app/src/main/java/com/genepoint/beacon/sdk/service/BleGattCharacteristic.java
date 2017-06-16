 package com.genepoint.beacon.sdk.service;

 import android.annotation.SuppressLint;

 import java.util.UUID;

 @SuppressLint({"NewApi"})
 public class BleGattCharacteristic
 {
   public static final int PROPERTY_READ = 2;
   public static final int PROPERTY_WRITE = 8;
   public static final int PROPERTY_NOTIFY = 16;
   public static final int PROPERTY_INDICATE = 32;
   public static final int FORMAT_UINT8 = 17;
   public static final int FORMAT_UINT16 = 18;
   public static final int FORMAT_UINT24 = 19;
   public static final int FORMAT_UINT32 = 20;
   public static final int FORMAT_SINT8 = 33;
   public static final int FORMAT_SINT16 = 34;
   public static final int FORMAT_SINT32 = 36;
   public static final int FORMAT_SFLOAT = 50;
   public static final int FORMAT_FLOAT = 52;
   private android.bluetooth.BluetoothGattCharacteristic mGattCharacteristicA;
   private com.broadcom.bt.gatt.BluetoothGattCharacteristic mGattCharacteristicB;
   private com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic mGattCharacteristicS;
   private BeaconService.BLESDK mBleSDK;
   private String name;

   public BleGattCharacteristic(android.bluetooth.BluetoothGattCharacteristic c)
   {
     this.mBleSDK = BeaconService.BLESDK.ANDROID;
     setGattCharacteristicA(c);
     initInfo();
   }

   public BleGattCharacteristic(com.broadcom.bt.gatt.BluetoothGattCharacteristic c) {
     this.mBleSDK = BeaconService.BLESDK.BROADCOM;
     setGattCharacteristicB(c);
   }

   public BleGattCharacteristic(com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic c) {
     this.mBleSDK = BeaconService.BLESDK.SAMSUNG;
     setGattCharacteristicS(c);
   }

   private void initInfo() {
     this.name = "Unknown characteristic";
   }

   public UUID getUuid() {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID)
       return getGattCharacteristicA().getUuid();
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM)
       return getGattCharacteristicB().getUuid();
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG) {
       return getGattCharacteristicS().getUuid();
     }

     return null;
   }

   protected android.bluetooth.BluetoothGattCharacteristic getGattCharacteristicA() {
     return this.mGattCharacteristicA;
   }

   public int getProperties() {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID)
       return getGattCharacteristicA().getProperties();
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM)
       return getGattCharacteristicB().getProperties();
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG) {
       return getGattCharacteristicS().getProperties();
     }

     return 0;
   }

   protected com.broadcom.bt.gatt.BluetoothGattCharacteristic getGattCharacteristicB() {
     return this.mGattCharacteristicB;
   }

   protected void setGattCharacteristicB(com.broadcom.bt.gatt.BluetoothGattCharacteristic mBCGattCharacteristic) {
     this.mGattCharacteristicB = mBCGattCharacteristic;
   }

   public String getName() {
     return this.name;
   }

   public void setName(String name) {
     this.name = name;
   }

   public boolean setValue(byte[] val) {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID)
       return getGattCharacteristicA().setValue(val);
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG)
       return this.mGattCharacteristicS.setValue(val);
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       return this.mGattCharacteristicB.setValue(val);
     }

     return false;
   }

   public byte[] getValue() {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID)
       return getGattCharacteristicA().getValue();
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG)
       return this.mGattCharacteristicS.getValue();
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       return this.mGattCharacteristicB.getValue();
     }

     return null;
   }

   public boolean setValue(int value, int formatType, int offset) {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID)
       return getGattCharacteristicA().setValue(value, formatType, offset);
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG)
       return this.mGattCharacteristicS.setValue(value, formatType, offset);
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       return this.mGattCharacteristicB.setValue(value, formatType, offset);
     }

     return false;
   }

   public boolean setValue(int mantissa, int exponent, int formatType, int offset) {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID)
       return getGattCharacteristicA().setValue(mantissa, exponent, formatType, offset);
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG)
       return this.mGattCharacteristicS.setValue(mantissa, exponent, formatType, offset);
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       return this.mGattCharacteristicB.setValue(mantissa, exponent, formatType, offset);
     }

     return false;
   }

   public boolean setValue(String value) {
     return setValue(value.getBytes());
   }

   public String getStringValue(int offset) {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID)
       return getGattCharacteristicA().getStringValue(offset);
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG)
       return this.mGattCharacteristicS.getStringValue(offset);
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       return this.mGattCharacteristicB.getStringValue(offset);
     }

     return null;
   }

   public Float getFloatValue(int formatType, int offset) {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID)
       return getGattCharacteristicA().getFloatValue(formatType, offset);
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG)
       return this.mGattCharacteristicS.getFloatValue(formatType, offset);
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       return this.mGattCharacteristicB.getFloatValue(formatType, offset);
     }

     return null;
   }

   public Integer getIntValue(int formatType, int offset) {
     if (this.mBleSDK == BeaconService.BLESDK.ANDROID) {
       if (formatType == 19) {
         byte[] value = getGattCharacteristicA().getValue();
         return byte2uint24(offset, value);
       }
       return getGattCharacteristicA().getIntValue(formatType, offset);
     }
     if (this.mBleSDK == BeaconService.BLESDK.SAMSUNG) {
       if (formatType == 19) {
         byte[] value = this.mGattCharacteristicS.getValue();
         return byte2uint24(offset, value);
       }
       return this.mGattCharacteristicS.getIntValue(formatType, offset);
     }
     if (this.mBleSDK == BeaconService.BLESDK.BROADCOM) {
       if (formatType == 19) {
         byte[] value = this.mGattCharacteristicB.getValue();
         return byte2uint24(offset, value);
       }
       return this.mGattCharacteristicB.getIntValue(formatType, offset);
     }


     return null;
   }

   private Integer byte2uint24(int offset, byte[] value) {
     if (offset + 3 > value.length)
       return null;
     return Integer.valueOf(value[offset] & 0xFF | (value[(offset + 1)] & 0xFF) << 8 | (value[(offset + 2)] & 0xFF) << 16);
   }

   protected com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic getGattCharacteristicS() {
     return this.mGattCharacteristicS;
   }

   protected void setGattCharacteristicS(com.samsung.android.sdk.bt.gatt.BluetoothGattCharacteristic mSSGattCharacteristic) {
     this.mGattCharacteristicS = mSSGattCharacteristic;
   }

   protected void setGattCharacteristicA(android.bluetooth.BluetoothGattCharacteristic mGattCharacteristicA) {
     this.mGattCharacteristicA = mGattCharacteristicA;
   }
 }

