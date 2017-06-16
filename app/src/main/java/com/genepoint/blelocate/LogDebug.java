package com.genepoint.blelocate;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogDebug {
    /**
     * 是否是debug模式
     */
    private static boolean isDebug = true;

    /**
     * 是否输出日志文件
     */
    private static boolean isLogFile = true;

    //日志队列，周期轮询持久化
    public static Queue<String> logQueue = new ConcurrentLinkedQueue<>();

    /**
     * 设置是否是debug模式，是的话，就会输出sdk的log信息
     *
     * @param debug
     */
    public static void setIsDebug(boolean debug) {
        isDebug = debug;
    }

    /**
     * 设置是否输出SDK的log信息到文件
     *
     * @param isLogToFile
     */
    public static void setIsLogFile(boolean isLogToFile) {
        isLogFile = isLogToFile;
    }

    public static boolean getIsDebug() {
        return isDebug;
    }

    public static boolean getIsLogFile() {
        return isLogFile;
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
            writeToLog(tag + "\t" + msg);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
            writeToLog(tag + "\t" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.i(tag, msg);
            writeToLog(tag + "\t" + msg);
        }
    }

    public static void v(String tag, String msg) {
        if (isDebug) {
            Log.v(tag, msg);
            writeToLog(tag + "\t" + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, msg);
            writeToLog(tag + "\t" + msg);
        }
    }

    public static void writeToLog(String logStr) {
        if (isLogFile) {
            logQueue.offer(logStr);
        }
    }

    /**
     * 清理日志文件（默认7天前）
     */
    static {
        File file = new File(Environment.getExternalStorageDirectory() + "/0log/");
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                long now = System.currentTimeMillis();
                for (File logFile : files) {
                    if ((now - logFile.lastModified()) > 86400 * 1000 * 7) {
                        logFile.delete();
                    }
                }
            }
        } else {
            file.mkdirs();
        }
    }
}

class AsyncPersistenceLogTask implements Runnable {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
    private static SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static String logFilePath = Environment.getExternalStorageDirectory() + "/0log/BLELocateSdk_log_"
            + simpleDateFormat2.format(new Date()) + ".txt";
    private volatile boolean canceled = false;

    public void run() {
        List<String> cacheList = new ArrayList<>();
        while (!LogDebug.logQueue.isEmpty()) {
            cacheList.add(LogDebug.logQueue.poll());
        }
        if (cacheList.size() > 0) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(logFilePath, true);
                for (String logStr : cacheList) {
                    fos.write((simpleDateFormat.format(new Date()) + "\t" + logStr + "\n").getBytes("utf-8"));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            cacheList.clear();
        }
    }
}
