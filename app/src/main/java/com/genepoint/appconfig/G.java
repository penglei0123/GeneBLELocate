package com.genepoint.appconfig;

import java.io.File;


import android.os.Environment;

public class G {

	public static String token = "342B7A1D8481473CA1845A00533C1D4C-1669023F3145C153D50E03A739979414";

	//上传url
	public static String UPLOAD_SERVER_URL="http://location.gene-point.com/platform/developer/upload?action=upload_collect_data&user=siweituxin";	
	// 指纹urlַ
	public static String FINGER_SERVER_URL = "http://location.gene-point.com/platform/";
	public static String mSavePath ;//指纹存放路径
	public static String mZipSuffix = ".zip";//压缩后缀名
	public static  String APPPath=getAppPath()+"/GPBLELocateDemo/";//app存放路径

	public static String userToken = "B7C24BE031EB4B54A9960BC9BF1E03F3-21232F297A57A5A743894A0E4A801FC3";//万能token
	//获取下载矢量数据url链接的请求url
	public static String getDataurl = "http://location.gene-point.com/platform/developer/download"+"?action=download_original_data&buildingCode=";
	//下载指纹的请求url
	public static String getFingerURL = "http://location.gene-point.com/platform/"+ "downloadFingerprintData?buildingCode="; 
	
	// 存放gis地图数据的路径
		public final static String GISMAPDATA_PATH = G.APPPath+"/mapdata/Gis";
	/**
	 * app目录结构
	 */
	public static void makeDir(){

		File dir3 = new File(APPPath+ "/DATA/");
		if (!dir3.exists()) {
			dir3.mkdirs();
		}
	}
	/** 获取手机内存目录路径
	 * @return
	 */
	public static  String getAppPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) { // 如果SD卡存在，则获取跟目录
			sdDir = Environment.getExternalStorageDirectory();
		} else {
			sdDir = Environment.getRootDirectory();// 如果没有SD卡，则存放于内存卡根目录
		}
		String path = sdDir.getPath();
		return path;
	}

}
