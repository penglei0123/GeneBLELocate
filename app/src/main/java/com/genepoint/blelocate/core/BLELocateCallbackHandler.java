package com.genepoint.blelocate.core;

import android.os.Handler;
import android.os.Message;

import com.genepoint.blelocate.BLELocateAction;
import com.genepoint.blelocate.BLELocateCallback;
import com.genepoint.blelocate.BLELocateService;
import com.genepoint.blelocate.BeaconLocation;
import com.genepoint.blelocate.G;
import com.genepoint.blelocate.IndoorLocationError;

import java.util.ArrayList;

/**
 * Created by jd on 2016/3/5.
 * 定位结果消息处理handler,运行在UI线程
 */
public class BLELocateCallbackHandler extends Handler {
    private BLELocateCallback callback;
    private BLELocateService service;

    public BLELocateCallbackHandler(BLELocateService bleLocateService, BLELocateCallback callback) {
        this.callback = callback;
        this.service = bleLocateService;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case IndoorLocationError.ERROR_LOCATE_FAIL_NOTMATCH_BEACON:
            case IndoorLocationError.ERROR_BLE_VERSION_LOW: //手机不支持
            case IndoorLocationError.ERROR_HTTP_QUERY_BUILDING://后台定位建筑错误
            case IndoorLocationError.ERROR_LBSKEY_NULL://key空
            case IndoorLocationError.ERROR_BLE_NOT_ON://蓝牙没有开启
            case IndoorLocationError.ERROR_SDCARD_WRITER_FORBIDDEN://没有写权限
            case IndoorLocationError.ERROR_INTERNT_ERROR://网络异常
            case IndoorLocationError.ERROR_LOCATE_TOO_LONG:// 定位时间过长
            case IndoorLocationError.ERROR_SCAN_EMPTY:// 蓝牙扫描为空
            case IndoorLocationError.ERROR_HASNOT_DATE://不存在数据
            case IndoorLocationError.ERROR_DATE_ERROR:
                this.callback.onFail((IndoorLocationError) msg.obj);
                service.stopNavigation();
                break;
            case BLELocateAction.ACTION_LOCATE_BUILD_SUCESS://定位建筑成功
                this.callback.onLocateBuildingSuccess(msg.obj.toString());
                break;
            case BLELocateAction.ACTION_INIT_SUCCESS://初始化完成
                this.callback.onInitFinish();
                break;
            case BLELocateAction.ACTION_TRANGLELOCATE_SUCCESS://定位成功
                ResultLoc point = (ResultLoc) msg.obj;
                ArrayList<BeaconLocation> list = (ArrayList<BeaconLocation>)msg.getData().getParcelableArrayList("beacons").get(0);

                G.locPointQueue.offer(point.resultsLoc.get(0));
                this.callback.onSuccess(point);
                break;
            case BLELocateAction.ACTION_TRANGLELOCATE_FAIL://定位失败
                this.callback.onFail((IndoorLocationError) msg.obj);
            //    service.stopNavigation();
                break;
//            case BLELocateAction.ACTION_LOCATE_QUIT:
//                this.callback.onMessage(msg.obj.toString());
//                break;


            default:
        }
    }
}
