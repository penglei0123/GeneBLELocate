package com.genepoint.beacon.sdk.bean;


import java.util.concurrent.ConcurrentHashMap;

public class BeaconThrowable {

    int code = 0;
    String error = null;
    private static final ConcurrentHashMap<Integer, String> mCodeMsgMap = new ConcurrentHashMap();


    public BeaconThrowable(String error, int code) {
        this.error = error;
        this.code = code;
    }


    static {
        mCodeMsgMap.put(Integer.valueOf(0), "");
        mCodeMsgMap.put(Integer.valueOf(-1), "连接超时");
        mCodeMsgMap.put(Integer.valueOf(-2), "连接断开");
        mCodeMsgMap.put(Integer.valueOf(-3), "对象为空");
        mCodeMsgMap.put(Integer.valueOf(-4), "无效参数");
        mCodeMsgMap.put(Integer.valueOf(-5), "写入异常");
        mCodeMsgMap.put(Integer.valueOf(-6), "读取异常");
        mCodeMsgMap.put(Integer.valueOf(-7), "更新异常");
        mCodeMsgMap.put(Integer.valueOf(100), "查询设备服务失败");
        mCodeMsgMap.put(Integer.valueOf(101), "不支持当前设备");
        mCodeMsgMap.put(Integer.valueOf(129), "扫描服务异常");
        mCodeMsgMap.put(Integer.valueOf(133), "还没扫描过服务以及特征就断开");
        mCodeMsgMap.put(Integer.valueOf(19), "APPKEY验证失败");
        mCodeMsgMap.put(Integer.valueOf(1001), "网络异常");
        mCodeMsgMap.put(Integer.valueOf(1002), "数据下载异常");
        mCodeMsgMap.put(Integer.valueOf(2000), "当前设备不支持该功能");
        mCodeMsgMap.put(Integer.valueOf(2001), "广播数据超过设备限制");
        mCodeMsgMap.put(Integer.valueOf(2002), "无法进行广播");
        mCodeMsgMap.put(Integer.valueOf(2003), "广播重复发送");
        mCodeMsgMap.put(Integer.valueOf(2004), "发生内部错误");
        mCodeMsgMap.put(Integer.valueOf(2005), "当前设备不支持该功能");
    }

}
