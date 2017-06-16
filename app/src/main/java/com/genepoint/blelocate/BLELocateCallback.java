package com.genepoint.blelocate;

import android.widget.RelativeLayout;

import com.genepoint.blelocate.core.LocPoint;
import com.genepoint.blelocate.core.ResultLoc;

/**
 * 定位结果回调方法
 */
public interface BLELocateCallback {
    /**
     * 成功初始化
     */
    void onInitFinish();

    /**
     * 定位建筑成功
     *
     * @param buildingCode 建筑编码
     */
    void onLocateBuildingSuccess(String buildingCode);

    /**
     * 定位成功
     *
     * @param p 定位结果
     */
    void onSuccess(ResultLoc p);

    /**
     * 定位失败
     *
     * @param error 错误信息
     */
    void onFail(IndoorLocationError error);
}
