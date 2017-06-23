package com.genepoint.blelocate.core;

import com.genepoint.blelocate.BLELocateService;
import com.genepoint.blelocate.BeaconLocation;
import com.genepoint.blelocate.G;
import com.genepoint.blelocate.LogDebug;
import com.genepoint.gpsensor.GPSensorManager;
import com.genepoint.locatebt.LocateCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jd on 2016/2/29.
 */
public class BLELocateCore {
    private static String TAG = "BLELocateCore";//防止混淆后名称不可区分
    private static long locate_t_pre = 0;//上次定位时间
    private static long locate_t_now = 0;//下次定位时间

    private final double JUMP_CHUNK_0 = 5.0; // 最大跳动距离m
    private final double JUMP_CHUNK_1 = 2.0;
    private final double JUMP_CHUNK_2 = 1.5;
    private final  double rate=0.5;

    //当前楼层
    private String curFloor = null;
    private boolean needJudgeFloor = true;

    private LocPoint lastPos = null;

    //用于筛选的BLE信号强度阈值
    private static int THREHOLD_RSSI = -95;//modified from -95
    //历史楼层定位结果
    private List<String> floorList = new ArrayList<String>();
    private Set<String> minorSet = new HashSet<>();

    private LinkedList<Double> stepList=new LinkedList<>();

    private boolean judgeFloorFirstTime = false;

    public BLELocateCore() {
        this.curFloor = null;
    }

    public static void initTime() {
        locate_t_pre = 0;
        locate_t_now = 0;
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
        this.curFloor = floor;
    }

    public void judgeFloorFirstTime(List<BeaconLocation> list) throws Exception {
        try {
            if (curFloor == null) {
                Map<String, Integer> countMap = new HashMap<>();
                for (BeaconLocation b : list) {
                    if (countMap.containsKey(b.getFloor())) {
                        countMap.put(b.getFloor(), countMap.get(b.getFloor()) + 1);
                    } else {
                        countMap.put(b.getFloor(), 1);
                    }
                }
                int max = 0;
                for (String floor : countMap.keySet()) {
                    if (countMap.get(floor) > max) {
                        curFloor = floor;
                        max = countMap.get(floor);
                    }
                }
                judgeFloorFirstTime = true;
                floorList.add(curFloor);
                //楼层定位完毕，数据整理用于定位
                Map<String, List<BeaconLocation>> beaconMap = new HashMap<>();
                for (BeaconLocation b : list) {
                    String minorStr = String.format("%04X", b.getMinor());
                    List<BeaconLocation> sameBeaconList = beaconMap.get(minorStr);
                    if (sameBeaconList != null) {
                        sameBeaconList.add(b);
                    } else {
                        sameBeaconList = new ArrayList<>();
                        sameBeaconList.add(b);
                        beaconMap.put(minorStr, sameBeaconList);
                    }
                }
                List<Integer> rssiList = new ArrayList<>();
                List<BeaconLocation> scanBeacons = new ArrayList<>(20);
                Iterator it = beaconMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<BeaconLocation>> entry = (Map.Entry) it.next();
                    String minorStr = entry.getKey();
                    List<BeaconLocation> sameBeaconList = entry.getValue();
                    BeaconLocation beaconLocation = new BeaconLocation();
                    beaconLocation.setMAC(sameBeaconList.get(0).getMAC());
                    beaconLocation.setMinor(Integer.parseInt(minorStr, 16));
                    beaconLocation.setFloor(G.beaconsLocation.get(minorStr).getFloor());
                    beaconLocation.setPosX(G.beaconsLocation.get(minorStr).getPosX());
                    beaconLocation.setPosY(G.beaconsLocation.get(minorStr).getPosY());
                    rssiList.clear();
                    for (BeaconLocation b : sameBeaconList) {
                        rssiList.add(b.getLevel());
                    }
                    int aveRssi = Gaussian.GaussianFilter(rssiList, 0.6f);
//							int total = 0;
//							for(Beacon beacon:list){
//								total+=beacon.getRssi();
//							}
//							aveRssi = total / list.size();
                    beaconLocation.setLevel(aveRssi);
                    scanBeacons.add(beaconLocation);
                }
                list.clear();
                list.addAll(scanBeacons);
                LogDebug.i(TAG, "judge floor first time");
            }
        } catch (Exception e) {
            throw new Exception(e.toString());
        }
    }

    public ResultLoc locate(List<BeaconLocation> list) {
        if (list.size() == 0) {
            return null;
        }
        try {
            //按信号强度降序
            Collections.sort(list, new Comparator<BeaconLocation>() {
                @Override
                public int compare(BeaconLocation lhs, BeaconLocation rhs) {
                    return rhs.getLevel() - lhs.getLevel();
                }
            });
            List<BeaconLocation> btInfoList = new ArrayList<>();

            //选取大于阈值的AP
            for (int i = 0; i < list.size(); i++) {
                BeaconLocation btInfo = list.get(i);
                if (btInfo.getLevel() >= THREHOLD_RSSI)
                    btInfoList.add(btInfo);
            }
            if (btInfoList.size() == 0) {
                //保证至少有一个
                btInfoList.add(list.get(0));
            }

            StringBuffer sb = new StringBuffer();
            for (BeaconLocation b : btInfoList) {
                sb.append(b.getMAC() + "|" + String.format("%04X", b.getMinor()) + "|" + b.getLevel() + "\t");
            }
            LogDebug.i(TAG, sb.toString());
            int size = btInfoList.size();
            String[] minorList = new String[size];
            int[] rssList = new int[size];
            for (int i = 0; i < size; i++) {
                BeaconLocation b = btInfoList.get(i);
                minorList[i] = String.format("%04X", b.getMinor());
                rssList[i] = b.getLevel();
            }
            Double corX = new Double(0);
            Double corY = new Double(0);
            Integer floorIndex = new Integer(0);
            int res = LocateCore.locate(minorList, rssList, size, corX, corY, floorIndex, 2);
            locate_t_now = System.currentTimeMillis();
            //定位出错时的处理
            if (res != 0) {
                if (locate_t_pre == 0 && locate_t_now == 0) {//首次定位
                    //返回错误
                    return new ResultLoc(res);
                }
                float timeInterval = (locate_t_now - locate_t_pre) / 1000.0f;
                if (timeInterval > 5) { // 定位异常间隔时间过长
                    return  new ResultLoc(res);
                } else {//返回上次定位结果
                    if (lastPos == null) {
                        LogDebug.e(TAG, "locate error:" + res + ",return last result:null");
                        return  new ResultLoc(res);
                    } else {
                        LogDebug.e(TAG, "locate error:" + res + ",return last result:" + lastPos.Floor + "\t" + lastPos.Xcor + "," + lastPos.Ycor);
                        return  new ResultLoc(lastPos,lastPos);
                    }
                }
            } else {
                //有定位结果
                curFloor = floorIndex > 0 ? "F" + floorIndex : "B" + Math.abs(floorIndex);
                LogDebug.i(TAG, "locate result:" + curFloor + "\t" + corX + "," + corY);
            }
            locate_t_pre = locate_t_now;
            btInfoList.clear();
            LocPoint resultLoc = new LocPoint(G.buildingCode, curFloor, corX, corY);
            LocPoint preLoc=new LocPoint(G.buildingCode, curFloor, corX, corY);
            if (lastPos == null||!lastPos.Floor.equals(resultLoc.Floor)) {
                lastPos = resultLoc;
                stepList.clear();
            }else{
                LogDebug.w(TAG, "优化前：" + resultLoc + "----lastPos:" + lastPos);
             if (BLELocateService.isDirectionOptimizationFlag()) {//方向象限优化
                resultLoc= optimizeLocPoint(resultLoc,  0.5);
                LogDebug.w(TAG, "1优化后：" + resultLoc + "----lastPos:" + lastPos);
             }
             stepList.add(dis(resultLoc,lastPos));
             if(stepList.size()>20){
                if(stepList.size()>65){
                    stepList.removeFirst();
                }
                 //聚类计算优化
                 LogDebug.w(TAG,"聚类："+dis(resultLoc,lastPos)+"--"+calcDisPerSecByCluster(stepList));
                 if(dis(resultLoc,lastPos)>1.5*calcDisPerSecByCluster(stepList)){

                     resultLoc.Xcor = lastPos.Xcor + (resultLoc.Xcor-lastPos.Xcor)*0.8;
                     resultLoc.Ycor = lastPos.Ycor + (resultLoc.Ycor - lastPos.Ycor)*0.8;
                     lastPos.Xcor = 2 * lastPos.Xcor / 3.0 + resultLoc.Xcor / 3.0;
                     lastPos.Ycor = 2 * lastPos.Ycor / 3.0 + resultLoc.Ycor / 3.0;
                     LogDebug.w(TAG, "2优化后：" + resultLoc + "----lastPos:" + lastPos);
                 }
             }

//             else {
//                 /********距离限制优化*********/
//                 if (BLELocateService.getXUnitDistance() != 0 && BLELocateService.getYUnitDistance() != 0) {
//                     double dx = resultLoc.Xcor - lastPos.Xcor;
//                     double dy = resultLoc.Ycor - lastPos.Ycor;
//                     if (Math.sqrt(Math.pow(dx * BLELocateService.getXUnitDistance(), 2) + Math.pow(dy * BLELocateService.getXUnitDistance(), 2)) > JUMP_CHUNK_0) {
//                         resultLoc.Xcor = rate * resultLoc.Xcor + rate * lastPos.Xcor;
//                         resultLoc.Ycor = rate * resultLoc.Ycor + rate * lastPos.Ycor;
//                         lastPos.Xcor = resultLoc.Xcor;
//                         lastPos.Ycor = resultLoc.Ycor;
//                     }
//                     LogDebug.w(TAG, "2优化后：" + resultLoc + "----lastPos:" + lastPos);
//                 }
//
//             }
            }
            return new ResultLoc(resultLoc,preLoc);
        } catch (Exception e) {
            LogDebug.e(TAG, "locate error:" + e.toString());
            return new ResultLoc(lastPos,lastPos);
        }
    }
    public double calcDisPerSecByCluster(LinkedList<Double> stepList){
        double min=1000,max=0;
        for(Double x:stepList){
            min=x<min?x:min;
            max=x>max?x:max;
        }
        while (true){

            List<Double> lowArr=new ArrayList<>();
            List<Double> highArr=new ArrayList<>();
            for(Double y:stepList){
               if(Math.abs(y-min)<Math.abs(y-max)){
                   lowArr.add(y);
               }else{
                   highArr.add(y);
               }
            }
            //求平均值
            double lowTotal=0,highTotal=0;
            for(int i=0;i<lowArr.size();i++){
                lowTotal += lowArr.get(i);
            }
            double lowAvr = lowTotal/lowArr.size();
            for(int i=0;i<highArr.size();i++){
                highTotal += highArr.get(i);
            }
            double highAvr = highTotal/highArr.size();
            LogDebug.w(TAG,"calcDisPerSecByCluster "+lowAvr+" low size:"+lowArr.size()+" "+min+" "+highAvr+" "+max );
            if((lowAvr-min)<10e-8&&(highAvr-max)<10e-8){
                LogDebug.w(TAG,"calcDisPerSecByCluster ok ");
                return highAvr ;
            }else{
                min=lowAvr;
                max=highAvr;
            }
        }

    }

    /**
     * @param originalPoint
     * @return
     */
    private LocPoint optimizeLocPoint(LocPoint originalPoint, double rate) {
        LocPoint resultLoc;
        double dis = dis(originalPoint, lastPos);//距离
        double dy = 0, dx = 0;
        if (dis > 0) {
            if (BLELocateService.isYAxisUpwardsFlag()) {
                dy = originalPoint.Ycor - lastPos.Ycor;
            } else {
                dy = lastPos.Ycor - originalPoint.Ycor;
            }
            dx = originalPoint.Xcor - lastPos.Xcor;
            double theta = Math.acos(dy / dis); // 此算法针对安卓手机，正北为0，正东90，正西-90
            if (dx < 0) {
                theta = 2 * Math.PI - theta;
            }
            double vectorQuad =  180 * theta / Math.PI;
            double rotQuad = GPSensorManager.getOrientation() ;
            LogDebug.w(TAG,"vectorQuad:"+vectorQuad+"/ rotQuad:"+rotQuad);
            if (Math.abs(vectorQuad- rotQuad)>45) {
                resultLoc = new LocPoint();
                resultLoc.building=originalPoint.building;
                resultLoc.Floor = originalPoint.Floor;
                resultLoc.status = originalPoint.status;
                resultLoc.Xcor = lastPos.Xcor + dx / 2;
                resultLoc.Ycor = lastPos.Ycor + (originalPoint.Ycor - lastPos.Ycor) / 2;

                lastPos.Xcor = 2 * lastPos.Xcor / 3.0 + originalPoint.Xcor / 3.0;
                lastPos.Ycor = 2 * lastPos.Ycor / 3.0 + originalPoint.Ycor / 3.0;
//                /********距离限制优化*********/
//                if (BLELocateService.getXUnitDistance() != 0 && BLELocateService.getYUnitDistance() != 0) {
//                    if (Math.sqrt(Math.pow(dx * BLELocateService.getXUnitDistance(), 2) + Math.pow(dy * BLELocateService.getXUnitDistance(), 2)) > JUMP_CHUNK_0) {
//                        resultLoc.Xcor = rate * resultLoc.Xcor + rate * lastPos.Xcor;
//                        resultLoc.Ycor = rate * resultLoc.Ycor + rate * lastPos.Ycor;
//                        lastPos.Xcor = resultLoc.Xcor;
//                        lastPos.Ycor = resultLoc.Ycor;
//                    }
//                }
                return resultLoc;
            }else{
                lastPos = originalPoint;
                return originalPoint;
            }
        } else {
            lastPos = originalPoint;
            return originalPoint;
        }

    }

    private double dis(LocPoint point1, LocPoint point2) {
        return Math.sqrt(Math.pow(point1.Xcor - point2.Xcor, 2) + Math.pow(point1.Ycor - point2.Ycor, 2));
    }



}
