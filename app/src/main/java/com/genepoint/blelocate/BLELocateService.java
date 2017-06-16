package com.genepoint.blelocate;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.genepoint.beacon.interfaces.BeaconManagerListener;
import com.genepoint.beacon.sdk.bean.Beacon;
import com.genepoint.beacon.sdk.bean.BeaconThrowable;
import com.genepoint.beacon.sdk.service.BeaconManager;
import com.genepoint.blelocate.accumulate.AsyncAccmulateTask;
import com.genepoint.blelocate.core.AsyncBLELocateTask;
import com.genepoint.blelocate.core.BLELocateCallbackHandler;
import com.genepoint.blelocate.core.BLELocateCore;
import com.genepoint.gpsensor.GPSensorManager;
import com.genepoint.locatebt.LocateCore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jd on 2016/1/20.
 */
public class BLELocateService {
    private static String TAG = BLELocateService.class.getSimpleName();
    private static String SDK_VERSION = "V2.1.20170105";

    private final static int RETRY_TIME = 3;//网络访问尝试3次
    /**
     * 单例模式
     */
    private static BLELocateService bleLocateServiceInstance = null;
    private Context ctx = null;
    private String buildingCode = null;
    //存放所有大厦数据的根目录
    private static String buildingDataPath;
    private static String localBuildingFilePath;

    /**
     * Beacon扫描
     */
    private BeaconManager brtBeaconManager = null;
    private boolean isServiceStarted = false;

    //扫描间隔时间（毫秒）
    private static int TIME_SCAN_PRERIOD = 200;//modified from 150

    private static int serverVersion = -1;//服务端数据版本

    private BLELocateCallbackHandler locateHandler = null;
    private AsyncBLELocateTask locateTask = null;
    /**
     * 定位回调接口
     */
    private BLELocateCallback callback = null;
    //消费者线程
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    //异步上传（周期调度任务）
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    private static int FIRST_SCAN_TIMES = 10;
    //是否是第一次扫描
    private boolean firstScan = true;

    private String deviceId = null;

    private boolean enableTestMode = false;//测试模式

    private boolean locateBuildingSuccess = false;
    //是否需要定位建筑
    private boolean needLocateBuilding = false;
    //存储定位建筑的多次扫描结果
    private ArrayList<Beacon> beaconsForLocateBuilding = new ArrayList<>();
    //定位建筑的剩余扫描次数
    private int remainJudgeBuildingScanTimes = 10;//目前设为10
    private HashMap<String, String> localBuildingsUMMap = null;//本地缓存建筑 UM对照  key:buildingcode value:UUID_Major


    //key
    private static String key;//token
    private static String downurl;//下载定位数据的url

    private GPSensorManager gpsensorManager = null;
    private boolean accFlag = false;
    private static boolean directionOptimizationFlag=false;//方向优化flag
    private static boolean YAxisUpwardsFlag=false;//y轴朝上flag
//    private static double XUnitDistance=0;//X轴单位距离（米）
//    private static double YUnitDistance=0;//Y轴单位距离（米）
    private boolean bleScanFlag=false;


    private BLELocateService(Context ctx) {
        this.ctx = ctx;
        buildingDataPath = getSDCardPath() + "/Android/data/" + getAppInfo(ctx)
                + "/files/bledata";
        localBuildingFilePath = buildingDataPath + "/loaclTable.pkg";
        File file = new File(buildingDataPath);
        if (!file.exists()) {
            file.mkdirs();
            LogDebug.w(TAG, buildingDataPath + " 创建目录");
        }
        brtBeaconManager = BeaconManager.getInstance(ctx);
        brtBeaconManager.startService();
        gpsensorManager = GPSensorManager.getInstance(ctx);
        loaclBuildingInit();//初始化本地查询库
        downurl = null;
        deviceId = getDeviceMac();
        //启动定位结果上传异步任务
        scheduledExecutorService.scheduleAtFixedRate(new AsyncAccmulateTask(deviceId), 5, 5, TimeUnit.SECONDS);
        //启动日志持久化任务
        scheduledExecutorService.scheduleAtFixedRate(new AsyncPersistenceLogTask(), 1, 1, TimeUnit.SECONDS);
    }


    /***
     * 是否启用优化
     * @param directionOptimizationFlag true 启用
     * @param YAxisUpwardsFlag true Y轴朝向向上
     */
    public void setDirectionOptimizationEnable(boolean directionOptimizationFlag,boolean YAxisUpwardsFlag) {
        if(directionOptimizationFlag){
            this.YAxisUpwardsFlag=YAxisUpwardsFlag;
            this.directionOptimizationFlag = directionOptimizationFlag;
        }
    }

    public static  boolean isYAxisUpwardsFlag() {
        return YAxisUpwardsFlag;
    }

    public static boolean isDirectionOptimizationFlag() {
        return directionOptimizationFlag;
    }

//    /***
//     *
//     * 坐标系距离1代表的距离米
//     *
//     * @return
//     */
//    public static double getXUnitDistance() {
//        return XUnitDistance;
//    }
//    /***
//     *
//     * 坐标系距离1代表的距离米
//     *
//     * @return
//     */
//    public static double getYUnitDistance() {
//        return YUnitDistance;
//    }
//
//    public static void setUnitDistance(double xunitDistance,double yunitDistance) {
//        if(xunitDistance>0&&yunitDistance>0) {
//            XUnitDistance = xunitDistance;
//            YUnitDistance = yunitDistance;
//        }
//    }

    /**
     * 蓝牙定位服务获取方法，单例模式
     *
     * @param ctx
     * @return
     */
    public static BLELocateService getInstance(Context ctx) {
        if (bleLocateServiceInstance == null) {
            bleLocateServiceInstance = new BLELocateService(ctx);
        }
        return bleLocateServiceInstance;
    }

    private BeaconManagerListener rangingListener = new BeaconManagerListener() {
        private int scanTimes = 5;
        private int remainTimes = scanTimes;
        private List<BeaconLocation> scanBeacons = new ArrayList<>(20);

        @Override
        public void onUpdateBeacon(final ArrayList<Beacon> rangingResult) {
           LogDebug.w(TAG, "蓝牙数量：" + rangingResult.size());
            //忽略第一次扫描结果
            if (firstScan) {
                firstScan = false;
                remainTimes = FIRST_SCAN_TIMES;
                return;
            }
            if (needLocateBuilding) {//判断大厦的扫描
                if (remainJudgeBuildingScanTimes > 0) {
                    synchronized (beaconsForLocateBuilding) {
                        for (Beacon mBeacon : rangingResult) {
                            beaconsForLocateBuilding.add(mBeacon);
                        }
                    }
                    remainJudgeBuildingScanTimes--;
                } else {
                    if (beaconsForLocateBuilding.size() == 0 && rangingResult.size() == 0) {
                        Message message = new Message();
                        message.what = IndoorLocationError.ERROR_SCAN_EMPTY;
                        message.obj = new IndoorLocationError(IndoorLocationError.ERROR_SCAN_EMPTY);
                        locateHandler.sendMessage(message);
                        return;
                    }
                    needLocateBuilding = false;
                    //将蓝牙统计结果 post查询
                    queryBuilding();
//                    //停止扫描
 //                   brtBeaconManager.stopRanging();
//                    brtBeaconManager.setBRTBeaconManagerListener(null);
                }
                return;
            }
            if (!locateBuildingSuccess) {
                return;
            }

            --remainTimes;
            for (Beacon beacon : rangingResult) {
                //剔除不相关Beacon
                if (!G.uuidMap.containsKey(beacon.getUuid())||G.majorMap.containsKey(beacon.getMajor())) {
                    continue;
                }
                BeaconLocation b = new BeaconLocation();
                b.setMinor(beacon.getMinor());
                b.setLevel(beacon.getRssi());
                scanBeacons.add(b);
            }
            if (remainTimes <= 0) {
                if (scanBeacons.size() == 0 && rangingResult.size() == 0) {
                    Message message = new Message();
                    message.what = IndoorLocationError.ERROR_SCAN_EMPTY;
                    message.obj = new IndoorLocationError(IndoorLocationError.ERROR_SCAN_EMPTY);
                    locateHandler.sendMessage(message);
                    return;
                }
                try {
                    //生产数据(UI线程，无界队列，无阻塞放入)
                    G.msgQueue.offer(scanBeacons);
                } catch (Exception e) {
                    sendMessage(IndoorLocationError.ERROR_LOCATE_TOO_LONG);
                    e.printStackTrace();
                }
                scanBeacons = new ArrayList<>(20);
                remainTimes = scanTimes;
            }
        }

        @Override
        public void onError(BeaconThrowable paramBRTThrowable) {

        }
    };

    /**
     * 开始定位接口
     *
     * @param callback 定位结果回调方法
     */
    public void startNavigation(BLELocateCallback callback) {
        stopNavigation();
        this.callback = callback;
        locateHandler = new BLELocateCallbackHandler(BLELocateService.this, callback);
        if ((key = getBLApiKey(ctx)) == null) {
            sendMessage(IndoorLocationError.ERROR_LBSKEY_NULL);
            return;
        }
      //  brtBeaconManager = BeaconManager.getInstance(ctx);
        if (!brtBeaconManager.isBluetoothEnabled()) {
            sendMessage(IndoorLocationError.ERROR_BLE_NOT_ON);
            return;
        }
        serverVersion = -1;//数据版本
        BLELocateCore.initTime();//初始化定位时间
        //初始化定位建筑的参数
        needLocateBuilding = true;
        remainJudgeBuildingScanTimes = 10;
        locateBuildingSuccess = false;
        beaconsForLocateBuilding = new ArrayList<>();
        startBeaconScan();//扫描

        if (gpsensorManager != null) {//惯导
            gpsensorManager.startCollectionACC();
            accFlag = true;
        }

    }

    /**
     * 开始定位接口
     *
     * @param buildingCode 建筑编码，指定值时加载该建筑编码数据去定位，为null自动判断建筑
     * @param callback     定位结果回调方法
     */
    public void startNavigation(String buildingCode, BLELocateCallback callback) {
        stopNavigation();
        this.callback = callback;
        locateHandler = new BLELocateCallbackHandler(BLELocateService.this, callback);
        if ((key = getBLApiKey(ctx)) == null) {
            sendMessage(IndoorLocationError.ERROR_LBSKEY_NULL);
            return;
        }
     //   brtBeaconManager = BeaconManager.getInstance(ctx);
        if (!brtBeaconManager.isBluetoothEnabled()) {
            sendMessage(IndoorLocationError.ERROR_BLE_NOT_ON);
            return;
        }
        serverVersion = -1;//数据版本
        BLELocateCore.initTime();//初始化定位时间
        if (buildingCode != null) {//指定建筑
            locateBuildingSuccess = true;
            startLocatePostion(buildingCode, null, callback);
        } else {//需要定位建筑
            //初始化定位建筑的参数
            needLocateBuilding = true;
            remainJudgeBuildingScanTimes = 10;
            locateBuildingSuccess = false;
            beaconsForLocateBuilding = new ArrayList<>();
            LogDebug.w(TAG, "去扫描以判断建筑");
            startBeaconScan();//扫描
        }
        if (gpsensorManager != null) {//惯导
            gpsensorManager.startCollectionACC();
            accFlag = true;
        }

    }

    /**
     * 查询大厦
     */
    private void queryBuilding() {
        Map<String, Integer> uuidMajor = new HashMap<>();
        synchronized (beaconsForLocateBuilding) {
            if (beaconsForLocateBuilding.size() == 0) {
                sendMessage(IndoorLocationError.ERROR_SCAN_EMPTY);
                return;
            }
            for (Beacon brtBeacon : beaconsForLocateBuilding) {
                String UM = brtBeacon.getUuid() + "\t" + String.format("%04X", brtBeacon.getMajor());
                Integer count;
                if ((count = uuidMajor.get(UM)) != null) {
                    uuidMajor.put(UM, count + 1);
                } else {
                    uuidMajor.put(UM, 1);
                }
            }
            beaconsForLocateBuilding.clear();
        }
        //定位建筑标识关闭
        needLocateBuilding = false;
        //后台查询
        LogDebug.w(TAG, "go to server query build");
        serverQueryBuilding(uuidMajor);
    }

    /**
     * 上传um信息后台查询，返回建筑信息等，查询失败本地查询
     *
     * @param uuidMajor
     */
    private void serverQueryBuilding(final Map<String, Integer> uuidMajor) {
        //创建json
        JSONObject queryJson = new JSONObject();
        JSONArray umJsonArray = new JSONArray();
        try {
            for (String UM : uuidMajor.keySet()) {
                String arr[] = UM.split("\t");
                if (arr.length == 2) {
                    JSONObject umJson = new JSONObject();
                    umJson.put("uuid", arr[0]);
                    umJson.put("major", arr[1]);
                    umJson.put("size", uuidMajor.get(UM));
                    umJsonArray.put(umJson);
                }
            }
            queryJson.put("ums", umJsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = G.QUERYBUILD_URL + "token=" + key + "&type=ble&data=" + queryJson.toString();//查询url
       // LogDebug.w(TAG, "url" + url);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        final Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                serverQueryBuildingFail(uuidMajor);
            }

            @Override
            public void onResponse(Call call, final Response response) {
                if (response.code() == 200) {
                    //获取buildingcode
                    try {
                        JSONObject result = new JSONObject(response.body().string());
                        LogDebug.w(TAG, "result:" + result.toString());
                        int status = result.getInt("status");
                        if (status == 1) {

                            String buildingcode = result.getString("buildingCode");
                            downurl = result.getString("downURL");
                            //获取判断建筑的 UUID和major写入本地查询文件
                            loaclUpdateBuilding(buildingcode, result.getString("uuid") + "\t"
                                    + result.getString("major"));
                            //获取建筑的最新数据版本
                            serverVersion = result.getInt("version");

                            LogDebug.w(TAG, "buildingcode:" + buildingcode);
                            Message message = new Message();
                            message.what = BLELocateAction.ACTION_LOCATE_BUILD_SUCESS;
                            message.obj = buildingcode;
                            locateHandler.sendMessage(message);
                            G.buildingCode = buildingcode;
                            //查询到建筑后，调用定位方法
                            startLocatePostion(buildingcode, null, callback);
                            locateBuildingSuccess = true;
                        } else {
                            Message message = new Message();
                            message.what = IndoorLocationError.ERROR_HTTP_QUERY_BUILDING;
                            IndoorLocationError error = new IndoorLocationError(IndoorLocationError.ERROR_HTTP_QUERY_BUILDING
                                    , "后台定位建筑出错：" + result.getInt("status") + "——" + result.getString("message"));
                            message.obj = error;
                            locateHandler.sendMessage(message);
                        }
                    } catch (Exception e) {
                        serverQueryBuildingFail(uuidMajor);
                    }

                } else {
                    serverQueryBuildingFail(uuidMajor);
                }

            }
        });

    }

    /**
     * 后台查询失败后转为本地查询
     *
     * @param uuidMajor
     */
    private void serverQueryBuildingFail(final Map<String, Integer> uuidMajor) {
        //本地查询
        String buildingCode = loaclQueryBuilding(uuidMajor);
        if (buildingCode == null) {
            Message message = new Message();
            message.what = BLELocateAction.ACTION_TRANGLELOCATE_FAIL;
            message.obj = new IndoorLocationError(IndoorLocationError.ERROR_INTERNT_ERROR);
            locateHandler.sendMessage(message);
        } else {
            Message message = new Message();
            message.what = BLELocateAction.ACTION_LOCATE_BUILD_SUCESS;
            message.obj = buildingCode;
            locateHandler.sendMessage(message);
            locateBuildingSuccess = true;
            startLocatePostion(buildingCode, null, callback);
        }
    }

    /**
     * 更新定位数据文件。更新失败用本地数据
     *
     * @param buildingCode
     * @param serverVersion
     */
    private void updateModelData(final String buildingCode, final int serverVersion) {
        //加载beacon数据，如果不存在则从服务器下载
        final String beaconFile = buildingDataPath + "/" + this.buildingCode + "/modelbt.par";
        final File file = new File(beaconFile);
        if (downurl == null) {//2017年1月5日新增，防止指定建筑编码定位但本地无数据url为null的崩溃
            if (!file.exists()) {
                Message message = new Message();
                message.what = IndoorLocationError.ERROR_HASNOT_DATE;//没有写权限
                message.obj = new IndoorLocationError(IndoorLocationError.ERROR_HASNOT_DATE);
                locateHandler.sendMessage(message);
                return;
            } else startLocate(beaconFile);
        } else { //如果文件不存在或数据版本小于服务器，下载最新数据
            if (!file.exists() || isNeedUpdateBuilding(buildingCode, serverVersion)) {
                OkHttpClient okHttpClient = new OkHttpClient();
                final Request request = new Request.Builder().url(downurl).build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        updateModelDataFail(buildingCode);
                    }

                    @Override
                    public void onResponse(Call call, final Response response) {
                        if (response.code() == 200) {
                            try {
                                InputStream reader = response.body().byteStream();
                                String parentPath = buildingDataPath + "/" + BLELocateService.this.buildingCode;
                                File directory = new File(parentPath);
                                if (!directory.exists() && !directory.mkdir()) {
                                    Message message = new Message();
                                    message.what = IndoorLocationError.ERROR_SDCARD_WRITER_FORBIDDEN;//没有写权限
                                    message.obj = new IndoorLocationError(IndoorLocationError.ERROR_SDCARD_WRITER_FORBIDDEN);
                                    locateHandler.sendMessage(message);
                                } else {
                                    FileOutputStream writer = new FileOutputStream(beaconFile);
                                    byte[] buffer = new byte[1024];
                                    int len = 0;
                                    while ((len = reader.read(buffer)) != -1) {
                                        writer.write(buffer, 0, len);
                                        writer.flush();
                                    }
                                    writer.close();
                                    reader.close();
                                    SaveLoaclDataVersion(buildingCode, serverVersion);
                                    startLocate(beaconFile);
                                }
                            } catch (IOException e) {
                                updateModelDataFail(buildingCode);
                            }
                        } else {
                            updateModelDataFail(buildingCode);
                        }

                    }
                });
            } else {
                startLocate(beaconFile);
            }

        }


    }

    /**
     * 更新定位文件失败时先用本地数据定位，没有本地文件则返回网络错误
     *
     * @param buildingCode
     */
    private void updateModelDataFail(String buildingCode) {
        String beaconFile = buildingDataPath + "/" + this.buildingCode + "/modelbt.par";
        if (!new File(beaconFile).exists()) {
            Message message = new Message();
            message.what = IndoorLocationError.ERROR_DATE_ERROR;
            message.obj = IndoorLocationError.getMessage(IndoorLocationError.ERROR_DATE_ERROR);
            locateHandler.sendMessage(message);
        } else {
            startLocate(beaconFile);
        }
    }


    /**
     * 开始定位服务
     *
     * @param buildingCode 大厦编号
     * @param floor        楼层编号（手动定位楼层时，传入floor的值，否则传入null）
     * @param callback     回调接口
     */
    private void startLocatePostion(String buildingCode, String floor, BLELocateCallback callback) {
        this.buildingCode = buildingCode;
        G.buildingCode = buildingCode;
        this.callback = callback;
        //调用定位建筑接口已经初始化了，在此初始化报错
        //locateHandler = new BLELocateCallbackHandler(callback);
        //实例化定位消费者异步任务
        locateTask = new AsyncBLELocateTask(locateHandler);
        if (floor == null) {
            locateTask.setNeedJudgeFloor(true);
        } else {
            locateTask.setNeedJudgeFloor(false);
            locateTask.setFloor(floor);
        }
        updateModelData(buildingCode, serverVersion);

    }

    /**
     * 启动测试模式
     */
    public void enableTestMode() {
        enableTestMode = true;
    }

    public void inputTestData(String[] macList, String[] minorList, int[] rssList) {
        List<BeaconLocation> scanBeacons = new ArrayList<>(20);
        for (int i = 0; i < minorList.length; i++) {
            BeaconLocation beaconLocation = new BeaconLocation();
            String minorStr = minorList[i];
            beaconLocation.setMAC(macList[i]);
            beaconLocation.setMinor(Integer.parseInt(minorStr, 16));
            beaconLocation.setLevel(rssList[i]);
            scanBeacons.add(beaconLocation);
        }
        try {
            //生产数据(UI线程，无界队列，无阻塞放入)
            G.msgQueue.offer(scanBeacons);
        } catch (Exception e) {
            Message message = new Message();
            message.what = IndoorLocationError.ERROR_LOCATE_TOO_LONG;
            message.obj = new IndoorLocationError(IndoorLocationError.ERROR_LOCATE_TOO_LONG);
            locateHandler.sendMessage(message);
            e.printStackTrace();
        }
    }

    public void TestData(String[] macList, String[] minorList, int[] rssList) {
        List<BeaconLocation> scanBeacons = new ArrayList<>(20);
        for (int i = 0; i < minorList.length; i++) {
            BeaconLocation beaconLocation = new BeaconLocation();
            String minorStr = minorList[i];
            beaconLocation.setMAC(macList[i]);
            beaconLocation.setMinor(Integer.parseInt(minorStr, 16));
            beaconLocation.setLevel(rssList[i]);
            scanBeacons.add(beaconLocation);
        }
        try {
            //生产数据(UI线程，无界队列，无阻塞放入)
            G.msgQueue.offer(scanBeacons);
        } catch (Exception e) {
            Message message = new Message();
            message.what = IndoorLocationError.ERROR_LOCATE_TOO_LONG;
            message.obj = new IndoorLocationError(IndoorLocationError.ERROR_LOCATE_TOO_LONG);
            locateHandler.sendMessage(message);
            e.printStackTrace();
        }
    }

    /**
     * 初始化定位数据
     *
     * @param beaconFile
     */
    private void startLocate(String beaconFile) {
        String[] uuidList = new String[10];
        String[] majorList = new String[10];
        Integer size = new Integer(0);
        int result = LocateCore.init(beaconFile, uuidList, majorList, size);
        LogDebug.i(TAG,"LocateCore.init:" + result+" uuidList:" + Arrays.toString(uuidList)+" majorList:" + Arrays.toString(majorList));
        if (result == 0) {
            //初始化完毕，发送回调命令
            Message message = new Message();
            message.what = BLELocateAction.ACTION_INIT_SUCCESS;
            locateHandler.sendMessage(message);
            if (size > 10) {
                size = 10;
            }
            for (int i = 0; i < size; i++) {
                G.uuidMap.put(uuidList[i], true);
                G.majorMap.put(majorList[i], true);
            }
            firstScan = true;
            //启动消费者工作任务
            executorService = Executors.newSingleThreadExecutor();
            executorService.execute(locateTask);
            //启动蓝牙扫描
            startBeaconScan();
        } else {
            //初始化完毕，发送回调命令
            Message message = new Message();
            message.what = IndoorLocationError.ERROR_DATE_ERROR;
            message.obj = IndoorLocationError.getMessage(IndoorLocationError.ERROR_DATE_ERROR);
            locateHandler.sendMessage(message);
        }

    }

    private void startBeaconScan() {
        if (enableTestMode) {
            return;
        }
        brtBeaconManager.setRangingTime(TIME_SCAN_PRERIOD);
        G.msgQueue.clear();
        if(!bleScanFlag) {
            // 开始扫描
            brtBeaconManager.setBRTBeaconManagerListener(rangingListener);
            brtBeaconManager.startRanging();
            bleScanFlag = true;
            LogDebug.w(TAG, "开始扫描");
        }

        isServiceStarted = true;

    }


    /**
     * 停止定位导航
     */
    public void stopNavigation() {
        if (gpsensorManager != null && accFlag)
            gpsensorManager.stopCollectionACC();
        if (isServiceStarted) {
            // 停止扫描
            if (brtBeaconManager != null) {
                bleScanFlag = false;
                brtBeaconManager.stopRanging();
                brtBeaconManager.setBRTBeaconManagerListener(null);
                LogDebug.w(TAG,"停止定位");
                if (locateTask != null) {
                    locateTask.stopTask();
                    if (executorService != null) {
                        //很重要
                        executorService.shutdownNow();
                        executorService = null;
                    }
                }
            }

            isServiceStarted = false;
        }
//        if (bleLocateServiceInstance != null) {
//            bleLocateServiceInstance = null;
//        }
        loaclBuildingFileSave();
    }

    /**
     * 释放蓝牙定位对象，清理内存数据
     */
    public void destroyService() {
        bleLocateServiceInstance = null;
        gpsensorManager = null;
        LogDebug.i(TAG, "destroyService");
        scheduledExecutorService.shutdown();
        brtBeaconManager =null;
    }


    public static String getSDKVersion() {
        return SDK_VERSION;
    }

    /**
     * 获取手机的Mac地址，如果未获取到mac，则返回设备的IMEI码
     */
    private String getDeviceMac() {
        String deviceMac;
        WifiManager manager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (manager != null && !CheckPermissionUtils.IsAndroidM(ctx)) {
            deviceMac = manager.getConnectionInfo().getMacAddress();
        } else {
            TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            deviceMac = tm.getDeviceId();
        }
        return deviceMac;
    }


    private int getLocalDataVersion(String buildingCode) {
        File buildingInfoFile = new File(buildingDataPath + "/" + buildingCode + "/version.json");
        int localversion = 0;
        if (buildingInfoFile.exists()) {
            StringBuffer buildingInfo = new StringBuffer();
            String line = null;
            try {
                LineNumberReader reader = new LineNumberReader(new FileReader(buildingInfoFile));
                while ((line = reader.readLine()) != null) {
                    buildingInfo.append(line);
                }
                reader.close();
                JSONObject jsonObject = new JSONObject(buildingInfo.toString());
                localversion = jsonObject.getInt("dataVersion");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return localversion;
    }

    /**
     * 获取用户key
     */
    private String getBLApiKey(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            String msg = appInfo.metaData.getString("GP_LBSKEY");
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //保存本地数据版本信息
    private void SaveLoaclDataVersion(String buildingCode, int version) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(buildingDataPath + "/" + buildingCode + "/version.json");
            JSONObject json = new JSONObject();
            try {
                json.put("buildingCode", buildingCode);
                json.put("dataVersion", version);
                fileOutputStream.write((json + "\n").getBytes());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //是否需要更新本地数据
    private boolean isNeedUpdateBuilding(String buildingCode, int serverVersion) {
        if (serverVersion == -1) return false;
        if (getLocalDataVersion(buildingCode) == serverVersion) return false;
        else return true;
    }

    //读取本地缓存文件，存入HashMap
    private void loaclBuildingInit() {
        localBuildingsUMMap = new HashMap<>();
        File localFile = new File(localBuildingFilePath);
        try {
            if (localFile.exists()) {
                LineNumberReader reader = new LineNumberReader(new FileReader(localFile));
                String line = null;
                if ((line = reader.readLine()) != null) {
                    String arr[] = line.split("#");
                    if (arr.length == 2) {
                        localBuildingsUMMap.put(arr[0], arr[1]);// buildingcode um
                    }
                }
                reader.close();

            } else {
                LogDebug.w(TAG, localBuildingFilePath + " 创建文件");
                localFile.createNewFile();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //本地查询建筑编码
    private String loaclQueryBuilding(Map<String, Integer> uuidMajor) {
        int max = 0;
        String queryUM = null;
        for (String um : uuidMajor.keySet()) {
            if (uuidMajor.get(um) > max) {
                max = uuidMajor.get(um);
                queryUM = um;
            }
        }
        LogDebug.w(TAG, "query:" + queryUM);
        if (queryUM == null) return null;
        if (localBuildingsUMMap == null || localBuildingsUMMap.size() == 0) return null;
        for (String key : localBuildingsUMMap.keySet()) {
            if (localBuildingsUMMap.get(key).equals(queryUM)) {
                return key;
            }
        }
        return null;
    }

    //插入或更新建筑编码对应的UM
    private void loaclUpdateBuilding(String buildingCode, String um) {
        localBuildingsUMMap.put(buildingCode, um);
    }

    //保存
    private void loaclBuildingFileSave() {
        try {
            if (localBuildingsUMMap != null && localBuildingsUMMap.size() > 0) {
                FileOutputStream fileOutputStream = new FileOutputStream(localBuildingFilePath);
                for (String key : localBuildingsUMMap.keySet()) {
                    fileOutputStream.write((key + "#" + localBuildingsUMMap.get(key) + "\n").getBytes());
                }
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void sendMessage(int what) {
        Message message = new Message();
        message.what = what;
        message.obj = new IndoorLocationError(what);
        locateHandler.sendMessage(message);
    }

    /**
     * 获取应用包名
     *
     * @param ctx
     * @return
     */
    private static String getAppInfo(Context ctx) {
        try {
            String pkName = ctx.getPackageName();
            return pkName;
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 获取sd卡路径
     *
     * @return
     */
    private String getSDCardPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) { // 如果SD卡存在，则获取跟目录
            sdDir = Environment.getExternalStorageDirectory();
        } else {
            sdDir = Environment.getRootDirectory();// 如果没有SD卡，则存放于内存卡根目录
        }
        String path = sdDir.getPath();
        return path;
    }

}
