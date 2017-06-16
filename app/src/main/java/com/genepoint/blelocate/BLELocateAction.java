package com.genepoint.blelocate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jd on 2016/1/7.
 */
public class BLELocateAction {
    public static final int ACTION_TRANGLELOCATE_SUCCESS = 0x004;//定位成功
    public static final int ACTION_TRANGLELOCATE_FAIL = 0x005;//定位失败
    public static final int ACTION_LOCATE_QUIT = 0x007;//定位线程退出
    public static final int ACTION_INIT_SUCCESS = 0x014;//初始化
    public static final int ACTION_LOCATE_BUILD_SUCESS = 0x016;//定位建筑成功

    private static Map<Integer, String> Message = new HashMap<Integer, String>();

    static {
        Message.put(ACTION_INIT_SUCCESS, "初始化成功，可以定位");
        Message.put(ACTION_TRANGLELOCATE_FAIL, "定位失败");
        Message.put(ACTION_TRANGLELOCATE_SUCCESS, "定位成功");
        Message.put(ACTION_LOCATE_QUIT, "蓝牙定位服务退出");
        Message.put(ACTION_LOCATE_BUILD_SUCESS, "定位建筑成功");
    }

    public static String getMessage(int status) {
        return Message.get(status);
    }
}
