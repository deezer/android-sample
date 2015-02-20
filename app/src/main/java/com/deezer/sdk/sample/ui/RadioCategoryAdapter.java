package com.deezer.sdk.sample.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deezer.sdk.model.AImageOwner.ImageSize;
import com.deezer.sdk.model.Radio;
import com.deezer.sdk.model.RadioCategory;
import com.deezer.sdk.sample.R;
import com.squareup.picasso.Picasso;


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
    public RadioCategoryAdapter(final Context context, final List<RadioCategory> list) {
        
        mContext = context;
        mList = list;
    }
    
    @Override
    public int getGroupCount() {
        return mList.size();
    }
    
    @Override
    public RadioCategory getGroup(final int groupPosition) {
        return mList.get(groupPosition);
    }
    
    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }
    
    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded,
            final View convertView,
            final ViewGroup parent) {
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
    public int getChildrenCount(final int groupPosition) {
        return getGroup(groupPosition).getRadios().size();
    }
    
    @Override
    public Radio getChild(final int groupPosition, final int childPosition) {
        return getGroup(groupPosition).getRadios().get(childPosition);
    }
    
    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        
        return (groupPosition * 0xF0000000L) + childPosition;
    }
    
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
            final boolean isLastChild,
            final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.item_title_cover, parent, false);
        }
        
        Radio radio = getChild(groupPosition, childPosition);
        
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(radio.getTitle());
        
        ImageView imageView = (ImageView) view.findViewById(android.R.id.icon);
        Picasso.with(mContext).load(radio.getImageUrl(ImageSize.small))
                .into(imageView);
        
        return view;
    }
    
    
    
    @Override
    public boolean hasStableIds() {
        return false;
    }
    
    @Override
    public boolean isChildSelectable(final int groupPosition, final int childPosition) {
        return true;
    }
}
