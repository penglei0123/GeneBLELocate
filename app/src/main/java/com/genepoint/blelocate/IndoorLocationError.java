package com.genepoint.blelocate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jsj on 2016/11/7.
 * 错误码信息
 */

public class IndoorLocationError {
    public static final int ERROR_INTERNT_ERROR = 301;//网络
    public static final int ERROR_LOCATE_TOO_LONG = 302;//定位超时
    public static final int ERROR_SCAN_EMPTY = 303;//蓝牙扫描为空
    public static final int ERROR_SDCARD_WRITER_FORBIDDEN = 304;//没有写权限
    public static final int ERROR_BLE_NOT_ON = 305;//没有开启蓝牙
    public static final int ERROR_BLE_VERSION_LOW = 306;//手机不支持
    public static final int ERROR_LBSKEY_NULL = 307;//APP里key为空
    public static final int ERROR_LOCATE_FAIL_JNI = 308;//定位失败jni
    public static final int ERROR_LOCATE_FAIL_NOTMATCH_BEACON = 309;//定位失败，没有匹配到大厦蓝牙
    public static final int ERROR_HTTP_QUERY_BUILDING = 310;//后台定位建筑错误
    public static final int ERROR_DATE_ERROR = 312;
    public static final int ERROR_HASNOT_DATE = 313;


    private static Map<Integer, String> Message = new HashMap<Integer, String>();

    static {
        Message.put(ERROR_INTERNT_ERROR, "网络错误，本地查询为空");
        Message.put(ERROR_LOCATE_TOO_LONG, "定位超时");
        Message.put(ERROR_SCAN_EMPTY, "扫描出错,请尝试重启设备蓝牙");
        Message.put(ERROR_SDCARD_WRITER_FORBIDDEN, "SD卡没有写权限");
        Message.put(ERROR_BLE_NOT_ON, "定位需要开启设备蓝牙");
        Message.put(ERROR_BLE_VERSION_LOW, "设备不支持低功耗蓝牙4.0");
        Message.put(ERROR_LBSKEY_NULL, "GP_LBSKEY为空");
        Message.put(ERROR_LOCATE_FAIL_JNI, "核心出错");
        Message.put(ERROR_LOCATE_FAIL_NOTMATCH_BEACON, "没有扫描到部署的蓝牙");
        Message.put(ERROR_DATE_ERROR, " data error");
        Message.put(ERROR_HASNOT_DATE, "has not data");
    }

    public static String getMessage(int status) {
        return Message.get(status);
    }

    public int errorCode;
    public String errorMsg;

    public IndoorLocationError(int errorCode) {
        this.errorCode = errorCode;
        this.errorMsg = getMessage(errorCode);
    }

    public IndoorLocationError(int errorCode, String msg) {
        this.errorCode = errorCode;
        this.errorMsg = msg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
