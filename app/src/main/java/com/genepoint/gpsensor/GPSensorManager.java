package com.genepoint.gpsensor;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

import com.genepoint.blelocate.LogDebug;

import java.math.BigDecimal;

/**
 * Created by jsj on 2016/10/20.
 */

public class GPSensorManager {
    private static final String TAG = "GPSensorManager";

    private static final float alpha = 0.8f;
  //  public static final int SCANMillis = 500;
    public static final int DATA_SIZE = 50;

    private Context ctx;
    public static SensorManager sensorManager = null;
    public static Sensor accelerometerSensor = null;//加速度
    public static Sensor magneticSensor=null;//磁场
    public static double accDatas[][] = new double[3][DATA_SIZE];

    private PowerManager.WakeLock mWakeLock;


    private float gravity[] = new float[3];
    private float linear_accelerometer[] = new float[3];

    float accValues[] = new float[3];
    float magValues[] = new float[3];
    float r[] = new float[9];
    float values[] = new float[3];
    private  static float orientation=-1.0f;

    /**
     * 单例模式
     */
    private static GPSensorManager gpSensorManagerInstance = null;


    private int index = 0;
    private boolean symbol = false;

    private GPSensorManager(Context context) {
        this.ctx = context;
        PowerManager manager = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        mWakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);// CPU保存运行
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);// 屏幕熄掉后依然运行
        sensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);

        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor=sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    public static GPSensorManager getInstance(Context ctx) {
        if (gpSensorManagerInstance == null) {
            gpSensorManagerInstance = new GPSensorManager(ctx);
            if ((sensorManager == null) || (accelerometerSensor == null)) {
                return null;
            }
        }
        return gpSensorManagerInstance;
    }

    public void startCollectionACC() {
        symbol = true;
        index = 0;
        sensorManager.registerListener(myAccelerometerListener, accelerometerSensor,
                SensorManager.SENSOR_DELAY_FASTEST);//50HZ
        sensorManager.registerListener(myAccelerometerListener,magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void stopCollectionACC() {
        if (myAccelerometerListener != null) {
            sensorManager.unregisterListener(myAccelerometerListener);
        }
        symbol = false;
        index = 0;

    }

    /*
    * SensorEventListener接口的实现，需要实现两个方法
    * 方法1 onSensorChanged 当数据变化的时候被触发调用
    * 方法2 onAccuracyChanged 当获得数据的精度发生变化的时候被调用，比如突然无法获得数据时
    * */
    final SensorEventListener myAccelerometerListener = new SensorEventListener() {


        //传感器输出变化响应函数
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accValues = sensorEvent.values.clone();
                //低通滤波滤除重力影响
                gravity[0] = alpha * gravity[0] + (1 - alpha) * accValues[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * accValues[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) *accValues[2];

                linear_accelerometer[0] =accValues[0] - gravity[0];
                linear_accelerometer[1] =accValues[1] - gravity[1];
                linear_accelerometer[2] = accValues[2] - gravity[2];

                //控制显示精度
                float accX = round(linear_accelerometer[0], 3, BigDecimal.ROUND_HALF_UP);
                float accY = round(linear_accelerometer[1], 3, BigDecimal.ROUND_HALF_UP);
                float accZ = round(linear_accelerometer[2], 3, BigDecimal.ROUND_HALF_UP);

                //数组写入
                if (symbol == true) {
                    accDatas[0][index] = (double) accX;
                    accDatas[1][index] = (double) accY;
                    accDatas[2][index] = (double) accZ;
                    index++;
                }

                if (index == 49) {
                    index = 0;
                    new Thread( new AsyncMotionGoTask(accDatas)).start();
                }
            }else if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magValues = sensorEvent.values.clone();
                SensorManager.getRotationMatrix(r, null, accValues, magValues);
                SensorManager.getOrientation(r, values);
                // 注意：用（磁场+加速度）得到的数据范围是（-180～180）,也就是说，0表示正北，90表示正东，180/-180表示正南，-90表示正西。
                orientation = (float) Math.toDegrees(values[0]);
                if(orientation<0){
                    orientation=orientation+360;
                }
            //    LogDebug.w(TAG,"orientation:"+orientation+"--"+values[0]);
            }
        }


        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    public static float round(float value, int scale, int roundingMode) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(scale, roundingMode);
        float f = bigDecimal.floatValue();
        return f;
    }
    public static float getOrientation(){
        return orientation;
    }


}
