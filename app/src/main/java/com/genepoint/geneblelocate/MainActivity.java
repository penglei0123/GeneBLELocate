package com.genepoint.geneblelocate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.genepoint.appconfig.G;
import com.genepoint.common.LogDebug;
import com.genepoint.datapack.Building;
import com.genepoint.datapack.BuildingAdapter;
import com.genepoint.datapack.Floor_fromJSON;
import com.genepoint.geneblelocate.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity{
	private Activity mActivity;
	
	public List<Building> mBuildings=new ArrayList<Building>();//建筑集合
	public static Building mBuilding;
	public BuildingAdapter buildingAdapter;
	private ListView buildingListView;
	private ProgressDialog progress;

	public String numvs="";
	private Context mContext;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_buildings);
		mContext=getApplicationContext();
		buildingListView=(ListView)findViewById(R.id.id_buildinglist);	
		readMapFromSDCard();
		buildingAdapter=new BuildingAdapter(mBuildings);
		
		buildingListView.setAdapter(buildingAdapter);
		buildingListView.setOnItemClickListener(buildingSelectListener);
	
	}

	private OnItemClickListener buildingSelectListener=new OnItemClickListener() {		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Building building = buildingAdapter.getItem(position);
			Intent intent=new Intent(mContext,LocateActivity.class);
			Bundle bundle=new Bundle();
			bundle.putSerializable("BUILDING",building);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
		}
	};

	/**
	 * 读取gis地图
	 */
	private void readMapFromSDCard() {
		File mfile = new File( G.GISMAPDATA_PATH);
		if (!mfile.isDirectory()) {
			Toast.makeText(mContext, "本地不存在任何地图，请加载", Toast.LENGTH_SHORT).show();
		} else {
			File[] files = mfile.listFiles();
			if (files.equals(null)) {
				Toast.makeText(mContext, "本地不存在任何地图，请加载", Toast.LENGTH_LONG).show();
			} else {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					if(file.isDirectory()){
						File mapdate=new File(file.getAbsolutePath()+"/index.dat");
						File buildingInfo=new File(file.getAbsolutePath()+"/buildingInfo.json");
						if(mapdate.exists()&&buildingInfo.exists()){						
							//读取buildinginfo.json,实例化一个building
							Building building=getBuildingInfo(file.getAbsolutePath()+"/buildingInfo.json");
							building.setBuilingMapPath(mapdate.getAbsolutePath());
							mBuildings.add(building);														
						}
					}
				}
			}
		}

	}


	/**
	 * 从Json获取建筑信息返回一个Building 对象
	 * 
	 * @param buildingInfoFilePath
	 *            地图数据里的buildinginfo.json 路径
	 */
	private Building getBuildingInfo(String buildingInfoFilePath) {
		Building building=new Building();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(buildingInfoFilePath));
			String lineStr = "";
			StringBuilder sBuilder = new StringBuilder();
			while ((lineStr = bReader.readLine()) != null) {
				sBuilder.append(lineStr);
			}
			bReader.close();
			JSONObject jsonObject = new JSONObject(sBuilder.toString());
			String buildingName = jsonObject.getString("buildingName");
			String buildingCode = jsonObject.getString("buildingID");	
			building.setLongitude(jsonObject.getDouble("centerX"));
			building.setLatitude(jsonObject.getDouble("centerY"));
			building.setLeftBottomX(jsonObject.getDouble("leftBottomX"));
			building.setLeftBottomY(jsonObject.getDouble("leftBottomY"));
			building.setRightTopX(jsonObject.getDouble("rightTopX"));
			building.setRightTopY(jsonObject.getDouble("rightTopY"));						
			building.setBuilingName(buildingName);
			building.setBuildingID(buildingCode);
			building.setFloorCount(jsonObject.getInt("floorCount"));
			JSONArray floorArray=jsonObject.getJSONArray("floorGroup");
			for (int j = 0; j < floorArray.length(); j++) {
				JSONObject floordata = floorArray.getJSONObject(j);
				Floor_fromJSON floor = new Floor_fromJSON();
				floor.setIndex(floordata.getInt("index"));
				floor.setName(floordata.getString("name")); 
				building.add(floor);
			}				
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return building;

	}

}
