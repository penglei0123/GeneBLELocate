package com.genepoint.locatebt;

import com.genepoint.blelocate.LogDebug;

/**
 * Created by jd on 2016/7/24.
 */
public class LocateCore {

    /**
     * 定位初始化接口
     *
     * @param apfilePath beacon数据路径
     * @param uuidList   回传的UUID数组
     * @param majorList  回传的major数组
     * @return
     */
    public native static int init(String apfilePath, String[] uuidList, String[] majorList, Integer uuidSize);

    /**
     * 定位方法接口
     *
     * @param minorList     minor数组
     * @param rssiList      信号强度数组
     * @param num           beacon数量
     * @param x             回传的定位坐标
     * @param y             回传的定位坐标
     * @param z             回传的楼层索引
     * @param algorithmType 定位算法
     * @return
     */
    public native static int    locate(String[] minorList, int[] rssiList, int num, Double x, Double y, Integer z, int algorithmType);

    /**
     * 动态库内存清理接口
     */
    public native static void clear();

    public native static int motiongo(double x[], double y[], double z[], int num);

    static {
        try {
            LogDebug.w("LocateCore","loadLibrary start");
            System.loadLibrary("LocateBT");
            LogDebug.w("LocateCore","loadLibrary end");
        }catch (Exception e){
            LogDebug.w("LocateCore","loadLibrary exception:"+e);
        }

    }
}
