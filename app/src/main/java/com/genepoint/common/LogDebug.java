package com.genepoint.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogDebug {
	private static boolean isDebug = false;
	private static boolean isBackups = true;//备份采集信息
	/**
	 * 是否输出日志文件
	 */
	private static boolean isLogFile = false;
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
	private static String logFilePath = Environment.getExternalStorageDirectory() + "/0log/nimapcollection_log_"
			+ simpleDateFormat.format(new Date()) + ".txt";
	

	public static void d(String tag, String msg) {
		if (isDebug) {
			Log.d(tag, msg);
			writeToLog(tag + "\t" + msg);
		}
	}

	public static void i(String tag, String msg) {
		if (isDebug) {
			Log.i(tag, msg);
			writeToLog(tag + "\t" + msg);
		}
	}

	public static void w(String tag, String msg) {
		if (isDebug) {
			Log.w(tag, msg);
			writeToLog(tag + "\t" + msg);
		}
	}

	public static void e(String tag, String msg) {
		if (isDebug) {
			Log.e(tag, msg);
			writeToLog(tag + "\t" + msg);
		}
	}
	public static void writeToLog(String logStr) {
		if (isLogFile) {
			File logFile = new File(logFilePath);
			if(!logFile.getParentFile().exists()){
				logFile.getParentFile().mkdir();
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(logFilePath, true);
				fos.write((System.currentTimeMillis() + "\t" + logStr + "\n").getBytes("utf-8"));
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
		}
	}
	public static void writebackups(String data,String path) {
		if (isBackups) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(path, true);
				fos.write((data + "\n").getBytes("utf-8"));
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
		}
	}
}
