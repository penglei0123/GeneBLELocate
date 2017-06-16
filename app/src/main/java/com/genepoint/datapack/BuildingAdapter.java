package com.genepoint.datapack;

import java.util.List;

import com.genepoint.geneblelocate.R;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class BuildingAdapter extends BaseAdapter {

    private List<Building> array;

    public BuildingAdapter(List<Building> buildings) {   	
        this.array = buildings;
    }

    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public Building getItem(int position) {
        return array.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        
        if (v == null) {
            v = View.inflate(parent.getContext(), R.layout.builds_item, null);
        }

        ViewHolder holder = (ViewHolder) v.getTag();

        if (holder == null) {
            holder = new ViewHolder(v);
            v.setTag(holder);
        }

        Building info = getItem(position);
        holder.textView.setText(info.getBuilingName());

        return v;
    }

    class ViewHolder {
    	AlwaysMarqueeTextView textView;
      //  TextView textView;

        public ViewHolder(View v) {
            textView = (AlwaysMarqueeTextView) v.findViewById(R.id.textView);
        }
    }

}

