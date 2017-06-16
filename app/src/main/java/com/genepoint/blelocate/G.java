package com.genepoint.blelocate;

import com.genepoint.blelocate.core.LocPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by jd on 2016/1/19.
 */
public class G {
    /**
     * 使用minor信息作为beacon标识符
     */
    public static Map<String, BeaconLocation> beaconsLocation = new HashMap<>();

    /**
     * 生产者消费者协作队列
     */
    public static BlockingQueue<List<BeaconLocation>> msgQueue = new LinkedBlockingQueue<>(1000);

    /**
     * 定位结果缓存队列
     */
    public static Queue<LocPoint> locPointQueue = new ConcurrentLinkedQueue<>();

    public static String uploadURL = "http://location.gene-point.com:8082/";
    /**
     * 全局保存建筑编码
     */
    public static String buildingCode = null;

    public static String QUERYBUILD_URL = "http://location.gene-point.com/platform/api/buildings/location?";

    public static Map<String, Boolean> uuidMap = new HashMap<>();
    public static Map<String, Boolean> majorMap = new HashMap<>();

}
