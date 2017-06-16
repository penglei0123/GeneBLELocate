package com.genepoint.beacon.utils;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@TargetApi(18)
public class Util
{

    public static String MD5(String str)
  {
    MessageDigest md5 = null;
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
    char[] charArray = str.toCharArray();
    byte[] byteArray = new byte[charArray.length];
    for (int i = 0; i < charArray.length; i++) {
      byteArray[i] = ((byte)charArray[i]);
    }
    byte[] md5Bytes = md5.digest(byteArray);
    StringBuffer hexValue = new StringBuffer();
    for (int i = 0; i < md5Bytes.length; i++) {
      int val = md5Bytes[i] & 0xFF;
      if (val < 16) {
        hexValue.append("0");
      }
      hexValue.append(Integer.toHexString(val));
    }
    return hexValue.toString().toLowerCase();
  }



  public static String getDeviceId(Context context)
  {
    try
    {
      TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
      String device_id = tm.getDeviceId();

      if (TextUtils.isEmpty(device_id)) {
        device_id = Secure.getString(context.getContentResolver(), "android_id");
      }
      if (TextUtils.isEmpty(device_id)) {
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        String mac = wifi.getConnectionInfo().getMacAddress();
        device_id = mac;
      }
      if (TextUtils.isEmpty(device_id)) {
        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        String bluetoothmac = bluetoothAdapter.getAddress();
        device_id = bluetoothmac;
      }

      return MD5(device_id);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return UUID.randomUUID().toString().replaceAll("-", "");
  }



  public static String getTimeNow()
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();
    return df.format(date);
  }

  public static Long timeStrToMillionSeconds(String str)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    long min = 0L;
    try
    {
      min = sdf.parse(str).getTime();
    } catch (ParseException e) {
      e.printStackTrace();
      min = 0L;
    } finally {
      return Long.valueOf(min);
    }
  }


  public static String MatchCode(String str_VarMboxRead)
  {
    if (TextUtils.isEmpty(str_VarMboxRead)) {
      return null;
    }
    String str_Result = "";String str_OneStr = "";
    for (int z = 0; z < str_VarMboxRead.length(); z++) {
      str_OneStr = str_VarMboxRead.substring(z, z + 1);

      if (str_OneStr.matches("[\\u4e00-\\u9fa5\\x00-\\x7F]+")) {
        str_Result = str_Result + str_OneStr;
      }
    }
    return str_Result.trim();
  }

  public static final Charset UTF_8 = Charset.forName("UTF-8");

  public static void checkOffsetAndCount(long size, long offset, long byteCount) {
    if (((offset | byteCount) < 0L) || (offset > size) || (size - offset < byteCount)) {
      throw new ArrayIndexOutOfBoundsException(String.format("size=%s offset=%s byteCount=%s", new Object[] { Long.valueOf(size), Long.valueOf(offset), Long.valueOf(byteCount) }));
    }
  }

  public static short reverseBytesShort(short s) {
    int i = s & 0xFFFF;
    int reversed = (i & 0xFF00) >>> 8 | (i & 0xFF) << 8;

    return (short)reversed;
  }

  public static int reverseBytesInt(int i) {
    return (i & 0xFF000000) >>> 24 | (i & 0xFF0000) >>> 8 | (i & 0xFF00) << 8 | (i & 0xFF) << 24;
  }

  public static long reverseBytesLong(long v) {
    return (v & 0L) >>> 56 | (v & 0L) >>> 40 | (v & 0L) >>> 24 | (v & 0L) >>> 8 | (v & 0xFFFFFFFFFF000000l) << 8 | (v & 0xFF0000) << 24 | (v & 0xFF00) << 40 | (v & 0xFF) << 56;
  }

  public static void sneakyRethrow(Throwable t) {
    try {
      sneakyThrow2(t);
    }
    catch (Throwable e) {
      e.printStackTrace();
    }
  }

  private static <T extends Throwable> void sneakyThrow2(Throwable t) throws Throwable {
    throw t;
  }

  public static boolean arrayRangeEquals(byte[] a, int aOffset, byte[] b, int bOffset, int byteCount) {
    for (int i = 0; i < byteCount; i++) {
      if (a[(i + aOffset)] != b[(i + bOffset)])
        return false;
    }
    return true;
  }

  public static byte[] upload(byte msg_id, byte[] msg_data)
  {
    byte[] msg_a = new byte[20];
    msg_a[0] = msg_id;
    if (msg_data == null) {
      msg_a[1] = 0;
    } else {
      msg_a[1] = ((byte)(msg_data.length & 0xFF));
      System.arraycopy(msg_data, 0, msg_a, 2, msg_data.length);
    }

    return msg_a;
  }

  public static byte[] encodeMsg(byte msg_id, byte[] msg_data)
  {
    byte[] msg_a = new byte[20];
    msg_a[0] = msg_id;
    if (msg_data == null) {
      msg_a[1] = 2;
    } else {
      msg_a[1] = ((byte)(msg_data.length + 2 & 0xFF));
      System.arraycopy(msg_data, 0, msg_a, 2, msg_data.length);
    }

    return msg_a;
  }

}

