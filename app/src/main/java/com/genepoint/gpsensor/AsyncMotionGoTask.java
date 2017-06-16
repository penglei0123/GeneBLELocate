package com.genepoint.gpsensor;

import com.genepoint.blelocate.LogDebug;
import com.genepoint.locatebt.LocateCore;

/**
 * Created by jsj on 2016/10/20.
 */

public class AsyncMotionGoTask implements Runnable {
    private static String TAG = "AsyncMotionGoTask";//防止混淆后名称不可区分
    private double accDatas[][] = new double[3][50];

    public AsyncMotionGoTask(double[][] datas) {
        if (accDatas.length == 3 && accDatas[0].length == 50) {
            this.accDatas = datas;
        }

    }

    @Override
    public void run() {
       //获取结构，发送消息
        StringBuffer stringBuffer=new StringBuffer();
            for(int i=0;i<3;i++){
                stringBuffer.append(""+i+":");
                for (int j=0;j<50;j++){
                    stringBuffer.append(accDatas[i][j]+"\t");
                }
            }
        try {
            LocateCore.motiongo(accDatas[0], accDatas[1], accDatas[2], 50);
        }catch (Exception e){
            LogDebug.w("LocateCore","LocateCore.motiongo Exception:"+e);
        }
    }
}
