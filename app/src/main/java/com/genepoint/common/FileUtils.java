package com.genepoint.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;

import android.content.Context;
import android.os.Environment;

public class FileUtils {
	/**  
     * 复制单个文件  
     * @param oldPath String 原文件路径 如：c:/fqf.txt  
     * @param newPath String 复制后路径 如：f:/fqf.txt  
     * @return boolean  
     */    
   public static void copyFile(String oldPath, String newPath) {   
       try {   
           int bytesum = 0;   
           int byteread = 0;   
           File oldfile = new File(oldPath);   
           if (oldfile.exists()) { //文件存在时 
               InputStream inStream = new FileInputStream(oldPath);  //读入原文件 
               FileOutputStream fs = new FileOutputStream(newPath);   
               byte[] buffer = new byte[4096];
               int length;   
               while ( (byteread = inStream.read(buffer)) != -1) {   
                   bytesum += byteread; //字节数 文件大小
                   fs.write(buffer, 0, byteread);   
               }   
               inStream.close(); 
               fs.close();
           }   
       }   
       catch (Exception e) {   
           System.out.println("复制单个文件操作出错");   
           e.printStackTrace();   
  
       }   
  
   }   
   /**  
    * 复制整个文件夹内容  
    * @param oldPath String 原文件路径 如：c:/fqf  
    * @param newPath String 复制后路径 如：f:/fqf/ff  
    * @return boolean  
    */   
   public static void copyFolder(String oldPath, String newPath) {   
  
       try {   
           (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹   
           File a=new File(oldPath);   
           String[] file=a.list();   
           File temp=null;   
           for (int i = 0; i < file.length; i++) {   
               if(oldPath.endsWith(File.separator)){   
                   temp=new File(oldPath+file[i]);   
               }   
               else{   
                   temp=new File(oldPath+File.separator+file[i]);   
               }   
  
               if(temp.isFile()){   
                   FileInputStream input = new FileInputStream(temp);   
                   FileOutputStream output = new FileOutputStream(newPath + "/" +   
                           (temp.getName()).toString());   
                   byte[] b = new byte[1024 * 5];   
                   int len;   
                   while ( (len = input.read(b)) != -1) {   
                       output.write(b, 0, len);   
                   }   
                   output.flush();   
                   output.close();   
                   input.close();   
               }   
               if(temp.isDirectory()){//如果是子文件夹
                   copyFolder(oldPath+"/"+file[i],newPath+"/"+file[i]);   
               }   
           }   
       }   
       catch (Exception e) {   
           System.out.println("复制整个文件夹内容操作出错");   
           e.printStackTrace();   
  
       }   
  
   }  
   /**
	 * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty(String input) {
		if (input == null || "".equals(input))
			return true;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
				return false;
			}
		}
		return true;
	}
	
	/**读取文本行数
	 * @param path 文本路径
	 * @return 行数
	 */
	public static int getlinenumfrontxt(String path){
		int lines = 0;
		File test= new File(path); 
		long fileLength = test.length(); 
		LineNumberReader rf = null; 
		try { 
		rf = new LineNumberReader(new FileReader(test)); 
		if (rf != null) { 		 
		rf.skip(fileLength); 
		lines = rf.getLineNumber(); 
		rf.close(); 
		} 
		} catch (IOException e) { 
		if (rf != null) { 
		try { 
		rf.close(); 
		} catch (IOException ee) { 
		} 
		
		} 
		}
		return lines;
	}

	 /**删除整个目录
	 * @param path 目录路径
	 * @return 
	 */
	public static boolean deleteDir(String path){
         boolean success = true ;
         File file = new File(path) ;
         if(file.exists()){
                 File[] list = file.listFiles() ;
                 if(list != null){
                         int len = list.length ;
                         for(int i = 0 ; i < len ; ++i){
                                 if(list[i].isDirectory()){
                                         deleteDir(list[i].getPath()) ;
                                 } else {
                                         boolean ret = list[i].delete() ;
                                         if(!ret){
                                                 success = false ;
                                         }
                                 }
                         }
                 }
         } else {
                 success = false ;
         }
         if(success){
                 file.delete() ;
         }
         return success ;
 }
	 /** 获取手机内存目录 
	 * @return 
	 */
	public static String getAppPath() {
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
	// 删除目录下所有文件
		public  static void deleteAllFiles(File root) {
			File files[] = root.listFiles();
			if (files != null)
				for (File f : files) {
					if (f.isDirectory()) { // 判断是否为文件夹
						deleteAllFiles(f);
						try {
							f.delete();
						} catch (Exception e) {
						}
					} else {
						if (f.exists()) { // 判断是否存在
							deleteAllFiles(f);
							try {
								f.delete();
							} catch (Exception e) {
							}
						}
					}
				}
		}
}
