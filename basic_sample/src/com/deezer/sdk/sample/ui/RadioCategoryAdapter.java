package com.deezer.sdk.sample.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.deezer.sdk.model.Radio;
import com.deezer.sdk.model.RadioCategory;
import com.deezer.sdk.sample.R;


/**
 * An adapter for an ExpandableListView to display radio categories
 * 
 * @author Deezer
 * 
 */
public class RadioCategoryAdapter extends BaseExpandableListAdapter {
    
    private final List<RadioCategory> mList;
    private final Context mContext;
    
    /**
     * 
     * @param context
     *            the current application context
     * @param list
     *            the list to adapt in an expandable list view
     */
    public RadioCategoryAdapter(Context context, List<RadioCategory> list) {
        
        mContext = context;
        mList = list;
    }
    
    @Override
    public int getGroupCount() {
        return mList.size();
    }
    
    @Override
    public RadioCategory getGroup(int groupPosition) {
        return mList.get(groupPosition);
    }
    
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(
                    R.layout.item_radio_category,
                    parent, false);
        }
        
        ((TextView) v).setText(getGroup(groupPosition).getTitle());
        
        return v;
    }
    
    @Override
    public int getChildrenCount(int groupPosition) {
        return getGroup(groupPosition).getRadios().size();
    }
    
    @Override
    public Radio getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getRadios().get(childPosition);
    }
    
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        
        return (groupPosition * 0xF0000000L) + childPosition;
    }
    
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(mContext).inflate(
                    android.R.layout.simple_list_item_1,
                    parent, false);
        }
        
        ((TextView) v.findViewById(android.R.id.text1)).setText(getChild(groupPosition,
                childPosition).getTitle());
        
        return v;
    }
    
    
    
    @Override
    public boolean hasStableIds() {
        return false;
    }
    
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
