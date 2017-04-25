package com.genepoint.geneblelocate;

import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;

public class MyApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		//Bugly
	//	CrashReport.initCrashReport(getApplicationContext(), "900017049", false);
		CrashReport.initCrashReport(getApplicationContext(), "138b1f302e", true);
	}
}
