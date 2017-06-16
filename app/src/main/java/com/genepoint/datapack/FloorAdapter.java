package com.genepoint.datapack;



import com.genepoint.geneblelocate.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * 
 */
public class FloorAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	ViewHolder holder = null;
	Context context;
	String[] floors;
	int n = 0;

	public FloorAdapter(Context context, String[] f) {
		inflater = LayoutInflater.from(context);
		this.context = context;
		floors = f;
	}
	
	public void setFloors(String[] f) {
		floors = f;
		notifyDataSetChanged();
	}

	public void setCurrentFloor(String floorName) {
		for(int i = 0; i < floors.length; i++)
		{
			if(floorName.trim().equalsIgnoreCase(floors[i].trim()))
			{
				n = i;
				break;
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return floors.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.m_listview_item, null);
			holder.tv = (TextView) convertView.findViewById(R.id.item11);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (position == n) {
			convertView.setBackgroundColor(Color.parseColor("#500000FF"));
		} else {
			convertView.setBackgroundDrawable(null);
		}
		holder.tv.setText(floors[position]);
		return convertView;
	}
	private class ViewHolder {
		TextView tv;
	}
}
