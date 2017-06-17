package com.genepoint.geneblelocate;

import com.genepoint.blelocate.BeaconLocation;
import com.genepoint.blelocate.G;
import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;

public class MyApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		//Bugly
	//	CrashReport.initCrashReport(getApplicationContext(), "900017049", false);
		CrashReport.initCrashReport(getApplicationContext(), "138b1f302e", true);
		
		loadBeaconsLocation();
	}
	
	private void loadBeaconsLocation(){
		
		G.beaconsLocation.put("28337", new BeaconLocation(28337,360.000000,343.000000));
		G.beaconsLocation.put("28339", new BeaconLocation(28339,461.000000,341.000000));
		G.beaconsLocation.put("28334", new BeaconLocation(28334,561.000000,344.000000
));
		G.beaconsLocation.put("28344", new BeaconLocation(28344,809.000000 ,97.000000
));
		G.beaconsLocation.put("28347", new BeaconLocation(28347,717.000000 ,103.000000
));
		G.beaconsLocation.put("28336", new BeaconLocation(28336,737.000000 ,185.000000
));
		G.beaconsLocation.put("28335", new BeaconLocation(28335,614.000000 ,183.000000
));
		G.beaconsLocation.put("28338", new BeaconLocation(28338,466.000000 ,185.000000
));
		G.beaconsLocation.put("28342", new BeaconLocation(28342,345.000000 ,184.000000
));
		G.beaconsLocation.put("28343", new BeaconLocation(28343,257.000000 ,184.000000
));
		G.beaconsLocation.put("28346", new BeaconLocation(28346,158.000000,81.000000
));
		G.beaconsLocation.put("28345", new BeaconLocation(28345,141.000000,216.000000
));
	}
}
