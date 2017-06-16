package com.genepoint.blelocate.core;

import android.os.Bundle;
import android.os.Message;

import com.genepoint.blelocate.BLELocateAction;
import com.genepoint.blelocate.BeaconLocation;
import com.genepoint.blelocate.G;
import com.genepoint.blelocate.IndoorLocationError;
import com.genepoint.blelocate.LogDebug;
import com.genepoint.locatebt.LocateCore;

import java.util.ArrayList;
import java.util.List;

/**
 * 定位线程（消费者）
 * Created by jd on 2016/7/4.
 */
public class AsyncBLELocateTask implements Runnable {
    private static String TAG = "AsyncBLELocateTask";//防止混淆后名称不可区分
    private BLELocateCore bleLocateCore = null;
    private boolean stopFlag = false;
    private BLELocateCallbackHandler handler = null;
    private boolean needJudgeFloor = true;
    private String floor = null;

    public AsyncBLELocateTask(BLELocateCallbackHandler handler) {
        this.handler = handler;
    }

    /**
     * 设定是否需要判断楼层（默认需要）
     *
     * @param needJudgeFloor
     */
    public void setNeedJudgeFloor(boolean needJudgeFloor) {
        this.needJudgeFloor = needJudgeFloor;
    }

    /**
     * 当不需要判断楼层时，传入对应楼层
     *
     * @param floor
     */
    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void run() {
        bleLocateCore = new BLELocateCore();
        if (!this.needJudgeFloor) {
            bleLocateCore.setFloor(floor);
            bleLocateCore.setNeedJudgeFloor(false);
        }
        while (!stopFlag) {
            try {
                List<BeaconLocation> scanBeacons = G.msgQueue.take();
                if (scanBeacons == null || scanBeacons.size() == 0) {
                    Message message = new Message();
                    message.what = IndoorLocationError.ERROR_LOCATE_FAIL_NOTMATCH_BEACON;
                    message.obj = new IndoorLocationError(IndoorLocationError.ERROR_LOCATE_FAIL_NOTMATCH_BEACON);
                    handler.sendMessage(message);
                    continue;
                }
                ResultLoc resultLoc = bleLocateCore.locate(scanBeacons);
                if(resultLoc.resultsLoc.size()!=2){
                    return;
                }
                LocPoint point=resultLoc.resultsLoc.get(0);
                if (point.status == 0) {
                    //过滤掉其他楼层的AP 在so里做

                    Bundle bundle = new Bundle();
                    ArrayList list = new ArrayList();
                    list.add(scanBeacons);
                    bundle.putParcelableArrayList("beacons",list);

                    Message message = new Message();
                    message.what = BLELocateAction.ACTION_TRANGLELOCATE_SUCCESS;//定位成功
                    message.obj = resultLoc;
                    message.setData(bundle);
                    handler.sendMessage(message);
                } else {
                    if (point.status == 5) {
                        Message message = new Message();
                        message.what = BLELocateAction.ACTION_TRANGLELOCATE_FAIL;//定位失败
                        message.obj = new IndoorLocationError(
                                IndoorLocationError.ERROR_LOCATE_FAIL_NOTMATCH_BEACON);
                        handler.sendMessage(message);

                    } else {
                        Message message = new Message();
                        message.what = BLELocateAction.ACTION_TRANGLELOCATE_FAIL;//定位失败
                        message.obj = new IndoorLocationError(IndoorLocationError.ERROR_LOCATE_FAIL_JNI
                                , "核心出错:" + point.status);
                        handler.sendMessage(message);

                    }
                }
            } catch (InterruptedException e) {
                //e.printStackTrace();
                //LogDebug.w(TAG,"No data to locate for long time");
                if (stopFlag) {
                    LogDebug.d(TAG, "locate task quit");
                    LocateCore.clear();
                    break;
                }
            } catch (Exception e) {
                LogDebug.e(TAG, e.toString());
            }

        }
    }

    /**
     * 停止定位线程
     */
    public void stopTask() {
        //不要在此方法中调用Thread.CurrentThread.interrupted()方法，实际上中断的是调用该方法的线程（该方法运行在调用线程中）
        stopFlag = true;
        Message message = new Message();
        message.what = BLELocateAction.ACTION_LOCATE_QUIT;
        message.obj = BLELocateAction.getMessage(BLELocateAction.ACTION_LOCATE_QUIT);
        handler.sendMessage(message);
    }
}
