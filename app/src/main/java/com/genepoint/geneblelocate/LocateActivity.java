package com.genepoint.geneblelocate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.genepoint.appconfig.G;
import com.genepoint.blelocate.BLELocateCallback;
import com.genepoint.blelocate.BLELocateService;
import com.genepoint.blelocate.BeaconLocation;
import com.genepoint.blelocate.IndoorLocationError;
import com.genepoint.blelocate.core.LocPoint;
import com.genepoint.common.PointUtils;
import com.genepoint.datapack.DragZoomImageSurfaceView;
import com.genepoint.datapack.FloorAdapter;
import com.navinfo.nimapapi.building.Floor;
import com.navinfo.nimapapi.geometry.GeoPoint;
import com.navinfo.nimapapi.geometry.Mark;
import com.navinfo.nimapapi.jni.JniUtil;
import com.navinfo.nimapapi.map.MapMoveListener;
import com.navinfo.nimapapi.map.MapRenderEventListener;
import com.navinfo.nimapapi.map.MapSelectedListener;
import com.navinfo.nimapapi.map.MapView;

import android.R.bool;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract.AggregationExceptions;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LocateActivity extends Activity implements OnClickListener {
	private DragZoomImageSurfaceView mapSurfaceView;
	public static String RadioPath; // 处理完成后的指纹数据存储路径
	private BLELocateService bleLocateService = null;
	// 地图相关
	private MapView mapView; // 地图对象
	private Mark markLocation;// 定位图标
	private int markLocationId = 412345;

	private ImageView btnLocate;
	private FloorAdapter adapter;

	private Button floorbtn;// 显示或隐藏floorlist
	private ListView floorlistV;//
	private TranslateAnimation showAni, HideAni;// list动画

	private boolean isLocating = false;
	private TextView rView;
	private Context mContext;
	private String curBuildingCode = null;
	private String TAG = "LocateActivity";
	private boolean isAnimation=false;
	private boolean surVIewFlag=false;
	
	private int  mapType=-1;//0 四维  1jpg

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	this.requestWindowFeature(Window.FEATURE_NO_TITLE); // 去掉标题栏
		setContentView(R.layout.activity_locate);
		mContext = getApplicationContext();
		initView();
		
		mapView.setVisibility(View.GONE);
		bleLocateService = BLELocateService.getInstance(mContext);
		bleLocateService.setDirectionOptimizationEnable(true,false);
	}

	@Override
	protected void onResume() {
		mapSurfaceView = (DragZoomImageSurfaceView) findViewById(R.id.sfv_map);
		mapSurfaceView.setVisibility(View.GONE);
		surVIewFlag=false;
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (bleLocateService != null) {
			stopLoc();
			bleLocateService.destroyService();
		}
		super.onDestroy();
	}

	@Override
	public void finish() {
		mapView.removeAllViews();
		mapView.closeMap();
		super.finish();
	}

	/**
	 * 切换楼层监听器
	 */
	private OnItemClickListener floorlistListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			List<Floor> floors = mapView.getCurBuilding().getFloors();
			String floor = floors.get(position).getName();
			mapView.getCurBuilding().setCurFloor(floor);
			adapter.setCurrentFloor(floor);
			floorbtn.setText(floor);

		}
	};

	private void initView() {		
	
		rView=(TextView)findViewById(R.id.result);
		floorlistV = (ListView) findViewById(R.id.floorlist);
		floorlistV.setOnItemClickListener(floorlistListener);
		floorbtn = (Button) findViewById(R.id.showfloorbtn);
		floorbtn.setOnClickListener(this);
		floorbtn.getBackground().setAlpha(200);
		mapView = (MapView) findViewById(R.id.mapview_indoor);
		btnLocate = (ImageView) findViewById(R.id.location);
		btnLocate.setOnClickListener(this);
		// 设置定位图标，注意图片资源是四维app中的
		markLocation = new Mark();
		markLocation.setFID(markLocationId);
		// markLocation.setImageName("nav_cur_location.png");
		markLocation.setImageName("position_logo.png");
		markLocation.setIconWidth(100);
		markLocation.setIconHeight(100);
		HideAni = new TranslateAnimation(1, 0.0F, 1, 0.0F, 1, 0.0F, 1, -1.0F);
		HideAni.setDuration(500);
		showAni = new TranslateAnimation(1, 0.0F, 1, 0.0F, 1, -1.0F, 1, 0.0F);
		showAni.setDuration(500);
		HideAni.setAnimationListener(floorHide);
	}

	private void initMap(String buildingCode) {
		String mapPath = G.GISMAPDATA_PATH + "/" + buildingCode + "/index.dat";
		File mapFile = new File(mapPath);
		if (!mapFile.exists()) {
			Toast.makeText(this, "地图文件不存在！", Toast.LENGTH_LONG).show();
			return;
		}
		// 打开地图数据
		int openMapResult = mapView.openMap(mapPath, "");
		if (openMapResult < 0) {
			Toast.makeText(this, "地图文件错误！", Toast.LENGTH_LONG).show();
			return;
		}
		// 监听地图点击事件
		mapView.setOnMapSelectedListener(new MapSelectedListener() {

			@Override
			public void onSelectedGeometry(float arg0, float arg1) {

			}
		});
		// 监听地图滑动事件
		mapView.setOnMapMoveListener(new MapMoveListener() {

			@Override
			public void onZoom(float arg0) {

			}

			@Override
			public void onMove(MotionEvent arg0) {

			}
		});

		JniUtil.setCreateLink(true);// 显示路径
		JniUtil.setShowLink(true);

		mapView.setmMapRenderEventListener(mapRenderEventListener);
		// 禁止旋转
		mapView.getMapController().setRotationGesturesEnabled(false);
		JniUtil.setCreateLink(true);
		JniUtil.setShowLink(true);

		// 刷新地图
		mapView.setOpened(true);
		mapView.setRenderFirst(true);
		mapView.refresh();
	}

	private AnimationListener floorHide = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			floorlistV.setVisibility(View.GONE);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.showfloorbtn:
			if (floorlistV.getVisibility() == View.VISIBLE) {
				// 如果楼层处于显示状态，掩藏
				floorlistV.startAnimation(HideAni);
			} else {
				// 否则展示
				floorlistV.setVisibility(View.VISIBLE);
				floorlistV.startAnimation(showAni);
			}
			break;
		case R.id.location:
			if (!isLocating) {
				startLoc();

			} else {
				stopLoc();

			}
			break;
		default:
			break;
		}
	}

	/**
	 * 画定位标记
	 * 
	 * @param floor
	 * @param x
	 * @param y
	 */
	private void drawPointInMap(String floor, double x, double y) {
		if (mapView == null || mapView.getCurBuilding() == null) {
			Toast.makeText(this, "地图未加载成功！", Toast.LENGTH_LONG).show();
			return;
		}		
		if (!mapView.getCurBuilding().getCurFloor().getName().equals(floor)){
			mapView.getCurBuilding().setCurFloor(floor);
		}
		floorbtn.setText(floor);		
		adapter.setCurrentFloor(floor);
		mapView.getOverLayer().deleteMark(markLocationId);
		// 经纬度转墨卡托坐标
		GeoPoint tmpPoint = PointUtils.LonLat2Mercator(new PointUtils(x, y));
		markLocation.setGeoPoint(tmpPoint);
		mapView.getOverLayer().addMark(markLocation);
		mapView.refresh();
	}

	MapRenderEventListener mapRenderEventListener = new MapRenderEventListener(mapView) {
		@Override
		public void beforeRenderEvent() {
		}

		@Override
		public void afterRenderEvent() {
			return;
		}
	};

	// 停止定位
	public void stopLoc() {
		isLocating = false;
		btnLocate.clearAnimation();
		btnLocate.setImageDrawable(getResources().getDrawable(R.drawable.location));
		bleLocateService.stopNavigation();
	}

	// 开始定位
	public void startLoc() {
		isLocating = true;
		Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.locationrotate);
		btnLocate.setImageDrawable(getResources().getDrawable(R.drawable.nav_image_progress));
		btnLocate.startAnimation(animation);
		isAnimation=true;
		bleLocateService.startNavigation(null, callback);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {		
			mapView.removeAllViews();
			mapView.closeMap();
			
			finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            ActivityManager am = (ActivityManager) getSystemService(Service.ACTIVITY_SERVICE);
            am.killBackgroundProcesses(getPackageName());
            System.exit(0);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	private BLELocateCallback callback = new BLELocateCallback() {
 
		@Override
		public void onFail(IndoorLocationError arg0) {
			Log.e(TAG, "定位失败:" + arg0.errorCode + arg0.getErrorMsg());
			if(arg0.errorCode==304||arg0.errorCode==305||arg0.errorCode==306||arg0.errorCode==307){
				stopLoc();
				
			}
				
			rView.setText("定位失败:" + arg0.errorCode + arg0.getErrorMsg());
		}

		@Override
		public void onInitFinish() {
			Log.i(TAG, "定位初始化完成");
		}

		@Override
		public void onLocateBuildingSuccess(String arg0) {
			if(arg0.equals("B_0101_153AC5FD0DF")){					
				mapType=1;
				adapter = new FloorAdapter(mContext, new String[]{});	
				floorlistV.setAdapter(adapter);	
			}else if(arg0.equals("B_0101_15571AD5636")){			
				mapType=1;
				adapter = new FloorAdapter(mContext, new String[]{});		
				floorlistV.setAdapter(adapter);	
			}else if(arg0.equals("41101")){			
				mapType=1;
				adapter = new FloorAdapter(mContext, new String[]{});		
				floorlistV.setAdapter(adapter);	
			}else if(arg0.equals("124")||arg0.equals("125")){
				mapView.setVisibility(View.VISIBLE);
				mapSurfaceView.setVisibility(View.GONE);
				mapType=0;
				if (curBuildingCode == null || !curBuildingCode.equals(arg0)) {
					curBuildingCode=arg0;
					mapView.cleanScene();
					//if(arg0.equals("B_0000_1585CC48F62"))
						if(arg0.equals("124"))
						initMap("110005");		
					//	if(arg0.equals("B_0000_1585CC82894"))
						if(arg0.equals("125"))
					initMap("110087");	
					List<Floor> list = mapView.getCurBuilding().getFloors();
					String[] strName = new String[list.size()];
					for (int i = 0; i < list.size(); i++) {
						strName[i] = list.get(i).getName();
					}
					adapter = new FloorAdapter(mContext, strName);		
					floorlistV.setAdapter(adapter);					
				}
				
			}			
			Log.i(TAG, "定位建筑成功：" + arg0);
		}

		@Override
		public void onSuccess(LocPoint point) {
			if(isAnimation){
			btnLocate.clearAnimation();
			btnLocate.setImageDrawable(getResources().getDrawable(R.drawable.locating));			
			isAnimation=false;
			}
			Log.i(TAG, "定位结果：" + point.Floor + point.Xcor + "  " + point.Ycor);
			rView.setText("定位结果：" +  String.format("%.6f", point.Xcor)+ "," +  String.format("%.6f", point.Ycor));
			if(mapType==0){
			drawPointInMap(point.Floor, point.Xcor, point.Ycor);
			surVIewFlag=false;
			}
			if(mapType==1){
				mapView.cleanScene();
				mapView.setVisibility(View.GONE);
				if(!surVIewFlag){				
				mapSurfaceView=(DragZoomImageSurfaceView) findViewById(R.id.sfv_map);
				mapSurfaceView.loadBackGround(R.drawable.map_ground);
				mapSurfaceView.loadPositionLogo(R.drawable.position_logo);				
				surVIewFlag=true;
				}
				mapSurfaceView.setVisibility(View.VISIBLE);
				if(openMap(point.building, point.Floor)){
					if(point.building.equals("B_0101_15571AD5636")){
						if(point.Floor.equals("F1")){
							floorbtn.setText("历史展厅");		
							}else if(point.Floor.equals("F2")){
								floorbtn.setText("现代展厅");		
							}
					}else{
						floorbtn.setText(point.Floor);
					}					
					mapSurfaceView.drawPosition(point.Xcor, point.Ycor);
				}
			}
				
		}

	};
	
	private boolean openMap(String building,String floor) {
		if (floor == null)
			return false;
		String picPath =G.APPPath+"/mapdata/jpg/" + building + "/map_jpg/" + floor + ".jpg";
		File mFile = new File(picPath);
		if (mFile.exists()) {
			mapSurfaceView.loadMap(picPath);
			return true;
		} else {
			Toast.makeText(getApplicationContext(), "地图不存在...", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
	}
	

}
