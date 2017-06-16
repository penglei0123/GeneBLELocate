package com.genepoint.blelocate;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.LocationManager;
import android.os.Build;

public class CheckPermissionUtils {
    /**
     * 手机android版本是否大于=6.0
     * @param context
     * @return
     */
	public static boolean IsAndroidM(Context context){
		if(Build.VERSION.SDK_INT>=23)return true;
		return false;
	}
    /**
     * 手机android版本是否大于18
     * @param context
     * @return
     */
    public static boolean IsEnaableBLE(Context context){
        if(Build.VERSION.SDK_INT>=18)return true;
        return false;
    }
	//获取权限清单代码：
	public static void getPermissionList(Context context){
		try {  
			PackageManager pm = context.getPackageManager();
            PackageInfo pack = pm.getPackageInfo("packageName", PackageManager.GET_PERMISSIONS);
            String[] permissionStrings = pack.requestedPermissions;
            //showToast("权限清单--->" + permissionStrings.toString());  
        } catch (NameNotFoundException e) {
            e.printStackTrace();  
        }
		
	}

	  /** 
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的 
     * @param context 
     * @return true 表示开启 
     */  
    public static final boolean isOPen(final Context context) {
        LocationManager locationManager
                                 = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）  
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）  
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {  
            return true;  
        }  
  
        return false;  
    }

   
}
