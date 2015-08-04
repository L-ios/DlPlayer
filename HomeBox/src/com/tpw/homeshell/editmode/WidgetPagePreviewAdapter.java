
package com.tpw.homeshell.editmode;

import java.util.List;

import com.tpw.homeshell.widgetpage.WidgetPageManager.WidgetPageInfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.tpw.homeshell.LauncherApplication;
import com.tpw.homeshell.R;

//this file add by huangweiwei, topwise, 2015-7-1
public class WidgetPagePreviewAdapter extends BaseAdapter {
	private List<WidgetPageInfo> mWigetPageList;
    private LayoutInflater mInflater;
    private Context mContext;

    public WidgetPagePreviewAdapter(Context context) {
    	mWigetPageList = LauncherApplication.getLauncher().mWidgetPageManager.getWigetPageTotalList();
        mInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mWigetPageList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            ViewHolder vh = new ViewHolder();
            convertView = mInflater.inflate(R.layout.preview_item, parent, false);

            vh.previewImgView = (ImageView) convertView.findViewById(R.id.preview_image);
            vh.previewImgView.setImageDrawable(mWigetPageList.get(position).getPreviewDrawable());
            vh.previewImgView.setScaleType(ScaleType.FIT_CENTER);
            vh.titleTextView = (TextView) convertView.findViewById(R.id.preview_title);
            vh.titleTextView.setText(mWigetPageList.get(position).getTitle());
            vh.previewChecked = (ImageView) convertView.findViewById(R.id.preview_checked);
            if (isChecked(position)) {
                vh.previewChecked.setVisibility(View.VISIBLE);
            } else {
                vh.previewChecked.setVisibility(View.GONE);
            }
            vh.position = position;
            convertView.setTag(vh);

        } else {
            ViewHolder vh = (ViewHolder) convertView.getTag();
            vh.previewImgView.setImageDrawable(mWigetPageList.get(position).getPreviewDrawable());
            vh.titleTextView.setText(mWigetPageList.get(position).getTitle());
            vh.position = position;
            if (isChecked(position)) {
                vh.previewChecked.setVisibility(View.VISIBLE);
            } else {
                vh.previewChecked.setVisibility(View.GONE);
            }

        }
        return convertView;
    }

    private boolean isChecked(int position) {
        return !mWigetPageList.get(position).getIsRemoved();
    }
    
    public void onClickItem(int position) {
    	android.util.Log.d("huangweiwei", "onClickItem position");
    	boolean removed = mWigetPageList.get(position).getIsRemoved();
    	mWigetPageList.get(position).setIsRemoved(!removed);
    }

    class ViewHolder {
        public int position;
        public ImageView previewImgView;
        public TextView titleTextView;
        public ImageView previewChecked;
    }

}
