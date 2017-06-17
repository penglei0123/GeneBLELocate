package com.genepoint.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class Helper {

	/**
	 * ��ѹ������ѹ������·��
	 */
	public static void loadZip(InputStream inputStream, String path) {
		File tmp = new File(path);
		if (!tmp.exists())
			tmp.mkdirs();

		try {
			// ������
			ZipInputStream zipInputStream = new ZipInputStream(inputStream);
			// ��ȡһ�������
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			// ʹ��1Mbuffer
			byte[] buffer = new byte[1024 * 1024];
			// ��ѹʱ�ֽڼ���
			int count = 0;
			// ��������Ϊ��˵���Ѿ�����������ѹ�������ļ���Ŀ¼
			while (zipEntry != null) {
				// �����һ��Ŀ¼
				if (zipEntry.isDirectory()) {
					tmp = new File(path + File.separator + zipEntry.getName());
					tmp.mkdir();
				} else {
					// ������ļ�
					tmp = new File(path + File.separator + zipEntry.getName());
					// �������ļ�
					tmp.createNewFile();
					FileOutputStream fileOutputStream = new FileOutputStream(
							tmp);
					while ((count = zipInputStream.read(buffer)) > 0) {
						fileOutputStream.write(buffer, 0, count);
					}
					fileOutputStream.close();
				}
				zipEntry = zipInputStream.getNextEntry();
			}
			zipInputStream.close();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static boolean isConnect(Context context) {
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				// 获取网络连接管理的对象
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					// 判断当前网络是否已经连接
					if ((info.getState() == NetworkInfo.State.CONNECTED)) {						
						return true;
					}
				}
			}
		} catch (Exception e) {
			Log.v("error", e.toString());
		}
		return false;
	}
	/*@author sichard
    * @category 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
    * @return
    */ 
   public static boolean ping() {    
       String result = null; 
       try { 
               String ip = "http://www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网 
               LogDebug.w("PL", "start  ping");
               Process p = Runtime.getRuntime().exec("ping -n 2 -w 2 " + ip);// ping网址3次 
               LogDebug.w("PL", "end  ping");
               // 读取ping的内容，可以不加 
               InputStream input = p.getInputStream(); 
               BufferedReader in = new BufferedReader(new InputStreamReader(input)); 
               StringBuffer stringBuffer = new StringBuffer(); 
               String content = ""; 
               while ((content = in.readLine()) != null) { 
                       stringBuffer.append(content); 
               } 
               LogDebug.w("PL", "ping.....result content : " + stringBuffer.toString()); 
               // ping的状态 
               int status = p.waitFor(); 
               if (status == 0) { 
                       result = "success"; 
                       return true; 
               } else { 
                       result = "failed"; 
               } 
       } catch (IOException e) { 
               result = "IOException"; 
       } catch (InterruptedException e) { 
               result = "InterruptedException"; 
       } finally { 
               LogDebug.w("PL","ping..result = " + result); 
       } 
       return false;
   }
}
