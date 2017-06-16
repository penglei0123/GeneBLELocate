 package com.genepoint.beacon.sdk.service;

 public class BleRequest {

   public static enum RequestType {
     CONNECT_GATT,  DISCOVER_SERVICE,  CHARACTERISTIC_NOTIFICATION,  CHARACTERISTIC_INDICATION,  READ_CHARACTERISTIC,  READ_DESCRIPTOR,  READ_RSSI,  WRITE_CHARACTERISTIC,  WRITE_DESCRIPTOR,  CHARACTERISTIC_STOP_NOTIFICATION;
   }

   public static enum FailReason {
     START_FAILED,  TIMEOUT,  RESULT_FAILED;
   }

   private RequestType type ;
   public String address;
   public BleGattCharacteristic characteristic;
   public String remark;

   public BleRequest(RequestType type, String address) {
       this.type = RequestType.CONNECT_GATT;
     this.type = type;
     this.address = address;
   }

   public BleRequest(RequestType type, String address, BleGattCharacteristic characteristic) {
       this.type = RequestType.CONNECT_GATT;
     this.type = type;
     this.address = address;
     this.characteristic = characteristic;
   }

   public BleRequest(RequestType type, String address, BleGattCharacteristic characteristic, String remark) {
       this.type = RequestType.CONNECT_GATT;
     this.type = type;
     this.address = address;
     this.characteristic = characteristic;
     this.remark = remark;
   }

   public RequestType getRequestType() {
     return this.type;
   }

   public boolean equals(Object o)
   {
     if (!(o instanceof BleRequest)) {
       return false;
     }

     BleRequest br = (BleRequest)o;
     return (this.type == br.type) && (this.address.equals(br.address));
   }
 }
