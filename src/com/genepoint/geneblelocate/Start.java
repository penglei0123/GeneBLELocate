package com.genepoint.geneblelocate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.genepoint.appconfig.G;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 *
 * @version 1.0
 * @author Administrator
 *
 */
public class Start extends Activity {
	private static final String TAG = "AppStart";	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		G.makeDir();
		// 将Gis地图工具解压到内存卡，地图文件结构见内存卡上mapdata目录
		try {
			InputStream stream = this.getResources().getAssets().open("mapdata.zip");
				         String outFileName = G.APPPath+ "/mapdata.zip";
				          // 判断目录是否存在。如不存在则创建一个目录
				          File file = new File(G.APPPath);
				          if (!file.exists()) {
				              file.mkdirs();
				          }
				          OutputStream myOutput = new FileOutputStream(outFileName);
				          // transfer bytes from the inputfile to the outputfile130
				          byte[] buffer = new byte[1024];
				          int length;
				          while ((length = stream.read(buffer)) > 0) {
				              myOutput.write(buffer, 0, length);
				          }
				          // Close the streams136
				          myOutput.flush();
				          myOutput.close();
				         stream.close();
				     unZipFiles(new File(outFileName), G.APPPath);
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			return;
		}
		redirectTo();

	}

	private void redirectTo() {
		Intent intent = new Intent(this, LocateActivity.class);
		startActivity(intent);
		finish();
	}
	/**
	 * 解压文件到指定目录
	 * @param zipFile
	 * @param descDir
	 * @author isea533
	 */
	@SuppressWarnings("rawtypes")
	public static void unZipFiles(File zipFile,String descDir)throws IOException{
		File pathFile = new File(descDir);
		if(!pathFile.exists()){
			pathFile.mkdirs();
		}
		ZipFile zip = new ZipFile(zipFile);
		for(Enumeration entries = zip.getEntries();entries.hasMoreElements();){
			ZipEntry entry = (ZipEntry)entries.nextElement();
			String zipEntryName = entry.getName();
			InputStream in = zip.getInputStream(entry);
			String outPath = (descDir+zipEntryName).replaceAll("\\*", "/");;
			//判断路径是否存在,不存在则创建文件路径
			File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
			if(!file.exists()){
				file.mkdirs();
			}
			//判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
			if(new File(outPath).isDirectory()){
				continue;
			}
			
			OutputStream out = new FileOutputStream(outPath);
			byte[] buf1 = new byte[1024];
			int len;
			while((len=in.read(buf1))>0){
				out.write(buf1,0,len);
			}
			in.close();
			out.close();
			}
	}
	
}

